# Odin-Contrib

This repository contains extra utilities that work with the [Odin] logging library.

## [`odin-testing`]

An [Odin] logger implementation that captures all log entries in a concurrent `Ref`. This is especially useful for
testing.

## [`log4cats-odin`]

Provides translation and implicit syntax between [Odin] and [Log4Cats] loggers. This allows libraries and applications
that use both logger types to convert between each.

Note that due to the use of [sourcecode] in Odin, the location of logging messages may appear to be from the point at
which the [Odin] logger was converted to a [Log4Cats] logger, so in some cases it may simply be better to span an
[Slf4J] logger where needed.

## [`odin-dynamic`]

An [Odin] logger implementation that allows runtime reconfiguration of log levels, both for the entire application, but
also certain packages or classes.

## [`odin-slf4j-bridge`]

A bridge between [Odin] and [Slf4j] allowing the dynamic [Odin] logger to be used by default for [Slf4j] log messages.

In benchmarks, the bridge performs as well as [Log4Cats] with a [Logback] backend.

[`odin-testing`]: odin-testing
[`log4cats-odin`]: log4cats-odin
[`odin-dynamic`]: odin-dynamic
[`odin-slf4j-bridge`]: odin-slf4j-bridge

[Log4Cats]: https://github.com/typelevel/log4cats
[Logback]: http://logback.qos.ch/
[Odin]: https://github.com/valskalla/odin
[Slf4J]: http://www.slf4j.org/
[sourcecode]: https://github.com/com-lihaoyi/sourcecode
