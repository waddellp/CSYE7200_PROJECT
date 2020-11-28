package edu.neu.coe.csye7200.proj

import org.apache.spark.rdd.RDD

import scala.util.Try

object ForecasterUtil {

  /**
   * Method to get a list of US Geological Survey data that is only of type 'earthquake'
   *
   * @param seismicEvents the US Geological Survey data to use
   * @return a try of RDD of USGeoSurvey data
   */
  def getEarthquakes(seismicEvents: Seq[USGeoSurvey]): Seq[USGeoSurvey] = {
    seismicEvents filter( u => u.isEarthquake)
  }

  /**
   * Method to get a list of US Geological Survey data that is only of type 'earthquake' and falls between a
   * date/time range
   *
   * @param earthquakes the US Geological Survey data earthquake list
   * @param start the start of the date/time range to get
   * @param end the end of the date/time range to get
   * @return a try of sequence of USGeoSurvey data
   */
  def getDateRange(earthquakes: Seq[USGeoSurvey], start: DateTime, end: DateTime): Seq[USGeoSurvey] = {
    earthquakes filter(u => (u.datetime <= end) && (start <= u.datetime))
  }

  /**
   * Method to get a list of US Geological Survey data that is only of type 'earthquake' and
   * is within the area around a location
   *
   * @param earthquakes the US Geological Survey data earthquake list
   * @param location the location from which to search around
   * @param radius the radius around the location to search within
   * @return a try of sequence of USGeoSurvey data
   */
  def getLocationArea(earthquakes: Seq[USGeoSurvey], location: Location, radius: Double): Seq[USGeoSurvey] = {
    earthquakes filter( u=> u.location.distance(location) <= radius)
  }

  /**
   * Method to sort the US Geological Survey data by magnitude
   * @param earthquakes the US Geological Survey data earthquake list
   * @return USGeoSurvey data sorted by magnitude
   */
  def sortByMagnitude(earthquakes: Seq[USGeoSurvey]): Seq[USGeoSurvey] = {
    earthquakes sortBy(- _.magnitude.magnitude)
  }

  /**
   * Method to find the top earthquake hotspots around the world
   * @params earthquakes the US Geological Survey data earthquake list
   * @return a sequence of tuples containing: the place of the hotspot, all it's surrounding activity
   */
  def getEarthquakeHotspots(earthquakes: Seq[USGeoSurvey]): Seq[(String, Seq[USGeoSurvey])] = {
    earthquakes.groupBy(_.location.place).toSeq.sortBy(- _._2.size)
  }
}
