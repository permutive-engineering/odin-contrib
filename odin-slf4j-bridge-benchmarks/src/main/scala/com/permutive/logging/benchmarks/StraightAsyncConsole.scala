package com.permutive.logging.benchmarks

import cats.effect.IO
import cats.effect.kernel.Async
import cats.effect.unsafe.implicits.global
import io.odin.formatter.Formatter
import io.odin.loggers.AsyncLogger
import io.odin.{consoleLogger, Level}
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}

import scala.concurrent.duration._

@State(Scope.Benchmark)
class StraightAsyncConsole {
  implicit val F: Async[IO] = IO.asyncForIO

  val logger = AsyncLogger
    .withAsync(
      consoleLogger[IO](minLevel = Level.Info, formatter = Formatter.default),
      10.millis,
      None
    )
    .allocated
    .unsafeRunSync()
    ._1

  @Benchmark
  def single(): Unit =
    logger.info("single").unsafeRunSync()
}
