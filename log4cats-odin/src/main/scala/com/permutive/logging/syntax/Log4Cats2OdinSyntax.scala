package com.permutive.logging.syntax

import cats.Applicative
import com.permutive.logging.Log4Cats2Odin
import io.odin.{Level, Logger}
import org.typelevel.log4cats.SelfAwareStructuredLogger

trait Log4Cats2OdinSyntax {
  implicit class ToOdinLogger[F[_]](logger: SelfAwareStructuredLogger[F]) {
    def toOdin(minLevel: Level = Level.Info)(implicit F: Applicative[F]): Logger[F] =
      Log4Cats2Odin.convert(logger, minLevel)
  }
}
