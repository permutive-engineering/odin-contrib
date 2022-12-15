package com.permutive.logging.benchmarks

import org.openjdk.jmh.annotations.{Benchmark, Scope, State}
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@State(Scope.Benchmark)
class OdinSlf4jConsole {
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  @Benchmark
  def single(): Unit =
    logger.info("single")
}
