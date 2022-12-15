package com.permutive.logging.benchmarks

import cats.effect.IO
import cats.effect.kernel.Async
import cats.effect.unsafe.implicits.global
import com.permutive.logging.slf4j.odin.GlobalLogger
import io.odin.formatter.Formatter
import io.odin.loggers.AsyncLogger
import io.odin.{consoleLogger, Level}
import org.openjdk.jmh.annotations.{Benchmark, Scope, State}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._

@State(Scope.Benchmark)
class OdinSlf4jAsyncConsole {
  implicit val F: Async[IO] = IO.asyncForIO

  AsyncLogger
    .withAsync(consoleLogger[IO](minLevel = Level.Info, formatter = Formatter.colorful), 10.millis, None)
    .flatMap(GlobalLogger.setLogger[IO])
    .allocated
    .unsafeRunSync()

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  @Benchmark
  def single(): Unit =
    logger.info("single")
}
