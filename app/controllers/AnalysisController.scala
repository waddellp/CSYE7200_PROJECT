package controllers

import java.util.Calendar

import akka.actor.ActorSystem
import controllers.AnalysisForm._
import javax.inject._
import model.edu.neu.coe.csye7200.proj.{DateTime, ForecasterUtil, Location, USGeoSurvey}
import org.apache.spark.FutureAction
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
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
class AnalysisController @Inject()(cc: MessagesControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc) {

  private val postUrl = routes.AnalysisController.analysisPost()
  private val spark = SparkSession.builder().appName("Analyze").master("local[*]").getOrCreate()
  private val sc = spark.sparkContext

  def analysis = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.analysis(form, postUrl))
  }

  /**
   * Creates an Action that returns a Sequence of US Geological Survey data
   */
  def analysisPost = Action.async { implicit request =>
    form.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.html.analysis(errors, postUrl))),
      {
        formData =>
          val valForm = validateForm(form.fill(formData))
          if (valForm.hasErrors) {
            Future.successful(Ok(views.html.analysis(valForm, postUrl)))
          } else {
            Future.successful(BadRequest)
          }
      })
  }
}
