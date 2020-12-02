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
   * Method to convert a Sequence of Try of X to a Try of Sequence of X
   *
   * @param xys the sequence of try of X
   * @tparam X the class to convert
   * @return a try of sequnce of X
   */
  def sequence[X](xys: Seq[Try[X]]): Try[Seq[X]] = (Try(Seq[X]()) /: xys) {
    (xsy, xy) => for (xs: Seq[X] <- xsy; x <- xy) yield xs :+ x
  }

  def flatten[X](xfy: Try[Future[X]]): Future[X] =
    xfy match {
      case Success(xf) => xf
      case Failure(e) => Future.failed(e)
    }
}
