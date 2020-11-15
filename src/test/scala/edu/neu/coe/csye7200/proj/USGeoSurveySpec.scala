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
  /**
   * Test successful parsing of the UTC date/time
   */
  it should "work for valid UTC date/time" in {
    val x = DateTime("2020-10-31T22:10:35.880Z")
    x should matchPattern {
      case DateTime(2020, 10, 31, 22, 10, 35) =>
    }
  }
  /**
   * Test failures for parsing of the UTC date/time
   */
  it should "not work for invalid year" in {
    assertThrows[Exception] { // Expect Exception
      DateTime("3020-10-31T22:10:35.880Z")
    }
  }
  it should "not work for invalid month" in {
    assertThrows[Exception] { // Expect Exception
      DateTime("2020-20-31T22:10:35.880Z")
    }
  }
  it should "not work for invalid day" in {
    assertThrows[Exception] { // Expect Exception
      DateTime("2020-10-41T22:10:35.880Z")
    }
  }
  it should "not work for invalid hour" in {
    assertThrows[Exception] { // Expect Exception
      DateTime("2020-10-31T32:10:35.880Z")
    }
  }
  it should "not work for invalid minute" in {
    assertThrows[Exception] { // Expect Exception
      DateTime("2020-10-31T22:60:35.880Z")
    }
  }
  it should "not work for invalid second" in {
    assertThrows[Exception] { // Expect Exception
      DateTime("2020-10-31T22:10:60.880Z")
    }
  }
}
