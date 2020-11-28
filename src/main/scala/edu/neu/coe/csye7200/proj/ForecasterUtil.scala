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
  def getEarthquakes(seismicEvents: RDD[USGeoSurvey]): RDD[USGeoSurvey] = {
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
  def getDateRange(earthquakes: RDD[USGeoSurvey], start: DateTime, end: DateTime): RDD[USGeoSurvey] = {
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
  def getLocationArea(earthquakes: RDD[USGeoSurvey], location: Location, radius: Double): RDD[USGeoSurvey] = {
    earthquakes filter( u=> u.location.distance(location) <= radius)
  }


  /**
   * Method to sort the US Geological Survey data by magnitude
   * @param earthquakes the US Geological Survey data earthquake list
   * @return USGeoSurvey data sorted by magnitude
   */
  def sortByMagnitude(earthquakes: RDD[USGeoSurvey]): RDD[USGeoSurvey] = {
    earthquakes sortBy(- _.magnitude.magnitude)
  }

  /**
   * Method to find the top earthquake hotspots around the world
   * @params earthquakes the US Geological Survey data earthquake list
   * @return a sequence of tuples containing: the place of the hotspot, all it's surrounding activity
   */
  def getEarthquakeHotspots(earthquakes: RDD[USGeoSurvey], numHotspots: Int): Seq[(String, Iterable[USGeoSurvey])] = {
    earthquakes.groupBy(_.location.place).collect().sortBy(- _._2.size).toSeq.take(numHotspots)
  }
}
