package controllers

import akka.actor.ActorSystem
import controllers.AnalysisForm._
import javax.inject._
import model.edu.neu.coe.csye7200.proj.{ForecasterUtil, USGeoSurvey}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.types.{DoubleType, StructField, StructType}
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
            Future.successful(
              Ok(views.html.analysisresult(
                formData.latitude, formData.longitude,
                linearAnalysis(formData.latitude, formData.longitude))))
          }
      })
  }

  def linearAnalysis(latitude: Double, longitude: Double) = {
    val data: RDD[USGeoSurvey] = ForecasterUtil.loadData(sc)
    val df = spark.createDataFrame(data).toDF()
    val flattenedDF = df.select(col("id"),
      col("datetime.*"),
      col("location.*"),
      col("magnitude.*"),
      col("eventtype")
    )

    //Renaming mag as 'label' for convenience purpose
    val renamedDF = flattenedDF.withColumnRenamed("magnitude", "label")

    val assembler1 = new VectorAssembler().
      setInputCols(Array("latitude", "longitude")).
      setOutputCol("features").setHandleInvalid("skip")

    val output = assembler1.transform(renamedDF)

    val trainingTest = output.randomSplit(Array(0.7,0.3))
    val trainingDF = trainingTest(0)
    val testDF = trainingTest(1)

    val lir = new LinearRegression()
      //.setLabelCol("mag")
      //.setFeaturesCol("latitude")
      //.setFeaturesCol("longitude")
      //.setFeaturesCol("depth")
      //.setFeaturesCol("depthError")
      .setRegParam(0.001)
      .setElasticNetParam(0.0001)
      .setMaxIter(100)
    //.setTol(1E-6)

    val lrModel = lir.fit(trainingDF)
    val lrPredictions = lrModel.transform(testDF)

    //Use case 1: Getting latitude, longitude details from User and displaying
    //the probable magnitude of an earthquake occurrence
    val userInputData = Seq(Row(latitude, longitude))
    val userInputSchema = List(
      StructField("latitude", DoubleType, true),
      StructField("longitude", DoubleType, true)
    )
    val userInputDF = spark.createDataFrame(sc.parallelize(userInputData),
      StructType(userInputSchema))
    val output1 = assembler1.transform(userInputDF)
    val userInputTrainingSet = output1.randomSplit(Array(1,0))
    val userInputDataSet1 = userInputTrainingSet(0)
    val userInputPredictions = lrModel.transform(userInputDataSet1)

    val userInputPredictionAndLabel = userInputPredictions.select("latitude","longitude","prediction").rdd.map(x => (x.getDouble(0), x.getDouble(1), x.getDouble(2)))
    userInputPredictionAndLabel.collect().toSeq.head._3
  }
}
