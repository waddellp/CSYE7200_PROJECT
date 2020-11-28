package edu.neu.coe.csye7200.proj

import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.io.Source

object SparkTest extends App {

    val log = Logger.getLogger(getClass.getName)
    log.setLevel(Level.ERROR)

    val spark = SparkSession
      .builder()
      .appName("MultipleLinearRegression")
      .master("local[*]")
      .getOrCreate()
    val sc = spark.sparkContext

    val parser = new DataParse[USGeoSurvey]()
    val fileStream = Source.getClass.getResourceAsStream("/USGS-2020.csv")
    val parseData = sc.parallelize(Source.fromInputStream(fileStream).getLines().toSeq map (u => parser(u)))
    val data: RDD[USGeoSurvey] = parseData flatMap(_.toOption)

    val e = ForecasterUtil.getEarthquakes(data)
    val er = ForecasterUtil.getDateRange(e, DateTime("2020-10-19T00:00:00.000Z"), DateTime("2020-10-26T23:59:59.000Z"))
    val erl = ForecasterUtil.getLocationArea(er, Location(54.662,-159.675, "Alaska Peninsula"), 50.0)
    val erls = ForecasterUtil.sortByMagnitude(erl)

    println( erls.collect().toSeq.take(1).toString() )

    // For implicit conversions like converting RDDs to DataFrames
    //import spark.implicits._
    //data.toDF()

    spark.stop()
}
