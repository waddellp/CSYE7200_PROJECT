package controllers

import java.util.Calendar

import akka.actor.ActorSystem
import javax.inject._
import model.edu.neu.coe.csye7200.proj.{DateTime, ForecasterUtil, Location}
import org.apache.spark.FutureAction
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import play.api.mvc._
import LookupForm._
import model.edu.neu.coe.csye7200.proj.USGeoSurvey

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
class LookupController @Inject()(cc: MessagesControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc) {

  private val postUrl = routes.LookupController.lookupPost()
  private val spark = SparkSession.builder().appName("HistoricalLookup").master("local[*]").getOrCreate()
  private val sc = spark.sparkContext

  def lookup = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.lookup(form, postUrl))
  }

  /**
   * Creates an Action that returns a Sequence of US Geological Survey data
   */
  def lookupPost = Action.async { implicit request =>
    form.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.html.lookup(errors, postUrl))),
      {
        formData =>
          val valForm = validateForm(form.fill(formData))
          if (valForm.hasErrors) {
            Future.successful(Ok(views.html.lookup(valForm, postUrl)))
          } else {
            val result = getFutureUSGS(formData)
            result map (res => {
              if (res.nonEmpty) {
                Ok(views.html.lookupresult(res))
              } else {
                BadRequest(views.html.lookup(form.withGlobalError("Error - No results found"), postUrl))
              }
            })
          }
      })
  }

  /**
   * Performs a lookup into the dataset based on values provided for user input search parameters
   *
   * @param formData Lookup data
   * @return Search results based on values provided for user input search parameters
   */
  private def getFutureUSGS(formData: LookupData): FutureAction[Seq[USGeoSurvey]] = {
    val cal = Calendar.getInstance()
    cal.setTime(formData.startDate)
    val startDate = DateTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0 )
    cal.setTime(formData.endDate)
    val endDate = DateTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59 )
    val data: RDD[USGeoSurvey] = ForecasterUtil.loadData(sc)
    val q = ForecasterUtil.getEarthquakes(data)
    val qr = ForecasterUtil.getDateRange(q, startDate, endDate)
    val qrl = ForecasterUtil.getLocationArea(qr, Location(formData.latitude, formData.longitude, ""), formData.radius)
    val qrls = ForecasterUtil.sortByMagnitude(qrl)
    qrls.collectAsync()
  }
}
