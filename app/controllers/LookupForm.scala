package controllers

import java.util.Date

import play.api.data.Forms._
import play.api.data.Form
import play.api.data.format.Formats._
import play.api.libs.json.JsNull.validate

/**
 * Northeastern University
 * CSYE 7200 - Big Data System Engineering Using Scala
 * Project: World Earthquake Forecaster
 *
 * @author Patrick Waddell [001058235]
 * @author Rajendra kumar Rajkumar [001405755]
 */

object LookupForm {

  /**
   * A form processing DTO that maps to the form below.
   *
   * Using a class specifically for form binding reduces the chances
   * of a parameter tampering attack and makes code clearer.
   */
  case class LookupData(latitude: Double, longitude: Double, radius: Double, startDate: Date, endDate: Date)

  /**
   * The form definition for the "create a historical lookup" form.
   * It specifies the form fields and their types,
   * as well as how to convert from a Data to form data and vice versa.
   */
  val form = Form(
    mapping(
      "latitude" -> of[Double],
      "longitude" -> of[Double],
      "radius" -> of[Double],
      "startDate" -> date("yyyy-MM-dd"),
      "endDate" -> date("yyyy-MM-dd")
    )(LookupData.apply)(LookupData.unapply)
  )

  def validateForm(form: Form[LookupData]) = {
    val data: LookupData = form.value.get
    if (data.latitude < -90.0 || data.latitude > 90.0) {
      form.withError("latitude", "latitude value error")
    } else if (data.longitude < -180.0 || data.longitude > 180.0) {
      form.withError("longitude", "longitude value error")
    } else if (data.radius <= 0.0) {
      form.withError("radius", "radius value error")
    } else if (data.startDate.after(data.endDate)) {
      form.withError("startDate", "start/end date error")
    } else {
      form
    }
  }


}
