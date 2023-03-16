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

import cats.effect.kernel._
import cats.kernel.Eq
import cats.syntax.eq._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Applicative, Monad}
import com.permutive.logging.dynamic.odin.DynamicOdinConsoleLogger.RuntimeConfig
import io.odin.config.enclosureRouting
import io.odin.formatter.Formatter
import io.odin.loggers.DefaultLogger
import io.odin.syntax._
import io.odin.{consoleLogger, Level, Logger, LoggerMessage}

import scala.concurrent.duration._

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
  protected def withLogger(f: Logger[F] => F[Unit]): F[Unit] = ref.get.flatMap {
    case (_, l) => f(l)
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
  sealed abstract class Config private (
      val formatter: Formatter,
      val asyncTimeWindow: FiniteDuration,
      val asyncMaxBufferSize: Option[Int]
  )

  object Config {
    def apply(
        formatter: Formatter,
        asyncTimeWindow: FiniteDuration = 1.millis,
        asyncMaxBufferSize: Option[Int] = None
    ): Config = new Config(formatter, asyncTimeWindow, asyncMaxBufferSize) {}
  }

  sealed abstract class RuntimeConfig private (
      val minLevel: Level,
      val levelMapping: Map[String, LevelConfig]
  )
  object RuntimeConfig {
    def apply(
        minLevel: Level,
        levelMapping: Map[String, LevelConfig] = Map.empty
    ): RuntimeConfig = new RuntimeConfig(minLevel, levelMapping) {}

    implicit val eq: Eq[RuntimeConfig] =
      Eq.by(config => (config.minLevel, config.levelMapping))
  }

  sealed trait LevelConfig

  object LevelConfig {
    private[odin] trait ToLevel { self: LevelConfig =>
      def toLevel: Level
    }

    case object Trace extends LevelConfig with ToLevel {
      val toLevel = Level.Trace
    }
    case object Debug extends LevelConfig with ToLevel {
      val toLevel = Level.Debug
    }
    case object Info extends LevelConfig with ToLevel {
      val toLevel = Level.Info
    }
    case object Warn extends LevelConfig with ToLevel {
      val toLevel = Level.Warn
    }
    case object Error extends LevelConfig with ToLevel {
      val toLevel = Level.Error
    }
    case object Unknown extends LevelConfig
    case object Off extends LevelConfig

    implicit val eq: Eq[LevelConfig] = cats.derived.semiauto.eq
  }

  def console[F[_]: Async](config: Config, initialConfig: RuntimeConfig)(
      implicit eq: Eq[RuntimeConfig]
  ): Resource[F, DynamicOdinConsoleLogger[F]] =
    create(config, initialConfig)(c =>
      consoleLogger(config.formatter, c.minLevel)
    )

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
          config.levelMapping.view.mapValues {
            case _: LevelConfig.Off.type     => Logger.noop
            case _: LevelConfig.Unknown.type => mainLogger
            case level: LevelConfig.ToLevel =>
              mainLogger.withMinimalLevel(level.toLevel)
          }.toList: _*
        ).withFallback(mainLogger)
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
      async <- underlying.withAsync(
        config.asyncTimeWindow,
        config.asyncMaxBufferSize
      )
    } yield new DefaultLogger[F](async.minLevel)
      with DynamicOdinConsoleLogger[F] {
      override def submit(msg: LoggerMessage): F[Unit] = async.log(msg)

      override def update(config: RuntimeConfig): F[Boolean] =
        underlying.update(config)
      override def getConfig: F[RuntimeConfig] = underlying.getConfig

      override def withMinimalLevel(level: Level): Logger[F] =
        async.withMinimalLevel(level)
    }
  }
}
