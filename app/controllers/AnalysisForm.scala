package controllers

import java.util.Date

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

object AnalysisForm {

  /**
   * A form processing DTO that maps to the form below.
   *
   * Using a class specifically for form binding reduces the chances
   * of a parameter tampering attack and makes code clearer.
   */
  case class AnalysisData(latitude: Double, longitude: Double)

  /**
   * The form definition for the "create a analysis" form.
   * It specifies the form fields and their types,
   * as well as how to convert from a Data to form data and vice versa.
   */
  val form = Form(
    mapping(
      "latitude" -> of[Double],
      "longitude" -> of[Double]
    )(AnalysisData.apply)(AnalysisData.unapply)
  )

  def validateForm(form: Form[AnalysisData]) = {
    val data: AnalysisData = form.value.get
    if (data.latitude < -90.0 || data.latitude > 90.0) {
      form.withError("latitude", "latitude value error")
    } else {
      form
    }
  }
}
