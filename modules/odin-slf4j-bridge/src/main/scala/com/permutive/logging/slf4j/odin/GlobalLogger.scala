/*
 * Copyright 2022-2025 Permutive Ltd. <https://permutive.com>
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

import java.util.concurrent.atomic.AtomicReference

import scala.util.control.NonFatal

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.kernel.Sync
import cats.effect.std.Dispatcher
import cats.effect.syntax.all._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._

import io.odin.Level
import io.odin.Logger
import io.odin.formatter.Formatter

/** Utilities for getting a logger that can be used by SLF4J. */
@SuppressWarnings(Array("scalafix:DisableSyntax.null", "scalafix:DisableSyntax.==", "scalafix:DisableSyntax.!="))
object GlobalLogger {

  private val default: OdinTranslator =
    OdinTranslator.unsafeConsole(level = Level.Info, Formatter.default)

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
        default.run(
          this.getClass.getName,
          Level.Error,
          "Failed to get logger from atomic ref",
          Some(th)
        )
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
    ).raiseError[F, Unit].toResource

    Resource.eval(Sync[F].delay(ref.get())).map(_ == null).ifM(set, error)
  }

}
