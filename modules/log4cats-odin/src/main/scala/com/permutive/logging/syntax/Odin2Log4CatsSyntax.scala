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

package com.permutive.logging.syntax

import cats.Applicative

import com.permutive.logging.Odin2Log4Cats
import io.odin.Logger
import io.odin.meta.Position
import org.typelevel.log4cats.SelfAwareStructuredLogger

trait Odin2Log4CatsSyntax {

  implicit class ToLog4CatsLogger[F[_]](logger: Logger[F]) {

    def toLog4Cats(implicit
        F: Applicative[F],
        position: Position
    ): SelfAwareStructuredLogger[F] =
      Odin2Log4Cats.convert(logger)

  }

}
