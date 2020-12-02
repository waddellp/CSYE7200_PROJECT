package controllers

object LookupForm {
  import play.api.data.Forms._
  import play.api.data.Form
  import play.api.data.format.Formats._

  /**
   * A form processing DTO that maps to the form below.
   *
   * Using a class specifically for form binding reduces the chances
   * of a parameter tampering attack and makes code clearer.
   */
  case class Data(latitude: Double, longitude: Double, radius: Double)

  /**
   * The form definition for the "create a historical lookup" form.
   * It specifies the form fields and their types,
   * as well as how to convert from a Data to form data and vice versa.
   */
  val form = Form(
    mapping(
      "latitude" -> of[Double],
      "longitude" -> of[Double],
      "radius" -> of[Double]
    )(Data.apply)(Data.unapply)
  )
}
