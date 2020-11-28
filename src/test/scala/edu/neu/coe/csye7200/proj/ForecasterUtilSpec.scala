package edu.neu.coe.csye7200.proj

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.scalatest.{FlatSpec, Matchers}

import scala.io.{Codec, Source}
import scala.util.{Success, Try}

/**
 * Northeastern University
 * CSYE 7200 - Big Data System Engineering Using Scala
 * Project: World Earthquake Forecaster
 *
 * @author Patrick Waddell [001058235]
 * @author Rajendra kumar Rajkumar [001405755]
 */
class ForecasterUtilSpec extends FlatSpec with Matchers {
  val spark = SparkSession.builder()
    .appName("MultipleLinearRegression")
    .master("local[*]")
    .getOrCreate()
  val sc = spark.sparkContext

  behavior of "USGeoSurvey.getEarthquakes"
  it should "work for the Oct2020 test data" in {
    implicit val codec = Codec.UTF8
    val parser = new DataParse[USGeoSurvey]()
    val source = Source.fromResource("USGS-Oct2020.csv")
    val testdata = sc.parallelize(parser(source))
    val q = ForecasterUtil.getEarthquakes(testdata)
    q.collect.size shouldBe 10113
    source.close()
  }

  behavior of "USGeoSurvey.getDateRange"
  it should "return results only from Oct. 31st" in {
    implicit val codec = Codec.UTF8
    val parser = new DataParse[USGeoSurvey]()
    val source = Source.fromResource("USGS-Oct2020.csv")
    val testdata = sc.parallelize(parser(source))
    val q = ForecasterUtil.getEarthquakes(testdata)
    val qr = ForecasterUtil.getDateRange(q, DateTime("2020-10-31T00:00:00.000Z"), DateTime("2020-10-31T23:59:59.000Z"))
    qr.collect.size shouldBe 208
    source.close()
  }

  behavior of "USGeoSurvey.getLocationArea"
  it should "return results only from October 19th in Alaska" in {
    // There was a major, 7.6 magnitude, earthquake in the Alaska Peninsula on Oct. 19th
    // as well as many aftershocks afterward
    implicit val codec = Codec.UTF8
    val parser = new DataParse[USGeoSurvey]()
    val source = Source.fromResource("USGS-Oct2020.csv")
    val testdata = sc.parallelize(parser(source))
    val q = ForecasterUtil.getEarthquakes(testdata)
    val qr = ForecasterUtil.getDateRange(q, DateTime("2020-10-19T00:00:00.000Z"), DateTime("2020-10-26T23:59:59.000Z"))
    val qrl = ForecasterUtil.getLocationArea(qr, Location(54.662,-159.675, "Alaska Peninsula"), 50.0)
    qrl.collect().size shouldBe 658
    source.close()
  }

  behavior of "USGeoSurvey.sortByMagnitude"
  it should "return largest earthquake in Alaska during Oct. 2020" in {

    // There was a major, 7.6 magnitude, earthquake in the Alaska Peninsula on Oct. 19th
    // as well as many aftershocks afterward
    implicit val codec = Codec.UTF8
    val parser = new DataParse[USGeoSurvey]()
    val source = Source.fromResource("USGS-Oct2020.csv")
    val testdata = sc.parallelize(parser(source))
    val q = ForecasterUtil.getEarthquakes(testdata)
    val qr = ForecasterUtil.getDateRange(q, DateTime("2020-10-01T00:00:00.000Z"), DateTime("2020-10-31T23:59:59.000Z"))
    val qrl = ForecasterUtil.getLocationArea(qr, Location(54.662, -159.675, "Alaska Peninsula"), 50.0)
    val qrls = ForecasterUtil.sortByMagnitude(qrl)
    qrls.collect().toList(0).magnitude.magnitude shouldEqual 7.6
    source.close()
  }

  behavior of "USGeoSurvey.getEarthquakeHotspots"
  it should "return the top 10 hot spots from Oct. 2020" in {
    implicit val codec = Codec.UTF8
    val parser = new DataParse[USGeoSurvey]()
    val source = Source.fromResource("USGS-Oct2020.csv")
    val testdata = sc.parallelize(parser(source))
    val q = ForecasterUtil.getEarthquakes(testdata)
    val t10 = ForecasterUtil.getEarthquakeHotspots(q, 10)
    t10.size shouldBe 10
    t10(0)._1 shouldBe "Sand Point"
    t10(0)._2.size shouldBe 1262
    t10(1)._1 shouldBe "Westmorland"
    t10(1)._2.size shouldBe 1157
    t10(2)._1 shouldBe "Mina"
    t10(2)._2.size shouldBe 585
    t10(3)._1 shouldBe "Pāhala"
    t10(3)._2.size shouldBe 310
    t10(4)._1 shouldBe "Volcano"
    t10(4)._2.size shouldBe 231
    t10(5)._1 shouldBe "Searles Valley"
    t10(5)._2.size shouldBe 184
    t10(6)._1 shouldBe "Stanley"
    t10(6)._2.size shouldBe 175
    t10(7)._1 shouldBe "Adak"
    t10(7)._2.size shouldBe 165
    t10(8)._1 shouldBe "Denali National Park"
    t10(8)._2.size shouldBe 160
    t10(9)._1 shouldBe "Petersville"
    t10(9)._2.size shouldBe 116
    source.close()
  }

}
