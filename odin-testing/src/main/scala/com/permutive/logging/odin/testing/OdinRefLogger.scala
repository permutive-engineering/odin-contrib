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
