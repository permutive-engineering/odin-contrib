/*
 * Copyright 2022 Permutive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.permutive.logging.dynamic.odin

import cats.effect.unsafe.IORuntime
import cats.effect.{IO, Resource}
import com.permutive.logging.dynamic.odin.DynamicOdinConsoleLogger.RuntimeConfig
import com.permutive.logging.odin.testing.OdinRefLogger
import io.odin.{Level, LoggerMessage}
import io.odin.formatter.Formatter
import io.odin.meta.Position
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Arbitrary
import org.scalacheck.effect.PropF

import scala.collection.immutable.Queue
import scala.concurrent.duration._

class DynamicOdinLoggerSpec extends CatsEffectSuite with ScalaCheckEffectSuite {

  implicit val runtime: IORuntime = IORuntime.global

  implicit val arbPosition: Arbitrary[Position] =
    Arbitrary(
      Arbitrary
        .arbitrary[(String, String, String, Int)]
        .map((Position.apply _).tupled)
    )

  test("record a message") {
    PropF.forAllF { (message: String) =>
      val messages = runTest(_.info(message))

      messages.map(_.map(_.message.value).toList).assertEquals(List(message))
    }
  }

  test("update min-level config") {
    PropF.forAllF { (messageBeforeChange: String, messageAfterChange: String) =>
      val messages = runTest { logger =>
        logger.info(messageBeforeChange) >>
          IO.sleep(10.millis) >>
          logger.update(RuntimeConfig(minLevel = Level.Warn)) >>
          logger.info(messageAfterChange)
      }
      messages
        .map(_.map(_.message.value).toList)
        .assertEquals(List(messageBeforeChange))
    }
  }

  test("update enclosure log level") {
    PropF.forAllNoShrinkF {
      (
          infoMsg1Pos1: String,
          infoMsg2Pos1: String,
          warnMsg1Pos1: String,
          infoMsg2Pos2: String,
          position1: Position,
          position2: Position
      ) =>
        val messages = runTest { logger =>
          val positionWhichChangesLevel =
            position1.copy(enclosureName = position1.enclosureName + "changes")
          logger.info(infoMsg1Pos1)(implicitly, positionWhichChangesLevel) >>
            IO.sleep(10.millis) >>
            logger.update(
              RuntimeConfig(
                Level.Info,
                Map(positionWhichChangesLevel.enclosureName -> Level.Warn)
              )
            ) >>
            logger.info(infoMsg2Pos1)(implicitly, positionWhichChangesLevel) >>
            logger.warn(warnMsg1Pos1)(implicitly, positionWhichChangesLevel) >>
            logger.info(infoMsg2Pos2)(implicitly, position2)
        }
        messages
          .map(_.map(_.message.value).toList)
          .assertEquals(List(infoMsg1Pos1, warnMsg1Pos1, infoMsg2Pos2))
    }
  }

  def runTest(
      useLogger: DynamicOdinConsoleLogger[IO] => IO[Unit]
  ): IO[Queue[LoggerMessage]] = (for {
    testLogger <- Resource.eval(OdinRefLogger.create[IO]())
    dynamic <- DynamicOdinConsoleLogger.create[IO](
      DynamicOdinConsoleLogger
        .Config(formatter = Formatter.default, asyncTimeWindow = 0.nanos),
      RuntimeConfig(Level.Info)
    )(config => testLogger.withMinimalLevel(config.minLevel))
    _ <- Resource.eval(useLogger(dynamic))
  } yield testLogger)
    .use { testLogger =>
      IO.sleep(50.millis) >> testLogger.getMessages
    }
}
