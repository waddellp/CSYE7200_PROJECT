package edu.neu.coe.csye7200.proj

import org.scalatest.{FlatSpec, Matchers}

import scala.io.{Codec, Source}
import scala.util.Success

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

  behavior of "Location"
  /**
   * Test successful parsing of the Location data (latitude/longitude/place)
   */
  it should "work for lat/long/place" in {
    val x = Location(List("-89.0", "179.0", "Antarctica"))
    x should matchPattern {
      case Location(-89.0, 179.0, "Antarctica") =>
    }
  }
  /**
   * Test failures for parsing of the Location data
   */
  it should "not work for invalid latitude" in {
    assertThrows[Exception] { // Expect Exception
      Location(List("bad_latitude", "179.0", "Antarctica"))
    }
  }
  it should "not work for invalid longitude" in {
    assertThrows[Exception] { // Expect Exception
      Location(List("-89.0", "bad_longitude", "Antarctica"))
    }
  }

  behavior of "Magnitude"
  /**
   * Test successful parsing of the Magnitude data (magnitude, units, depth)
   */
  it should "work for magnitude/unit/depth" in {
    val x = Magnitude(List("1.66", "ml", "16.67"))
    x should matchPattern {
      case Magnitude(1.66, "ml", 16.67) =>
    }
  }
  /**
   * Test failures for parsing of the Magnitude data
   */
  it should "not work for invalid magnitude" in {
    assertThrows[Exception] { // Expect Exception
      Magnitude(List("bad_magnitude", "ml", "16.67"))
    }
  }
  it should "not work for invalid depth [km]" in {
    assertThrows[Exception] { // Expect Exception
      Magnitude(List("1.66", "ml", "bad_depth"))
    }
  }

  behavior of "USGeoSurvey.getEarthquakes"
  it should "work for the Oct2020 test data" in {
    val parser = new DataParse[USGeoSurvey]()
    implicit val codec = Codec.UTF8
    val source = Source.fromResource("USGS-Oct2020.csv")
    val msy = USGeoSurvey.getEarthquakes(parser(source))
    msy should matchPattern { case Success(_) => }
    msy.get.size shouldBe 10113
    source.close()
  }

}
