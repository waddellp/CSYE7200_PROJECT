package controllers

import java.util.{Calendar, GregorianCalendar}

import akka.actor.ActorSystem
import javax.inject._
import model.edu.neu.coe.csye7200.proj.{DateTime, ForecasterUtil, Location}
import org.apache.spark.FutureAction
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import play.api.mvc._

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future, Promise}

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

  import LookupForm._
  import model.edu.neu.coe.csye7200.proj.USGeoSurvey

  private val postUrl = routes.LookupController.lookupPost()
  private val spark = SparkSession.builder().appName("HistoricalLookup").master("local[*]").getOrCreate()
  private val sc = spark.sparkContext

  def lookup = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.lookup(form, postUrl))
  }

  /**
   * Creates an Action that returns a plain text message after a delay
   * of 1 second.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/message`.
   */
  def lookupPost = Action.async { implicit request =>
    val formData: Data = form.bindFromRequest.get
    val result = getFutureUSGS(formData)
    result map (res => Ok(views.html.lookupresult(res)))
  }

  private def getFutureUSGS(formData: Data): FutureAction[Seq[USGeoSurvey]] = {
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
