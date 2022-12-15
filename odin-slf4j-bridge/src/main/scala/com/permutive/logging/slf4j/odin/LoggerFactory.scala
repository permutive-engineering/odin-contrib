package com.permutive.logging.slf4j.odin

import org.slf4j.{ILoggerFactory, Logger}

class LoggerFactory extends ILoggerFactory {
  def getLogger(name: String): Logger = new LoggerAdapter(name)
}
