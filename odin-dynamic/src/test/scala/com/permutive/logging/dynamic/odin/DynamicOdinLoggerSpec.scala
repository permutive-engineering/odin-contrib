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

import cats.effect.{IO, Resource}
import com.permutive.logging.dynamic.odin.DynamicOdinConsoleLogger.{
  LevelConfig,
  RuntimeConfig
}
import com.permutive.logging.odin.testing.OdinRefLogger
import io.odin.formatter.Formatter
import io.odin.meta.Position
import io.odin.{Level, LoggerMessage}
import munit.{CatsEffectSuite, ScalaCheckEffectSuite}
import org.scalacheck.Arbitrary
import org.scalacheck.effect.PropF

import scala.collection.immutable.Queue
import scala.concurrent.duration._

class DynamicOdinLoggerSpec extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val arbPosition: Arbitrary[Position] =
    Arbitrary(
      Arbitrary
        .arbitrary[(String, String, String, Int)]
        .map((Position.apply _).tupled)
    )

  private val defaultConfig = RuntimeConfig(Level.Info)

  test("record only messages at the min level") {
    PropF.forAllF { (debugMessage: String, infoMessage: String) =>
      val messages = runTest()(logger =>
        logger.debug(debugMessage) >>
          logger.info(infoMessage)
      )

      messages
        .map(_.map(_.message.value).toList)
        .assertEquals(List(infoMessage))
    }
  }

  test("disable logging for a particular enclosure") {
    PropF.forAllF {
      (
          pos1Msg: String,
          pos2Msg: String,
          position1: Position
      ) =>
        val position2 =
          position1.copy(enclosureName = position1.enclosureName + "2")
        val messages = runTest(
          RuntimeConfig(
            Level.Info,
            Map(
              position2.enclosureName -> LevelConfig.Off
            )
          )
        )(logger =>
          logger.info(pos1Msg)(implicitly, position1) >>
            logger.error(pos2Msg)(implicitly, position2)
        )

        messages
          .map(_.map(_.message.value).toList)
          .assertEquals(List(pos1Msg))
    }
  }

  test("raises default-level config") {
    PropF.forAllF { (messageBeforeChange: String, messageAfterChange: String) =>
      val messages = runTest() { logger =>
        logger.info(messageBeforeChange) >>
          IO.sleep(10.millis) >>
          logger.update(RuntimeConfig(defaultLevel = Level.Warn)) >>
          logger.info(messageAfterChange)
      }
      messages
        .map(_.map(_.message.value).toList)
        .assertEquals(List(messageBeforeChange))
    }
  }

  test("lowers default-level config") {
    PropF.forAllF { (messageBeforeChange: String, messageAfterChange: String) =>
      val messages = runTest() { logger =>
        logger.info(messageBeforeChange) >>
          IO.sleep(10.millis) >>
          logger.update(RuntimeConfig(defaultLevel = Level.Debug)) >>
          logger.debug(messageAfterChange)
      }
      messages
        .map(_.map(_.message.value).toList)
        .assertEquals(List(messageBeforeChange, messageAfterChange))
    }
  }

  test("overrides default level for a certain package") {
    PropF.forAllF { (message: String) =>
      val messages = runTest(
        RuntimeConfig(
          defaultLevel = Level.Warn,
          Map("com.permutive.logging.dynamic.odin" -> LevelConfig.Debug)
        )
      ) { logger =>
        logger.debug(message)
      }
      messages
        .map(_.map(_.message.value).toList)
        .assertEquals(List(message))
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
        val messages = runTest() { logger =>
          val positionWhichChangesLevel =
            position1.copy(enclosureName = position1.enclosureName + "changes")
          logger.info(infoMsg1Pos1)(implicitly, positionWhichChangesLevel) >>
            IO.sleep(10.millis) >>
            logger.update(
              RuntimeConfig(
                Level.Info,
                Map(positionWhichChangesLevel.enclosureName -> LevelConfig.Warn)
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

  private def runTest(initialConfig: RuntimeConfig = defaultConfig)(
      useLogger: DynamicOdinConsoleLogger[IO] => IO[Unit]
  ): IO[Queue[LoggerMessage]] = (for {
    testLogger <- Resource.eval(OdinRefLogger.create[IO]())
    dynamic <- DynamicOdinConsoleLogger.create[IO](
      DynamicOdinConsoleLogger
        .Config(formatter = Formatter.default, asyncTimeWindow = 0.nanos),
      initialConfig
    )(config => testLogger.withMinimalLevel(config.defaultLevel))
    _ <- Resource.eval(useLogger(dynamic))
  } yield testLogger)
    .use { testLogger =>
      IO.sleep(50.millis) >> testLogger.getMessages
    }
}
