package com.permutive.logging.benchmarks

import scala.concurrent.duration._

import cats.effect.IO
import cats.effect.kernel.Async
import cats.effect.unsafe.implicits.global

import com.permutive.logging.slf4j.odin.GlobalLogger
import io.odin.Level
import io.odin.consoleLogger
import io.odin.formatter.Formatter
import io.odin.loggers.AsyncLogger
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@State(Scope.Benchmark)
class OdinSlf4jAsyncConsole {

  implicit val F: Async[IO] = IO.asyncForIO

  AsyncLogger
    .withAsync(consoleLogger[IO](minLevel = Level.Info, formatter = Formatter.colorful), 10.millis, None)
    .flatMap(GlobalLogger.setLogger[IO])
    .allocated
    .unsafeRunSync(): Unit

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  @Benchmark
  def single(): Unit =
    logger.info("single")

}
