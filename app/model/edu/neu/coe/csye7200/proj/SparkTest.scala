package model.edu.neu.coe.csye7200.proj

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/**
 * Northeastern University
 * CSYE 7200 - Big Data System Engineering Using Scala
 * Project: World Earthquake Forecaster
 * @author Patrick Waddell [001058235]
 * @author Rajendra kumar Rajkumar [001405755]
 */

/**
 * This object is a test of the ForecasterUtil class with Spark and RDDs
 */
object SparkTest extends App {

  val log = Logger.getLogger(getClass.getName)
  log.setLevel(Level.ERROR)

  val spark = SparkSession
    .builder()
    .appName("MultipleLinearRegression")
    .master("local[*]")
    .getOrCreate()
  val sc: SparkContext = spark.sparkContext

  val data: RDD[USGeoSurvey] = ForecasterUtil.loadData(sc)
  val e = ForecasterUtil.getEarthquakes(data)
  val er = ForecasterUtil.getDateRange(e, DateTime("2020-10-19T00:00:00.000Z"), DateTime("2020-10-26T23:59:59.000Z"))
  val erl = ForecasterUtil.getLocationArea(er, Location(54.662, -159.675, "Alaska Peninsula"), 50.0)
  val erls = ForecasterUtil.sortByMagnitude(erl)

  println(erls.collect().toSeq.take(1).toString())

  // For implicit conversions like converting RDDs to DataFrames
  //import spark.implicits._
  //data.toDF()

  spark.stop()
}
