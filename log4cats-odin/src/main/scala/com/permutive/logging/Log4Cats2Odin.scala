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

package com.permutive.logging

import cats.Applicative
import cats.syntax.foldable._

import io.odin.Level
import io.odin.LoggerMessage
import io.odin.meta.Position
import io.odin.meta.Render
import io.odin.meta.ToThrowable
import io.odin.{Logger => OdinLogger}
import org.typelevel.log4cats.SelfAwareStructuredLogger

@SuppressWarnings(Array("scalafix:DisableSyntax.implicitConversion"))
object Log4Cats2Odin {

  abstract class Log4CatsOdinLogger[F[_]: Applicative](
      logger: SelfAwareStructuredLogger[F],
      level: Level = Level.Info
  ) extends OdinLogger[F] {

    implicit private def renderMessage[M](msg: => M)(implicit
        render: Render[M]
    ): String = render.render(msg)

    implicit private def toThrowable[E](e: E)(implicit
        toThrowable: ToThrowable[E]
    ): Throwable =
      toThrowable.throwable(e)

    override def minLevel: Level = level

    override def withMinimalLevel(level: Level): OdinLogger[F] =
      new Log4CatsOdinLogger[F](logger, level) {}

    override def log(msg: LoggerMessage): F[Unit] = msg.level match {
      case Level.Trace =>
        msg.exception.fold(trace(msg.message.value, msg.context))(t => trace(msg.message.value, msg.context, t))
      case Level.Debug =>
        msg.exception.fold(debug(msg.message.value, msg.context))(t => debug(msg.message.value, msg.context, t))
      case Level.Info =>
        msg.exception.fold(info(msg.message.value, msg.context))(t => info(msg.message.value, msg.context, t))
      case Level.Warn =>
        msg.exception.fold(warn(msg.message.value, msg.context))(t => warn(msg.message.value, msg.context, t))
      case Level.Error =>
        msg.exception.fold(error(msg.message.value, msg.context))(t => error(msg.message.value, msg.context, t))
    }

    override def log(msgs: List[LoggerMessage]): F[Unit] =
      msgs.traverse_(log)

    override def trace[M](
        msg: => M
    )(implicit render: Render[M], position: Position): F[Unit] =
      logger.trace(msg)

    override def trace[M, E](msg: => M, e: E)(implicit
        render: Render[M],
        tt: ToThrowable[E],
        position: Position
    ): F[Unit] = logger.trace(e)(msg)

    override def trace[M](msg: => M, ctx: Map[String, String])(implicit
        render: Render[M],
        position: Position
    ): F[Unit] = logger.trace(ctx)(msg)

    override def trace[M, E](msg: => M, ctx: Map[String, String], e: E)(implicit
        render: Render[M],
        tt: ToThrowable[E],
        position: Position
    ): F[Unit] = logger.trace(ctx, e)(msg)

    override def debug[M](
        msg: => M
    )(implicit render: Render[M], position: Position): F[Unit] =
      logger.debug(msg)

    override def debug[M, E](msg: => M, e: E)(implicit
        render: Render[M],
        tt: ToThrowable[E],
        position: Position
    ): F[Unit] = logger.debug(e)(msg)

    override def debug[M](msg: => M, ctx: Map[String, String])(implicit
        render: Render[M],
        position: Position
    ): F[Unit] = logger.debug(ctx)(msg)

    override def debug[M, E](msg: => M, ctx: Map[String, String], e: E)(implicit
        render: Render[M],
        tt: ToThrowable[E],
        position: Position
    ): F[Unit] = logger.debug(ctx, e)(msg)

    override def info[M](
        msg: => M
    )(implicit render: Render[M], position: Position): F[Unit] =
      logger.info(msg)

    override def info[M, E](msg: => M, e: E)(implicit
        render: Render[M],
        tt: ToThrowable[E],
        position: Position
    ): F[Unit] = logger.info(e)(msg)

    override def info[M](msg: => M, ctx: Map[String, String])(implicit
        render: Render[M],
        position: Position
    ): F[Unit] = logger.info(ctx)(msg)

    override def info[M, E](msg: => M, ctx: Map[String, String], e: E)(implicit
        render: Render[M],
        tt: ToThrowable[E],
        position: Position
    ): F[Unit] = logger.info(ctx, e)(msg)

    override def warn[M](
        msg: => M
    )(implicit render: Render[M], position: Position): F[Unit] =
      logger.warn(msg)

    override def warn[M, E](msg: => M, e: E)(implicit
        render: Render[M],
        tt: ToThrowable[E],
        position: Position
    ): F[Unit] = logger.warn(e)(msg)

    override def warn[M](msg: => M, ctx: Map[String, String])(implicit
        render: Render[M],
        position: Position
    ): F[Unit] = logger.warn(ctx)(msg)

    override def warn[M, E](msg: => M, ctx: Map[String, String], e: E)(implicit
        render: Render[M],
        tt: ToThrowable[E],
        position: Position
    ): F[Unit] = logger.warn(ctx, e)(msg)

    override def error[M](
        msg: => M
    )(implicit render: Render[M], position: Position): F[Unit] =
      logger.error(msg)

    override def error[M, E](msg: => M, e: E)(implicit
        render: Render[M],
        tt: ToThrowable[E],
        position: Position
    ): F[Unit] = logger.error(e)(msg)

    override def error[M](msg: => M, ctx: Map[String, String])(implicit
        render: Render[M],
        position: Position
    ): F[Unit] = logger.error(ctx)(msg)

    override def error[M, E](msg: => M, ctx: Map[String, String], e: E)(implicit
        render: Render[M],
        tt: ToThrowable[E],
        position: Position
    ): F[Unit] = logger.error(ctx, e)(msg)

  }

  def convert[F[_]: Applicative](
      logger: SelfAwareStructuredLogger[F],
      level: Level = Level.Info
  ): OdinLogger[F] =
    new Log4CatsOdinLogger[F](logger, level) {}

}
