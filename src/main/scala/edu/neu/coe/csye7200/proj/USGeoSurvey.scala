package edu.neu.coe.csye7200.proj

import scala.collection.mutable
import scala.util.Try

/**
 * Northeastern University
 * CSYE 7200 - Big Data System Engineering Using Scala
 * Project: World Earthquake Forecaster
 * @author Patrick Waddell [001058235]
 * @author Rajendra kumar Rajkumar [001405755]
 */

/**
 * Case class - US Geological Survey information that makes up a seismic event
 * @param id the unique identifier of the seismic event
 * @param datetime the date and time of the seismic event in UTC (Zulu time)
 * @param location the location of the seismic event
 * @param magnitude the magnitude of the seismic event
 * @param eventtype the type of seismic event (only 'earthquake' used by this tool)
 */
case class USGeoSurvey(id: String, datetime: DateTime, location: Location, magnitude: Magnitude, eventtype: String) {
  def isEarthquake = eventtype equals "earthquake"
}

/**
 * Case class - location information: Latitude, Longitude and a description of the location
 * @param latitude the latitude of the location
 * @param longitude the longitude of the location
 * @param place the description of the location
 */
case class Location(latitude: Double, longitude: Double, place: String) {
  override def toString = {
    s"$latitude,$longitude,$place"
  }
}

object Location {
  def apply(params: List[String]): Location = params match {
    case latitude :: longitude :: place :: Nil => apply(latitude.toDouble, longitude.toDouble, place)
    case _ => throw new Exception(s"Parse error in location data: $params")
  }
}

case class DateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) {
  override def toString = { s"$year%4d-$month%2d-$day%2d $hour%2d:$minute%2d:$second%dZ" }
}

object DateTime {
  // this regex will not parse the UTC (Zulu time) in the dataset
  val rDateTime = """^[1-2]{1}\d{3}-[0-1]{1}\d{1}-[0-3]{1}\d{1}T[0-1]{1}\d{1}:[0-5]{1}\d{1}:[0-5]{1}\d{1}\.\d{3}Z$""".r

  def apply(datetime: String): DateTime = datetime match {
    case rDateTime(year, month, day, hour, minute, second) => apply(year.toInt, month.toInt, day.toInt, hour.toInt, minute.toInt, second.toInt)
    case _ => throw new Exception(s"Parse error in UTC date time: $datetime")
  }
}

/**
 * Case class - US Geological Survey seismic data measuring magnitude of the event
 * @param magnitude the magnitude of the seismic event
 * @param units the unit of measurement for the magnitude
 */
case class Magnitude(magnitude: Double, units: String, depth: Double) {
  s"$magnitude[$units]"
}

object Magnitude {
  def apply(params: List[String]): Magnitude = params match {
    case magnitude :: units :: depth :: Nil => apply(magnitude.toDouble, units, depth.toDouble)
    case _ => throw new Exception(s"Parse error in magnitude: $params")
  }
}

object USGeoSurvey extends App {

  trait ParsibleUSGeoSurvey extends Parsible[USGeoSurvey] {
    def fromString(w: String): Try[USGeoSurvey] = Try {
      apply(w.split(",").toSeq)
    }
  }

  implicit object ParsibleUSGeoSurvey extends ParsibleUSGeoSurvey

  /**
   * Form a list from the elements explicitly specified (by position) from the given list
   *
   * @param list    a list of Strings
   * @param indices a variable number of index values for the desired elements
   * @return a list of Strings containing the specified elements in order
   */
  def elements(list: Seq[String], indices: Int*): List[String] = {
    val x = mutable.ListBuffer[String]()
    for(i <- indices) x += list(i)
    x.toList
  }

  /**
   * Alternative apply method for the US Geological Survey class
   *
   * @param ws a sequence of Strings
   * @return a Movie
   */
  def apply(ws: Seq[String]): USGeoSurvey = {
    val id = ws(11)
    val datetime = DateTime(ws(0))
    val location = Location(elements(ws, 1, 2, 13))
    val magnitude = Magnitude(elements(ws, 4, 5, 3))
    val eventtype = ws(17)
    USGeoSurvey(id, datetime, location, magnitude, eventtype)
  }
}
