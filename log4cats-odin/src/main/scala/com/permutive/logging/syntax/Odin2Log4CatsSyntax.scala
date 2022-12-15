package com.permutive.logging.syntax

import cats.Applicative
import com.permutive.logging.Odin2Log4Cats
import io.odin.Logger
import io.odin.meta.Position
import org.typelevel.log4cats.SelfAwareStructuredLogger

trait Odin2Log4CatsSyntax {
  implicit class ToLog4CatsLogger[F[_]](logger: Logger[F]) {
    def toLog4Cats(implicit F: Applicative[F], position: Position): SelfAwareStructuredLogger[F] =
      Odin2Log4Cats.convert(logger)
  }
}
