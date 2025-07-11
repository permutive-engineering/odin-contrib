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

package com.permutive.logging.dynamic.odin

import cats.Applicative
import cats.Monad
import cats.effect.kernel._
import cats.kernel.Eq
import cats.syntax.eq._
import cats.syntax.flatMap._
import cats.syntax.functor._

import com.permutive.logging.dynamic.odin.DynamicOdinConsoleLogger.RuntimeConfig
import io.odin.Level
import io.odin.Logger
import io.odin.LoggerMessage
import io.odin.config.enclosureRouting
import io.odin.consoleLogger
import io.odin.formatter.Formatter
import io.odin.loggers.DefaultLogger
import io.odin.syntax._

trait DynamicOdinConsoleLogger[F[_]] extends Logger[F] {

  def update(config: RuntimeConfig): F[Boolean]

  def getConfig: F[RuntimeConfig]

}

class DynamicOdinConsoleLoggerImpl[F[_]: Monad: Clock] private[odin] (
    ref: Ref[F, (RuntimeConfig, Logger[F])],
    level: Level
)(make: RuntimeConfig => Logger[F])(implicit eq: Eq[RuntimeConfig])
    extends DefaultLogger[F](level)
    with DynamicOdinConsoleLogger[F] { outer =>

  protected def withLogger(f: Logger[F] => F[Unit]): F[Unit] = ref.get.flatMap { case (_, l) =>
    f(l)
  }

  override def update(config: RuntimeConfig): F[Boolean] =
    ref.get
      .map(_._1.neqv(config))
      .flatTap(Applicative[F].whenA(_)(ref.set(config -> make(config))))

  override def getConfig: F[RuntimeConfig] = ref.get.map(_._1)

  override def submit(msg: LoggerMessage): F[Unit] = withLogger(_.log(msg))

  override def withMinimalLevel(level: Level): Logger[F] =
    new DynamicOdinConsoleLoggerImpl[F](ref, level)(make) {

      override protected def withLogger(f: Logger[F] => F[Unit]): F[Unit] =
        outer.withLogger(l => f(l.withMinimalLevel(level)))

    }

}

object DynamicOdinConsoleLogger {

  final case class Config(
      formatter: Formatter,
      asyncMaxBufferSize: Option[Int] = None
  )

  final case class RuntimeConfig(
      minLevel: Level,
      levelMapping: Map[String, Level] = Map.empty
  )

  object RuntimeConfig {

    implicit val eq: Eq[RuntimeConfig] = cats.derived.semiauto.eq

  }

  def console[F[_]: Async](config: Config, initialConfig: RuntimeConfig)(implicit
      eq: Eq[RuntimeConfig]
  ): Resource[F, DynamicOdinConsoleLogger[F]] =
    create(config, initialConfig)(c => consoleLogger(config.formatter, c.minLevel))

  def create[F[_]: Async](
      config: Config,
      runtimeConfig: RuntimeConfig
  )(
      make: RuntimeConfig => Logger[F]
  )(implicit
      eq: Eq[RuntimeConfig]
  ): Resource[F, DynamicOdinConsoleLogger[F]] = {
    val makeWithLevels: RuntimeConfig => Logger[F] = { config =>
      val mainLogger = make(config)

      if (config.levelMapping.isEmpty) mainLogger
      else
        enclosureRouting(
          config.levelMapping.view
            .mapValues(mainLogger.withMinimalLevel)
            .toList: _*
        )
          .withFallback(mainLogger)
    }

    for {
      ref <- Resource.eval(
               Ref.of[F, (RuntimeConfig, Logger[F])](
                 runtimeConfig -> makeWithLevels(runtimeConfig)
               )
             )
      underlying = new DynamicOdinConsoleLoggerImpl[F](
                     ref,
                     runtimeConfig.minLevel
                   )(makeWithLevels)
      async <- underlying.withAsync(config.asyncMaxBufferSize)
    } yield new DefaultLogger[F](async.minLevel) with DynamicOdinConsoleLogger[F] {
      override def submit(msg: LoggerMessage): F[Unit] = async.log(msg)

      override def update(config: RuntimeConfig): F[Boolean] =
        underlying.update(config)
      override def getConfig: F[RuntimeConfig] = underlying.getConfig

      override def withMinimalLevel(level: Level): Logger[F] =
        async.withMinimalLevel(level)
    }
  }

}
