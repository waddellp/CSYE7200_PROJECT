package controllers

import java.util.Calendar

import akka.actor.ActorSystem
import javax.inject._
import model.edu.neu.coe.csye7200.proj.{DateTime, ForecasterUtil}
import org.apache.spark.FutureAction
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import play.api.mvc._
import TopTenForm._
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
class TopTenController @Inject()(cc: MessagesControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends MessagesAbstractController(cc) {

  private val postUrl = routes.TopTenController.toptenPost()
  private val spark = SparkSession.builder().appName("TopTenLookup").master("local[*]").getOrCreate()
  private val sc = spark.sparkContext

  def topten = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.topten(form, postUrl))
  }

  /**
   * Creates an Action that returns top ten US Geological Survey data
   */
  def toptenPost = Action.async { implicit request =>
    form.bindFromRequest.fold(
      errors => Future.successful(BadRequest(views.html.topten(errors, postUrl))),
      {
        formData =>
          val valForm = validateForm(form.fill(formData))
          if (valForm.hasErrors) {
            Future.successful(Ok(views.html.topten(valForm, postUrl)))
          } else {
            val result = getFutureUSGS(formData)
            result map (res => {
              if (res.nonEmpty && res.size >= 10) {
                Ok(views.html.toptenresult(res.sortBy(-_._2.size).take(10)))
              } else {
                BadRequest(views.html.topten(form.withGlobalError("Error - ten results were not found"), postUrl))
              }
            })
          }
      })
  }

  /**
   * Displays the information about top 10 earthquake locations based on user input start date and end date
   *
   * @param formData TopTenData
   * @return Search results based on values provided for user input start date and end date
   */
  private def getFutureUSGS(formData: TopTenData): FutureAction[Seq[(String, Seq[USGeoSurvey])]] = {
    val cal = Calendar.getInstance()
    cal.setTime(formData.startDate)
    val startDate = DateTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0 )
    cal.setTime(formData.endDate)
    val endDate = DateTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59 )
    val data: RDD[USGeoSurvey] = ForecasterUtil.loadData(sc)
    val q = ForecasterUtil.getEarthquakes(data)
    val qr = ForecasterUtil.getDateRange(q, startDate, endDate)
    qr.groupBy(_.location.place).map(y => y._1 -> y._2.toSeq ).collectAsync()
  }
}
