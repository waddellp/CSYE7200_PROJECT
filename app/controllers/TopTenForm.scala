package controllers

import java.util.{Calendar, Date}
import controllers.LookupForm.LookupData
import play.api.data.Form
import play.api.data.Forms._

/**
 * Northeastern University
 * CSYE 7200 - Big Data System Engineering Using Scala
 * Project: World Earthquake Forecaster
 *
 * @author Patrick Waddell [001058235]
 * @author Rajendra kumar Rajkumar [001405755]
 */

object TopTenForm {

  /**
   * A form processing DTO that maps to the form below.
   *
   * Using a class specifically for form binding reduces the chances
   * of a parameter tampering attack and makes code clearer.
   */
  case class TopTenData(startDate: Date, endDate: Date)

  /**
   * The form definition for the "create a top ten lookup" form.
   * It specifies the form fields and their types,
   * as well as how to convert from a Data to form data and vice versa.
   */
  val form = Form(
    mapping(
      "startDate" -> date("yyyy-MM-dd"),
      "endDate" -> date("yyyy-MM-dd")
    )(TopTenData.apply)(TopTenData.unapply)
  )

  def validateForm(form: Form[TopTenData]) = {
    val data: TopTenData = form.value.get
    // Create calendar to validate start date
    // Data begins at 1/1/2010
    val c: Calendar = Calendar.getInstance()
    c.set(Calendar.MONTH, 11)
    c.set(Calendar.DATE, 31)
    c.set(Calendar.YEAR, 2009)
    if (data.startDate.after(data.endDate)) {
      form.withError("startDate", "start/end date error")
    } else if (data.startDate.before(c.getTime)) {
      form.withError("startDate", "start date must be after 1/1/2010")
    } else {
      form
    }
  }
}
