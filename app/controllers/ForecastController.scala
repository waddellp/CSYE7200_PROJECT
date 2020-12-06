package controllers

import akka.actor.ActorSystem
import controllers.ForecastForm._
import javax.inject._
import model.edu.neu.coe.csye7200.proj.MLSpark.data
import model.edu.neu.coe.csye7200.proj.{ForecasterUtil, Location, USGeoSurvey}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
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
   * Creates an Action that returns a Sequence of US Geological Survey data
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
            Future.successful(
              Ok(views.html.forecastresult(
                formData.latitude, formData.longitude, formData.radius,
                formData.magnitude, formData.years,
                forecastAnalysis(formData.latitude, formData.longitude, formData.radius,
                  formData.magnitude, formData.years))))
          }
      })
  }

  def forecastAnalysis(latitude: Double, longitude: Double, radius: Double, magnitude: Double, years: Int): Double = {
    val data: RDD[USGeoSurvey] = ForecasterUtil.loadData(sc)
    val q = ForecasterUtil.getEarthquakes(data)
    val ql = ForecasterUtil.getLocationArea(q, Location(latitude, longitude, ""), radius)
    val qlm = ForecasterUtil.filterByMagnitude(ql,magnitude)
    val fEarthquakecount = qlm.count()
    val earthquakeFrequency = fEarthquakecount/years
    val probOfAtleast1Earthquake = 1 - scala.math.exp(-(earthquakeFrequency * years))
    probOfAtleast1Earthquake
  }
}
