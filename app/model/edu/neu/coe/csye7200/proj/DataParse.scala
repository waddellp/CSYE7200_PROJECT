package model.edu.neu.coe.csye7200.proj

import scala.io.Source
import scala.util.Try

/**
 * Northeastern University
 * CSYE 7200 - Big Data System Engineering Using Scala
 * Project: World Earthquake Forecaster
 * @author Patrick Waddell [001058235]
 * @author Rajendra kumar Rajkumar [001405755]
 */
class DataParse [T: Parsible] extends (Source => Seq[T]) {
  def apply(source: Source): Seq[T] =
    source.getLines.toSeq.drop(1).map(e => implicitly[Parsible[T]].fromString(e)) flatMap (_.toOption)

  def apply(line: String): Try[T] =
    implicitly[Parsible[T]].fromString(line)
}

trait Parsible[X] {
  def fromString(w: String): Try[X]
}

