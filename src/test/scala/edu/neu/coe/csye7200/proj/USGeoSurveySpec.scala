package edu.neu.coe.csye7200.proj

import org.scalatest.{FlatSpec, Matchers}

/**
 * Northeastern University
 * CSYE 7200 - Big Data System Engineering Using Scala
 * Project: World Earthquake Forecaster
 * @author Patrick Waddell [001058235]
 * @author Rajendra kumar Rajkumar [001405755]
 */
/**
 * Created by scalaprof on 9/13/16.
 */
class USGeoSurveySpec extends FlatSpec with Matchers {

  behavior of "DateTime"
  it should "work for params" in {
    val x = DateTime("2020-10-31T22:10:35.880Z")
    x should matchPattern {
      case DateTime(2020, 10, 31, 22, 10, 35) =>
    }
  }
}
