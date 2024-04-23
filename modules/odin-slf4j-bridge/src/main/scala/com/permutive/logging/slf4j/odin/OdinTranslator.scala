/*
 * Copyright 2022-2024 Permutive Ltd. <https://permutive.com>
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

package com.permutive.logging.slf4j.odin

import java.io.PrintStream

import cats.Eval
import cats.effect.kernel.Clock
import cats.effect.kernel.Sync
import cats.effect.std.Dispatcher
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.partialOrder._

import io.odin.Level
import io.odin.Logger
import io.odin.LoggerMessage
import io.odin.formatter.Formatter
import io.odin.meta.Position

private[odin] trait OdinTranslator {

  def minLevel: Level

  def run(
      name: String,
      level: Level,
      msg: String,
      t: Option[Throwable] = None
  ): Unit

}

object OdinTranslator {

  def apply[F[_]: Sync](
      underlying: Logger[F]
  )(implicit dispatcher: Dispatcher[F]): OdinTranslator =
    new OdinTranslator {

      override def run(
          loggerName: String,
          level: Level,
          msg: String,
          t: Option[Throwable]
      ): Unit =
        dispatcher.unsafeRunSync(for {
          timestamp <- Clock[F].realTime
          _ <- underlying.log(
                 makeMessage(loggerName, level, msg, t, timestamp.toMillis)
               )
        } yield ())

      override def minLevel: Level = underlying.minLevel

    }

  /** Pretty much a copy/paste of Odin's console logger, but without any cats-effect protections.
    *
    * This mitigates this "bug" https://github.com/valskalla/odin/issues/364 where when the CE `Dispatcher` is created
    * in an object in an unsafe way and thread that is logging using Slf4j is interrupted so does the dispatcher, and
    * the object fails initialisation.
    *
    * This issue _does not_ affect dispatchers that have been safely created inside the `IORuntime` properly. So when
    * setting the global logger later in the initialisation does not result in fatal exceptions crashing the whole app
    * when a thread that is trying to log gets interrupted
    */
  private[odin] def unsafeConsole(
      level: Level,
      formatter: Formatter
  ): OdinTranslator = new OdinTranslator {

    override def minLevel: Level = level

    private def println(out: PrintStream, msg: LoggerMessage): Unit =
      out.println(formatter.format(msg))

    override def run(
        loggerName: String,
        level: Level,
        msg: String,
        t: Option[Throwable]
    ): Unit =
      if (level >= minLevel) {
        val message =
          makeMessage(loggerName, level, msg, t, System.currentTimeMillis())

        if (level < Level.Warn) {
          println(System.out, message)
        } else {
          println(System.err, message)
        }
      } else {
        ()
      }

  }

  private def makeMessage(
      loggerName: String,
      level: Level,
      msg: String,
      t: Option[Throwable],
      timestamp: Long
  ) =
    LoggerMessage(
      level = level,
      message = Eval.now(msg),
      context = Map.empty,
      exception = t,
      position = Position(
        fileName = loggerName,
        enclosureName = loggerName,
        packageName = loggerName,
        line = -1
      ),
      threadName = Thread.currentThread().getName,
      timestamp = timestamp
    )

}
