/*
 * Copyright 2022-2026 Permutive Ltd. <https://permutive.com>
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

import scala.annotation.nowarn
import scala.collection.immutable.Queue
import scala.concurrent.duration._

import cats.effect.IO
import cats.effect.Resource
import cats.effect.unsafe.IORuntime
import cats.syntax.all._

import com.permutive.logging.odin.testing.OdinRefLogger
import io.odin.Level
import io.odin.LoggerMessage
import io.odin.formatter.Formatter
import munit.CatsEffectSuite
import munit.Exceptions
import munit.Location
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.scalacheck.rng.Seed
import org.scalacheck.util.Pretty

class DynamicOdinLoggerSpec extends CatsEffectSuite with ScalaCheckSuite {

  implicit val runtime: IORuntime = IORuntime.global

  test("record a message") {
    PropF.forAllF { (message: String) =>
      val messages = runTest(_.info(message))

      messages.map(_.map(_.message.value).toList).assertEquals(List(message))
    }
  }

  test("update global log level") {
    PropF.forAllF { (message1: String, message2: String) =>
      val messages = runTest { logger =>
        logger.info(message1) >> IO.sleep(10.millis) >> logger.update(
          DynamicOdinConsoleLogger.RuntimeConfig(Level.Warn)
        ) >> logger.info(
          message2
        )
      }
      messages.map(_.map(_.message.value).toList).assertEquals(List(message1))
    }
  }

  test("update enclosure log level") {
    PropF.forAllF { (message1: String, message2: String, message3: String) =>
      val messages = runTest { logger =>
        logger.info(message1) >> IO.sleep(10.millis) >> logger.update(
          DynamicOdinConsoleLogger.RuntimeConfig(
            Level.Info,
            Map("com.permutive" -> Level.Warn)
          )
        ) >> logger.info(
          message2
        ) >> logger.warn(message3)
      }
      messages
        .map(_.map(_.message.value).toList)
        .assertEquals(List(message1, message3))
    }
  }

  def runTest(
      useLogger: DynamicOdinConsoleLogger[IO] => IO[Unit]
  ): IO[Queue[LoggerMessage]] = (for {
    testLogger <- Resource.eval(OdinRefLogger.create[IO]())
    dynamic    <- DynamicOdinConsoleLogger.create[IO](
                 DynamicOdinConsoleLogger.Config(formatter = Formatter.default),
                 DynamicOdinConsoleLogger.RuntimeConfig(Level.Info)
               )(config => testLogger.withMinimalLevel(config.minLevel))
    _ <- Resource.eval(useLogger(dynamic))
  } yield testLogger).use { testLogger =>
    IO.sleep(50.millis) >> testLogger.getMessages
  }

  private val genParameters: Gen.Parameters =
    Gen.Parameters.default
      .withLegacyShrinking(scalaCheckTestParameters.useLegacyShrinking)
      .withInitialSeed(
        scalaCheckTestParameters.initialSeed.getOrElse(
          Seed.fromBase64(scalaCheckInitialSeed).get
        )
      )

  @nowarn
  override def munitValueTransforms: List[ValueTransform] = {
    val testResultTransform = new ValueTransform(
      "ScalaCheck TestResult",
      { case p: Test.Result => super.munitValueTransform(parseTestResult(p)) }
    )

    val scalaCheckPropFValueTransform = new ValueTransform(
      "ScalaCheck PropF",
      { case p: PropF[f] =>
        super.munitValueTransform(checkPropF[f](p))
      }
    )

    super.munitValueTransforms :+ scalaCheckPropFValueTransform :+ testResultTransform
  }

  private def checkPropF[F[_]](prop: PropF[F])(implicit loc: Location): F[Unit] = {
    import prop.F
    prop.check(scalaCheckTestParameters, genParameters).map(fixResultException).map(parseTestResult)
  }

  private def parseTestResult(result: Test.Result)(implicit loc: Location): Unit = {
    if (!result.passed) {
      val seed        = genParameters.initialSeed.get
      val seedMessage =
        s"""|Failing seed: ${seed.toBase64}
            |You can reproduce this failure by adding the following override to your suite:
            |
            |  override def scalaCheckInitialSeed = "${seed.toBase64}"
            |""".stripMargin
      fail(seedMessage + "\n" + Pretty.pretty(result, scalaCheckPrettyParameters))
    }
  }

  private def fixResultException(result: Test.Result): Test.Result =
    result.copy(
      status = result.status match {
        case p @ Test.PropException(_, e, _) => p.copy(e = Exceptions.rootCause(e))
        case default                         => default
      }
    )

}
