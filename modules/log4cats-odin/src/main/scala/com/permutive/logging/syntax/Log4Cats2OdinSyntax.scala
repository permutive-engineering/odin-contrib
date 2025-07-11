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

package com.permutive.logging.syntax

import cats.Applicative

import com.permutive.logging.Log4Cats2Odin
import io.odin.Level
import io.odin.Logger
import org.typelevel.log4cats.SelfAwareStructuredLogger

trait Log4Cats2OdinSyntax {

  implicit class ToOdinLogger[F[_]](logger: SelfAwareStructuredLogger[F]) {

    def toOdin(minLevel: Level = Level.Info)(implicit
        F: Applicative[F]
    ): Logger[F] =
      Log4Cats2Odin.convert(logger, minLevel)

  }

}
