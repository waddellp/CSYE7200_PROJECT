package controllers

import akka.actor.ActorSystem
import javax.inject._
import model.edu.neu.coe.csye7200.proj.{DateTime, ForecasterUtil, Location}
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

  var userFormData: Data = Data(0.0,0.0,0.0)
  private val postUrl = routes.LookupController.lookupPost()

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
  def lookupPost = Action.async {
    getFutureUSGS(3.second).map (u => Ok(views.html.lookupresult(u)))
  }

  private def getFutureUSGS(delayTime: FiniteDuration): Future[Seq[USGeoSurvey]] = {
    val promise: Promise[Seq[USGeoSurvey]] = Promise[Seq[USGeoSurvey]]()
    actorSystem.scheduler.scheduleOnce(delayTime) {
      val spark = SparkSession.builder().appName("HistoricalLookup").master("local[*]").getOrCreate()
      val sc = spark.sparkContext
      val data: RDD[USGeoSurvey] = ForecasterUtil.loadData(sc)
      val q = ForecasterUtil.getEarthquakes(data)
      val qr = ForecasterUtil.getDateRange(q, DateTime("2020-10-01T00:00:00.000Z"), DateTime("2020-10-31T23:59:59.000Z"))
      val qrl = ForecasterUtil.getLocationArea(qr, Location(54.662, -159.675, "Alaska Peninsula"), 50.0)
      val qrls = ForecasterUtil.sortByMagnitude(qrl)
      promise.success(qrls.collect().toSeq)
      spark.close()
    }(actorSystem.dispatcher) // run scheduled tasks using the actor system's dispatcher
    promise.future
  }
}
