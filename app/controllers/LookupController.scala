package controllers

import akka.actor.ActorSystem
import javax.inject._
import model.edu.neu.coe.csye7200.proj.{DataParse, DateTime, ForecasterUtil, Location, USGeoSurvey}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import play.api.data.Form
import play.api.i18n._
import play.api.mvc._

import scala.collection.convert.ImplicitConversions.`collection asJava`
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class LookupController @Inject()(cc: MessagesControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc) {

  import LookupForm._
  import model.edu.neu.coe.csye7200.proj.USGeoSurvey

  var userFormData: Data = Data(0.0,0.0,0.0)
  private val postUrl = routes.LookupController.message()

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
  def message = Action.async {
    getFutureUSGS(20.second).map { u => Ok(u) }
  }

  private def getFutureUSGS(delayTime: FiniteDuration): Future[String] = {
    val promise: Promise[String] = Promise[String]()
    actorSystem.scheduler.scheduleOnce(delayTime) {
      println("START")
      println("Lat: " + userFormData.latitude + " long: " + userFormData.longitude + " radius: " + userFormData.radius)
      val spark = SparkSession.builder().appName("HistoricalLookup").master("local[*]").getOrCreate()
      val sc = spark.sparkContext
      val data: RDD[USGeoSurvey] = ForecasterUtil.loadData(sc)
      val q = ForecasterUtil.getEarthquakes(data)
      val qr = ForecasterUtil.getDateRange(q, DateTime("2020-10-01T00:00:00.000Z"), DateTime("2020-10-31T23:59:59.000Z"))
      val qrl = ForecasterUtil.getLocationArea(qr, Location(54.662, -159.675, "Alaska Peninsula"), 50.0)
      val qrls = ForecasterUtil.sortByMagnitude(qrl)
      promise.success(qrls.collect().toSeq.head.toString())
      spark.close()
    }(actorSystem.dispatcher) // run scheduled tasks using the actor system's dispatcher
    promise.future
  }

  def lookupPost = Action.async { implicit request: Request[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Data] =>
        // binding failure, you retrieve the form containing errors:
        Future(BadRequest(views.html.lookupresult("Error")))
      }
    val successFunction = { formData: Data =>
        /* binding success, you get the actual value. */
        println("Received data: " + formData.latitude + ", " + formData.longitude + ", " + formData.radius)
        userFormData = formData
        Future(Redirect(routes.LookupController.message()).flashing("info" -> "Lookup complete"))
    }
    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  def lookupresult = Action {
    Ok(views.html.lookupresult(""))
  }
/*
import scala.collection.convert.ImplicitConversions.`collection asJava`
import scala.collection.mutable

class LookupController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  import LookupForm._
  import model.edu.neu.coe.csye7200.proj.USGeoSurvey

  private var historicalEvents: Seq[USGeoSurvey] = Seq.empty[USGeoSurvey]
  private val postUrl = routes.LookupController.historicalLookup()

  def lookup = Action { implicit request: MessagesRequest[AnyContent] =>
    // Pass an unpopulated form into the template
    Ok(views.html.lookup(historicalEvents, form, postUrl))
  }

  def clear = Action { implicit request: MessagesRequest[AnyContent] =>
    // Pass an unpopulated form into the template
    historicalEvents.clear()
    println("LOOKUP CLEARED")
    Redirect(routes.LookupController.lookup()).flashing("info" -> "Lookup cleared")
  }

  def historicalLookup = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Data] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.lookup(historicalEvents, formWithErrors, postUrl))
    }

    val successFunction = { data: Data =>
      // This is the good case, where the form was successfully parsed as a Data object.
      println("START")
      val spark = SparkSession.builder().appName("HistoricalLookup").master("local[*]").getOrCreate()
      val sc = spark.sparkContext
      val data: RDD[USGeoSurvey] = ForecasterUtil.loadData(sc)
      val q = ForecasterUtil.getEarthquakes(data)
      val qr = ForecasterUtil.getDateRange(q, DateTime("2020-10-01T00:00:00.000Z"), DateTime("2020-10-31T23:59:59.000Z"))
      val qrl = ForecasterUtil.getLocationArea(qr, Location(54.662, -159.675, "Alaska Peninsula"), 50.0)
      val qrls = ForecasterUtil.sortByMagnitude(qrl)
      historicalEvents = qrls.collect().toSeq
      spark.close()
      println("COMPLETE")
      //Redirect(routes.LookupController.lookup()).flashing("info" -> "Lookup complete")
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
  */
}
