package model.edu.neu.coe.csye7200.proj

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * Northeastern University
 * CSYE 7200 - Big Data System Engineering Using Scala
 * Project: World Earthquake Forecaster
 *
 * @author Patrick Waddell [001058235]
 * @author Rajendra kumar Rajkumar [001405755]
 */
object Function {
  // Generic function object

  /**
   * Form a list from the elements explicitly specified (by position) from the given list
   *
   * @param list    a list of Strings
   * @param indices a variable number of index values for the desired elements
   * @return a list of Strings containing the specified elements in order
   */
  def elements(list: Seq[String], indices: Int*): List[String] = {
    val x = mutable.ListBuffer[String]()
    for (i <- indices) x += list(i)
    x.toList
  }

  /**
   * Method to return an Option of Double when passed a String
   * @param s the String to convert to Double
   * @return an Option of Double
   */
  def toDouble(s: String):Option[Double] = {
    try {
      Some(s.toDouble)
    } catch {
      case _: NumberFormatException => None
    }
  }
}
