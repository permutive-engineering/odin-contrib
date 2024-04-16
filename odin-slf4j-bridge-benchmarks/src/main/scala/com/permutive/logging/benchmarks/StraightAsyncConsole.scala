package com.permutive.logging.benchmarks

import scala.concurrent.duration._

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
