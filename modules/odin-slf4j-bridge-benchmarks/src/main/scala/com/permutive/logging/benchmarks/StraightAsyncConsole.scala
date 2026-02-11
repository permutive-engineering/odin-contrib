/*
 * Copyright 2022-2026 Permutive Ltd. <https://permutive.com>
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

package com.permutive.logging.benchmarks

import cats.effect.IO
import cats.effect.kernel.Async
import cats.effect.unsafe.implicits.global

import io.odin.Level
import io.odin.consoleLogger
import io.odin.formatter.Formatter
import io.odin.loggers.AsyncLogger
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
class StraightAsyncConsole {

  implicit val F: Async[IO] = IO.asyncForIO

  val logger = AsyncLogger
    .withAsync(consoleLogger[IO](minLevel = Level.Info, formatter = Formatter.default), None)
    .allocated
    .unsafeRunSync()
    ._1

  @Benchmark
  def single(): Unit =
    logger.info("single").unsafeRunSync()

}
