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

  behavior of "DateTime.less"
  /**
   * Test successful comparison method of the UTC date/time
   */
  it should "work for year date/time less than another date/time" in {
    val after = DateTime("2020-10-31T22:10:35.880Z")
    val before = DateTime("2019-10-31T22:10:35.880Z")
    before.less(after) shouldBe true
    after.less(before) shouldBe false
  }
  it should "work for month date/time less than another date/time" in {
    val after = DateTime("2020-10-31T22:10:35.880Z")
    val before = DateTime("2020-09-31T22:10:35.880Z")
    before.less(after) shouldBe true
    after.less(before) shouldBe false
  }
  it should "work for day date/time less than another date/time" in {
    val after = DateTime("2020-10-31T22:10:35.880Z")
    val before = DateTime("2020-10-30T22:10:35.880Z")
    before.less(after) shouldBe true
    after.less(before) shouldBe false
  }
  it should "work for hour date/time less than another date/time" in {
    val after = DateTime("2020-10-31T22:10:35.880Z")
    val before = DateTime("2020-10-31T21:10:35.880Z")
    before.less(after) shouldBe true
    after.less(before) shouldBe false
  }
  it should "work for minute date/time less than another date/time" in {
    val after = DateTime("2020-10-31T22:10:35.880Z")
    val before = DateTime("2020-10-31T22:09:35.880Z")
    before.less(after) shouldBe true
    after.less(before) shouldBe false
  }
  it should "work for second date/time less than another date/time" in {
    val after = DateTime("2020-10-31T22:10:35.880Z")
    val before = DateTime("2020-10-31T22:10:34.880Z")
    before.less(after) shouldBe true
    after.less(before) shouldBe false
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

  behavior of "Location.distance"
  /**
   * Test distance calculation between two Location (latitude/longitude) points
   */
  it should "work for distance between Boston and NYC" in {
    val boston = Location(42.3584, -71.0598,"Boston, MA")
    val nyc = Location(40.7143, -74.006, "New York City, NY")
    boston.distance(nyc) shouldBe 305.836 +- 0.001 // ~306 kilometers (as the bird flys) between Boston & NYC
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

  behavior of "USGeoSurvey.getQuakes"
  it should "work for the Oct2020 test data" in {
    val parser = new DataParse[USGeoSurvey]()
    implicit val codec = Codec.UTF8
    val source = Source.fromResource("USGS-Oct2020.csv")
    val msy = USGeoSurvey.getQuakes(parser(source))
    msy should matchPattern { case Success(_) => }
    msy.get.size shouldBe 10113
    source.close()
  }

  behavior of "USGeoSurvey.getQuakesDateRange"
  it should "return results only from Oct. 31st" in {
    val parser = new DataParse[USGeoSurvey]()
    implicit val codec = Codec.UTF8
    val source = Source.fromResource("USGS-Oct2020.csv")
    val msy = USGeoSurvey.getQuakesDateRange(parser(source), DateTime("2020-10-31T00:00:00.000Z"), DateTime("2020-10-31T23:59:59.000Z"))
    msy should matchPattern { case Success(_) => }
    msy.get.size shouldBe 208
    source.close()
  }

}
