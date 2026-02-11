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

package com.permutive.logging.slf4j.odin

import cats.syntax.partialOrder._

import io.odin.Level
import org.slf4j.Logger
import org.slf4j.helpers.FormattingTuple
import org.slf4j.helpers.MarkerIgnoringBase
import org.slf4j.helpers.MessageFormatter

class LoggerAdapter(loggerName: String) extends MarkerIgnoringBase with Logger {

  override def getName: String = loggerName

  private def run(
      level: Level,
      msg: String,
      t: Option[Throwable] = None
  ): Unit =
    GlobalLogger.get.run(loggerName, level, msg, t)

  private[slf4j] def runFormatted(level: Level, tuple: FormattingTuple): Unit =
    run(level, tuple.getMessage, Option(tuple.getThrowable))

  override lazy val isTraceEnabled: Boolean =
    GlobalLogger.get.minLevel <= Level.Trace

  override def trace(msg: String): Unit =
    if (isTraceEnabled) run(Level.Trace, msg)

  override def trace(format: String, arg: Any): Unit =
    if (isTraceEnabled) runFormatted(Level.Trace, MessageFormatter.format(format, arg))

  override def trace(format: String, arg1: Any, arg2: Any): Unit =
    if (isTraceEnabled) runFormatted(Level.Trace, MessageFormatter.format(format, arg1, arg2))

  override def trace(msg: String, t: Throwable): Unit =
    if (isTraceEnabled) run(Level.Trace, msg, Option(t))

  override def trace(format: String, arguments: AnyRef*): Unit =
    if (isTraceEnabled)
      runFormatted(
        Level.Trace,
        MessageFormatter.arrayFormat(format, arguments.toArray)
      )

  override lazy val isDebugEnabled: Boolean =
    GlobalLogger.get.minLevel <= Level.Debug

  override def debug(msg: String): Unit =
    if (isDebugEnabled) run(Level.Debug, msg)

  override def debug(format: String, arg: Any): Unit =
    if (isDebugEnabled) runFormatted(Level.Debug, MessageFormatter.format(format, arg))

  override def debug(format: String, arg1: Any, arg2: Any): Unit =
    if (isDebugEnabled) runFormatted(Level.Debug, MessageFormatter.format(format, arg1, arg2))

  override def debug(msg: String, t: Throwable): Unit =
    if (isDebugEnabled) run(Level.Debug, msg, Option(t))

  override def debug(format: String, arguments: AnyRef*): Unit =
    if (isDebugEnabled)
      runFormatted(
        Level.Debug,
        MessageFormatter.arrayFormat(format, arguments.toArray)
      )

  override lazy val isInfoEnabled: Boolean =
    GlobalLogger.get.minLevel <= Level.Info

  override def info(msg: String): Unit =
    if (isInfoEnabled) run(Level.Info, msg)

  override def info(format: String, arg: Any): Unit =
    if (isInfoEnabled) runFormatted(Level.Info, MessageFormatter.format(format, arg))

  override def info(format: String, arg1: Any, arg2: Any): Unit =
    if (isInfoEnabled) runFormatted(Level.Info, MessageFormatter.format(format, arg1, arg2))

  override def info(msg: String, t: Throwable): Unit =
    if (isInfoEnabled) run(Level.Info, msg, Option(t))

  override def info(format: String, arguments: AnyRef*): Unit =
    if (isInfoEnabled)
      runFormatted(
        Level.Info,
        MessageFormatter.arrayFormat(format, arguments.toArray)
      )

  override lazy val isWarnEnabled: Boolean =
    GlobalLogger.get.minLevel <= Level.Warn

  override def warn(msg: String): Unit =
    if (isWarnEnabled) run(Level.Warn, msg)

  override def warn(format: String, arg: Any): Unit =
    if (isWarnEnabled) runFormatted(Level.Warn, MessageFormatter.format(format, arg))

  override def warn(format: String, arg1: Any, arg2: Any): Unit =
    if (isWarnEnabled) runFormatted(Level.Warn, MessageFormatter.format(format, arg1, arg2))

  override def warn(msg: String, t: Throwable): Unit =
    if (isWarnEnabled) run(Level.Warn, msg, Option(t))

  override def warn(format: String, arguments: AnyRef*): Unit =
    if (isWarnEnabled)
      runFormatted(
        Level.Warn,
        MessageFormatter.arrayFormat(format, arguments.toArray)
      )

  override lazy val isErrorEnabled: Boolean =
    GlobalLogger.get.minLevel <= Level.Error

  override def error(msg: String): Unit =
    if (isErrorEnabled) run(Level.Error, msg)

  override def error(format: String, arg: Any): Unit =
    if (isErrorEnabled) runFormatted(Level.Error, MessageFormatter.format(format, arg))

  override def error(format: String, arg1: Any, arg2: Any): Unit =
    if (isErrorEnabled) runFormatted(Level.Error, MessageFormatter.format(format, arg1, arg2))

  override def error(msg: String, t: Throwable): Unit =
    if (isErrorEnabled) run(Level.Error, msg, Option(t))

  override def error(format: String, arguments: AnyRef*): Unit =
    if (isErrorEnabled)
      runFormatted(
        Level.Error,
        MessageFormatter.arrayFormat(format, arguments.toArray)
      )

}
