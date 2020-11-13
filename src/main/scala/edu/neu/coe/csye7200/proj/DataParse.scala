package edu.neu.coe.csye7200.proj;

import scala.io.Source
import scala.util.Try

/**
 * Northeastern University
 * CSYE 7200 - Big Data System Engineering Using Scala
 * Project: World Earthquake Forecaster
 * @author Patrick Waddell [001058235]
 * @author Rajendra kumar Rajkumar [001405755]
 */

class DataParse1 [T: Parsible] extends (Source => Iterator[Try[T]]) {
  def apply(source: Source): Iterator[Try[T]] =
    source.getLines.toSeq.drop(1).map(e => implicitly[Parsible[T]].fromString(e)).iterator
}

trait Parsible[X] {
  def fromString(w: String): Try[X]
}

