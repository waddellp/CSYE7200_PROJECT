package edu.neu.coe.csye7200.proj

import scala.util.{Try}

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

  /**
   * Method to return the distance between two locations
   * @param location the location from which to find the distance to
   * @return the distance in kilometers
   */
  def distance(location: Location ): Double =  {
    def rad(x: Double) = x * Math.PI / 180.0
    val R = 6371.0; // Earth’s mean radius in kilometers
    val dLat = rad(location.latitude - this.latitude)
    val dLong = rad(location.longitude - this.longitude)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(rad(this.latitude)) * Math.cos(rad(location.latitude)) *
        Math.sin(dLong / 2) * Math.sin(dLong / 2)
    R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  }
}

object Location {
  def apply(params: List[String]): Location = params match {
    case latitude :: longitude :: description :: Nil =>
      apply(latitude.toDouble, longitude.toDouble, description.substring(description.lastIndexOf("of ") + 3))
    case _ => throw new Exception(s"Parse error in location data: $params")
  }
}

/**
 * Case clase - the UTC date/time for US Geological Survey seismic data
 * @param year the year of the seismic event
 * @param month the month of the seismic event
 * @param day the day of the seismic event
 * @param hour the hour of the seismic event
 * @param minute the minute of the seismic event
 * @param second the second of the seismic event
 */
case class DateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) extends Ordered[DateTime] {
  import scala.math.Ordered.orderingToOrdered

  /**
   * Override default toString method
   * @return
   */
  override def toString = { "%1$04d-%2$02d-%3$02d %4$02d:%5$02d:%6$02dZ".format(year,month,day,hour,minute,second) }

  /**
   * Comparison method for DateTime
   * @param that the date/time to compare to
   * @return 0 if equal, -1 if this is less than that, and 1 if this is greater than that
   */
  override def compare(that: DateTime): Int =
    if (this == that) 0
    else if ((year < that.year) ||
          (year == that.year && month < that.month) ||
          (year == that.year && month == that.month && day < that.day) ||
          (year == that.year && month == that.month && day == that.day && hour < that.hour) ||
          (year == that.year && month == that.month && day == that.day && hour == that.hour && minute < that.minute) ||
          (year == that.year && month == that.month && day == that.day && hour == that.hour && minute == that.minute && second < that.second)) -1
    else 1
}

object DateTime {
  // this regex will not parse the UTC (Zulu time) in the dataset
  val rDateTime = """^([1-2]{1}\d{3})-([0-1]{1}\d{1})-([0-3]{1}\d{1})T([0-2]{1}\d{1}):([0-5]{1}\d{1}):([0-5]{1}\d{1})\.\d{3}Z$""".r

  def apply(datetime: String): DateTime = datetime match {
    case rDateTime(year, month, day, hour, minute, second) => apply(year.toInt, month.toInt, day.toInt, hour.toInt, minute.toInt, second.toInt)
    case _ => throw new Exception(s"Parse error in UTC date time: $datetime")
  }
}

/**
 * Case class - The magnitude data associated with the US Geological Survey seismic data
 * @param magnitude the magnitude of the seismic event
 * @param units the unit of measurement for the magnitude
 */
case class Magnitude(magnitude: Double, units: String, depth: Double) {
  override def toString = { s"$magnitude[$units],$depth[km]" }
}

object Magnitude {
  def apply(params: List[String]): Magnitude = params match {
    case magnitude :: units :: depth :: Nil => apply(magnitude.toDouble, units, depth.toDouble)
    case _ => throw new Exception(s"Parse error in magnitude: $params")
  }
}

object USGeoSurvey extends App with Serializable {

  trait ParsibleUSGeoSurvey extends Parsible[USGeoSurvey] {
    def fromString(w: String): Try[USGeoSurvey] = Try {
      apply(w.split(",").toSeq)
    }
  }

  implicit object ParsibleUSGeoSurvey extends ParsibleUSGeoSurvey

  /**
   * Alternative apply method for the US Geological Survey class
   *
   * @param ws a sequence of Strings
   * @return a Movie
   */
  def apply(ws: Seq[String]): USGeoSurvey = {
    val id = ws(11)
    val datetime = DateTime(ws(0))
    val location = Location(Function.elements(ws, 1, 2, 13))
    val magnitude = Magnitude(Function.elements(ws, 4, 5, 3))
    val eventtype = ws(15)
    USGeoSurvey(id, datetime, location, magnitude, eventtype)
  }
}
