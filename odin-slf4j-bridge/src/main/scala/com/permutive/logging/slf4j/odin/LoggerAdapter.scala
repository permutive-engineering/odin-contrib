package com.permutive.logging.slf4j.odin

import cats.syntax.partialOrder._
import io.odin.Level
import org.slf4j.Logger
import org.slf4j.helpers.{FormattingTuple, MarkerIgnoringBase, MessageFormatter}

class LoggerAdapter(loggerName: String) extends MarkerIgnoringBase with Logger with LoggerVarargsAdapter {

  override def getName: String = loggerName

  private def run(level: Level, msg: String, t: Option[Throwable] = None): Unit =
    GlobalLogger.get.run(loggerName, level, msg, t)

  private[slf4j] def runFormatted(level: Level, tuple: FormattingTuple): Unit =
    run(level, tuple.getMessage, Option(tuple.getThrowable))

  def isTraceEnabled: Boolean = GlobalLogger.get.minLevel <= Level.Trace

  def trace(msg: String): Unit = run(Level.Trace, msg)

  def trace(format: String, arg: Any): Unit = runFormatted(Level.Trace, MessageFormatter.format(format, arg))

  def trace(format: String, arg1: Any, arg2: Any): Unit =
    runFormatted(Level.Trace, MessageFormatter.format(format, arg1, arg2))

  def trace(msg: String, t: Throwable): Unit =
    run(Level.Trace, msg, Option(t))

  def isDebugEnabled: Boolean = GlobalLogger.get.minLevel <= Level.Debug

  def debug(msg: String): Unit = run(Level.Debug, msg)

  def debug(format: String, arg: Any): Unit = runFormatted(Level.Debug, MessageFormatter.format(format, arg))

  def debug(format: String, arg1: Any, arg2: Any): Unit =
    runFormatted(Level.Debug, MessageFormatter.format(format, arg1, arg2))

  def debug(msg: String, t: Throwable): Unit =
    run(Level.Debug, msg, Option(t))

  def isInfoEnabled: Boolean = GlobalLogger.get.minLevel <= Level.Info

  def info(msg: String): Unit = run(Level.Info, msg)

  def info(format: String, arg: Any): Unit = runFormatted(Level.Info, MessageFormatter.format(format, arg))

  def info(format: String, arg1: Any, arg2: Any): Unit =
    runFormatted(Level.Info, MessageFormatter.format(format, arg1, arg2))

  def info(msg: String, t: Throwable): Unit =
    run(Level.Info, msg, Option(t))

  def isWarnEnabled: Boolean = GlobalLogger.get.minLevel <= Level.Warn

  def warn(msg: String): Unit = run(Level.Warn, msg)

  def warn(format: String, arg: Any): Unit = runFormatted(Level.Warn, MessageFormatter.format(format, arg))

  def warn(format: String, arg1: Any, arg2: Any): Unit =
    runFormatted(Level.Warn, MessageFormatter.format(format, arg1, arg2))

  def warn(msg: String, t: Throwable): Unit =
    run(Level.Warn, msg, Option(t))

  def isErrorEnabled: Boolean = GlobalLogger.get.minLevel <= Level.Error

  def error(msg: String): Unit = run(Level.Error, msg)

  def error(format: String, arg: Any): Unit = runFormatted(Level.Error, MessageFormatter.format(format, arg))

  def error(format: String, arg1: Any, arg2: Any): Unit =
    runFormatted(Level.Error, MessageFormatter.format(format, arg1, arg2))

  def error(msg: String, t: Throwable): Unit =
    run(Level.Error, msg, Option(t))

}
