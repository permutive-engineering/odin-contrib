package com.permutive.logging.slf4j.odin

import java.util.concurrent.atomic.AtomicReference

import cats.effect.kernel.{Async, Resource, Sync}
import cats.effect.std.Dispatcher
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import io.odin.formatter.Formatter
import io.odin.{Level, Logger}

import scala.util.control.NonFatal

/**
  * Utilities for getting a logger that can be used by SLF4J.
  */
object GlobalLogger {
  private val default: OdinTranslator = OdinTranslator.unsafeConsole(level = Level.Info, Formatter.default)

  private val ref = new AtomicReference[OdinTranslator]

  // the logger may not yet be allocated, so do a null check
  // all in try/catch for efficiency
  private[odin] def get: OdinTranslator =
    try {
      val log = ref.get()
      if (log == null) default else log
    } catch {
      case NonFatal(th) =>
        // Use default logger if there is an error getting the logger from the atomic ref
        default.run(this.getClass.getName, Level.Error, "Failed to get logger from atomic ref", Some(th))
        default
    }

  def isSet[F[_]: Sync]: F[Boolean] = Sync[F].delay(ref.get() != null)

  // sets the logger in the atomic ref
  // sets the ref to null when the resource is de-allocated in order to avoid resource leak of the underlying logger
  def setLogger[F[_]: Async](logger: Logger[F]): Resource[F, Unit] = {
    val set = Dispatcher
      .sequential[F]
      .map(implicit dis => OdinTranslator[F](logger))
      .flatMap(log => Resource.make(Sync[F].delay(ref.set(log)))(_ => Sync[F].delay(ref.set(null))))

    val error: Resource[F, Unit] = new RuntimeException(
      "Logger already set: Cannot set the global logger multiple times"
    ).raiseError[Resource[F, *], Unit]

    Resource.eval(Sync[F].delay(ref.get())).map(_ == null).ifM(set, error)
  }
}
