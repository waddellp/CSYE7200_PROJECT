package controllers

import akka.actor.ActorSystem
import controllers.ForecastForm._
import javax.inject._
import model.edu.neu.coe.csye7200.proj.{ForecasterUtil, Location, USGeoSurvey}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Northeastern University
 * CSYE 7200 - Big Data System Engineering Using Scala
 * Project: World Earthquake Forecaster
 *
 * @author Patrick Waddell [001058235]
 * @author Rajendra kumar Rajkumar [001405755]
 */

@Singleton
class ForecastController @Inject()(cc: MessagesControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc) {

  private val postUrl = routes.ForecastController.forecastPost()
  private val spark = SparkSession.builder().appName("Analyze").master("local[*]").getOrCreate()
  private val sc = spark.sparkContext

  def forecast = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.forecast(form, postUrl))
  }

  /**
   * Creates an Action that returns of forecast analysis involving probability of earthquake occurrence
   */
  def forecastPost = Action.async { implicit request =>
    form.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.html.forecast(errors, postUrl))),
      {
        formData =>
          val valForm = validateForm(form.fill(formData))
          if (valForm.hasErrors) {
            Future.successful(Ok(views.html.forecast(valForm, postUrl)))
          } else {
            try {
              val result = forecastAnalysis(formData.latitude, formData.longitude, formData.radius,
                formData.magnitude, formData.years)
              Future.successful(
                Ok(views.html.forecastresult(
                  formData.latitude, formData.longitude, formData.radius,
                  formData.magnitude, formData.years,
                  result)))
            }
            catch {
              case e : Exception => Future.successful(BadRequest(views.html.forecast(form.withGlobalError("Error - No results found"), postUrl)))
            }
            }
      })
  }

  /**
   * Performs multiple linear regression on the input dataset and predicts the magnitude of a possible earthquake
   * occurrence at the user provided latitude, longitude and depth
   *
   * @param latitude  the latitude of the earthquake occurrence
   * @param longitude the longitude of the earthquake occurrence
   * @param radius    the radius within the user provided latitude and longitude
   * @param magnitude the magnitude of the earthquake occurrence
   * @param years     the number of years within which the probability needs to be calculated
   * @return Probability percentage of at least one possible earthquake occurrence
   */
  def forecastAnalysis(latitude: Double, longitude: Double, radius: Double, magnitude: Double, years: Int): Double = {
    val data: RDD[USGeoSurvey] = ForecasterUtil.loadData(sc)
    val q = ForecasterUtil.getEarthquakes(data)
    val ql = ForecasterUtil.getLocationArea(q, Location(latitude, longitude, ""), radius)
    val qlm = ForecasterUtil.filterByMagnitude(ql, magnitude)
    val fEarthquakecount = qlm.count()
    val earthquakeFrequency = fEarthquakecount / 11.0 //11.0 is used since ll year data is considered for analysis
    val probOfAtleast1Earthquake = 1 - scala.math.exp(-(earthquakeFrequency * years))
    probOfAtleast1Earthquake
  }
}
