package com.permutive.logging.odin.testing

import cats.Monad
import cats.effect.kernel.{Clock, Ref}
import io.odin.{Level, Logger, LoggerMessage}
import io.odin.loggers.DefaultLogger
import cats.syntax.functor._

import scala.collection.immutable.Queue

class OdinRefLogger[F[_]: Clock: Monad] private (
    minLevel: Level,
    private val ref: Ref[F, Queue[LoggerMessage]]
) extends DefaultLogger[F](minLevel) {
  def getMessages: F[Queue[LoggerMessage]] = ref.get

  override def submit(msg: LoggerMessage): F[Unit] = ref.update(_.appended(msg))

  override def withMinimalLevel(level: Level): Logger[F] =
    new OdinRefLogger[F](level, ref)
}

object OdinRefLogger {
  def create[F[_]: Monad: Clock: Ref.Make](
      minLevel: Level = Level.Info
  ): F[OdinRefLogger[F]] =
    Ref.of(Queue.empty[LoggerMessage]).map { ref =>
      new OdinRefLogger[F](minLevel, ref)
    }
}
