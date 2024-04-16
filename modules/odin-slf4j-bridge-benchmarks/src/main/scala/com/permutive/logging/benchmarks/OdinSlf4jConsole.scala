package com.permutive.logging.benchmarks

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@State(Scope.Benchmark)
class OdinSlf4jConsole {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  @Benchmark
  def single(): Unit =
    logger.info("single")

}
