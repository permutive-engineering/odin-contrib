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

package com.permutive.logging

import cats.Applicative
import cats.syntax.all._

import io.odin.Level
import io.odin.meta.Position
import io.odin.{Logger => OdinLogger}
import org.typelevel.log4cats.SelfAwareStructuredLogger

object Odin2Log4Cats {

  abstract class OdinSelfAwareStructuredLogger[F[_]](odin: OdinLogger[F])(implicit
      F: Applicative[F],
      pos: Position
  ) extends SelfAwareStructuredLogger[F] {

    override def error(t: Throwable)(message: => String): F[Unit] =
      odin.error(message, t)

    override def warn(t: Throwable)(message: => String): F[Unit] =
      odin.warn(message, t)

    override def info(t: Throwable)(message: => String): F[Unit] =
      odin.info(message, t)

    override def debug(t: Throwable)(message: => String): F[Unit] =
      odin.debug(message, t)

    override def trace(t: Throwable)(message: => String): F[Unit] =
      odin.trace(message, t)

    override def error(message: => String): F[Unit] = odin.error(message)

    override def warn(message: => String): F[Unit] = odin.warn(message)

    override def info(message: => String): F[Unit] = odin.info(message)

    override def debug(message: => String): F[Unit] = odin.debug(message)

    override def trace(message: => String): F[Unit] = odin.trace(message)

    override def trace(ctx: Map[String, String])(msg: => String): F[Unit] =
      odin.trace(msg, ctx)

    override def trace(ctx: Map[String, String], t: Throwable)(
        msg: => String
    ): F[Unit] = odin.trace(msg, ctx, t)

    override def debug(ctx: Map[String, String])(msg: => String): F[Unit] =
      odin.debug(msg, ctx)

    override def debug(ctx: Map[String, String], t: Throwable)(
        msg: => String
    ): F[Unit] = odin.debug(msg, ctx, t)

    override def info(ctx: Map[String, String])(msg: => String): F[Unit] =
      odin.info(msg, ctx)

    override def info(ctx: Map[String, String], t: Throwable)(
        msg: => String
    ): F[Unit] = odin.info(msg, ctx, t)

    override def warn(ctx: Map[String, String])(msg: => String): F[Unit] =
      odin.warn(msg, ctx)

    override def warn(ctx: Map[String, String], t: Throwable)(
        msg: => String
    ): F[Unit] = odin.warn(msg, ctx, t)

    override def error(ctx: Map[String, String])(msg: => String): F[Unit] =
      odin.error(msg, ctx)

    override def error(ctx: Map[String, String], t: Throwable)(
        msg: => String
    ): F[Unit] = odin.error(msg, ctx, t)

    override def isTraceEnabled: F[Boolean] =
      (odin.minLevel === Level.Trace).pure

    override def isDebugEnabled: F[Boolean] =
      (odin.minLevel === Level.Debug).pure

    override def isInfoEnabled: F[Boolean] = (odin.minLevel === Level.Info).pure

    override def isWarnEnabled: F[Boolean] = (odin.minLevel === Level.Warn).pure

    override def isErrorEnabled: F[Boolean] =
      (odin.minLevel === Level.Error).pure

  }

  def convert[F[_]: Applicative](
      odin: OdinLogger[F]
  )(implicit pos: Position): SelfAwareStructuredLogger[F] =
    new OdinSelfAwareStructuredLogger[F](odin)(
      implicitly,
      pos.copy(line = -1)
    ) {}

}
