package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._

/**
 * Northeastern University
 * CSYE 7200 - Big Data System Engineering Using Scala
 * Project: World Earthquake Forecaster
 *
 * @author Patrick Waddell [001058235]
 * @author Rajendra kumar Rajkumar [001405755]
 */

object ForecastForm {

  /**
   * A form processing DTO that maps to the form below.
   *
   * Using a class specifically for form binding reduces the chances
   * of a parameter tampering attack and makes code clearer.
   */
  case class ForecastData(latitude: Double, longitude: Double, radius: Double, magnitude: Double, years: Int)

  /**
   * The form definition for the "create a analysis" form.
   * It specifies the form fields and their types,
   * as well as how to convert from a Data to form data and vice versa.
   */
  val form = Form(
    mapping(
      "latitude" -> of[Double],
      "longitude" -> of[Double],
      "radius" -> of[Double],
      "magnitude" -> of[Double],
      "years" -> of[Int]
    )(ForecastData.apply)(ForecastData.unapply)
  )

  def validateForm(form: Form[ForecastData]) = {
    val data: ForecastData = form.value.get
    if (data.latitude < -90.0 || data.latitude > 90.0) {
      form.withError("latitude", "latitude value error")
    } else if (data.longitude < -180.0 || data.longitude > 180.0) {
      form.withError("longitude", "longitude value error")
    } else if (data.radius <= 0.0) {
      form.withError("radius", "radius value error")
    } else if (data.magnitude < 2.5) {
      form.withError("magnitude", "magnitude value error")
    } else if (data.years < 1) {
      form.withError("years", "years value error")
    } else {
      form
    }
  }
}
