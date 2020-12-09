package model.edu.neu.coe.csye7200.proj

import algebra.instances.all.catsKernelStdOrderForString
import model.edu.neu.coe.csye7200.proj.SparkTest.{getClass, sc}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.ml.feature.{StandardScaler, StringIndexer, VectorAssembler}
import org.apache.spark.ml.regression.{DecisionTreeRegressor, GeneralizedLinearRegression, LinearRegression}
import org.apache.spark.sql.catalyst.ScalaReflection.universe.show
import spire.implicits.eqOps
import org.apache.spark.ml.feature.Normalizer
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.catalyst.encoders.RowEncoder
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.functions.{col, exp}
import org.apache.spark.sql.types.{DoubleType, IntegerType, StructField, StructType}

/**
 * Northeastern University
 * CSYE 7200 - Big Data System Engineering Using Scala
 * Project: World Earthquake Forecaster
 *
 * @author Patrick Waddell [001058235]
 * @author Rajendra kumar Rajkumar [001405755]
 */

object MLSpark extends App{
    val log = Logger.getLogger(getClass.getName)
    log.setLevel(Level.ERROR)

    val spark = SparkSession.builder()
      .master("local")
      .appName("Creating DF using CSV in Spark 2.x way using Spark Session")
      .getOrCreate()
    val sc = spark.sparkContext
    val data: RDD[USGeoSurvey] = ForecasterUtil.loadData(sc)
    val df = spark.createDataFrame(data).toDF()
    df.printSchema()
    println("The total number of rows:"+df.count())

    val flattenedDF = df.select(col("id"),
      col("datetime.*"),
      col("location.*"),
      col("magnitude.*"),
      col("eventtype")
    )

    flattenedDF.printSchema()

    //Renaming mag as 'label' for convenience purpose
    val renamedDF = flattenedDF.withColumnRenamed("magnitude", "label")
    val filteredDF = renamedDF.where(col("eventtype") === "earthquake").toDF()

    val assembler1 = new VectorAssembler().
      setInputCols(Array("latitude", "longitude", "depth")).
      setOutputCol("features").setHandleInvalid("skip")

    val output = assembler1.transform(filteredDF)

    val trainingTest = output.randomSplit(Array(0.7,0.3))
    val trainingDF = trainingTest(0)
    val testDF = trainingTest(1)

    val lir = new LinearRegression()
      //.setLabelCol("mag")
      //.setFeaturesCol("latitude")
      //.setFeaturesCol("longitude")
      //.setFeaturesCol("depth")
      //.setFeaturesCol("depthError")
      .setRegParam(0.001)//0.001
      .setElasticNetParam(0.0001)//0.0001
      .setMaxIter(100)
      //.setEpsilon(100)
      //.setAggregationDepth(100)
      //.setFitIntercept(false)
      .setTol(1E-24)

    val lrModel = lir.fit(trainingDF)
    val lrPredictions = lrModel.transform(testDF)
    lrPredictions.show()

    println("Printing the schema of testDF")
    testDF.printSchema()
    println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")

    val trainingSummary = lrModel.summary
    println(s"numIterations: ${trainingSummary.totalIterations}")
    println(s"objectiveHistory: [${trainingSummary.objectiveHistory.mkString(",")}]")
    trainingSummary.residuals.show()
    println(s"RMSE: ${trainingSummary.rootMeanSquaredError}")
    println(s"r2: ${trainingSummary.r2}")
    println(s"Adjusted r2: ${trainingSummary.r2adj}")

    val predictionAndLabel = lrPredictions.select("prediction", "label").rdd.map(x => (x.getDouble(0), x.getDouble(1)))
    predictionAndLabel.take(20).foreach(println)

    lrModel.transform(testDF).select("features","label", "prediction").show()
    println("***Printing only features***")
    lrModel.transform(testDF).select("features").show()

    //Use case 1: Getting latitude, longitude details from User and displaying
    //the probable magnitude of an earthquake occurrence
    //To be received from UI. Hardcoded for now
    //val userInputData = Seq(Row(67.5132, -160.9215, 700.0, 8.0)) //Latitude and Longitude
    val userInputData = Seq(Row(67.5132, -160.9215, 700.0)) //Latitude and Longitude


    val userInputSchema = List(
      StructField("latitude", DoubleType, true),
      StructField("longitude", DoubleType, true),
        StructField("depth", DoubleType, true)
    )

    val userInputDF = spark.createDataFrame(spark.sparkContext.parallelize(userInputData),
      StructType(userInputSchema))

    val output1 = assembler1.transform(userInputDF)
    val userInputTrainingSet = output1.randomSplit(Array(1,0))
    val userInputDataSet1 = userInputTrainingSet(0)

    val userInputPredictions = lrModel.transform(userInputDataSet1)

    userInputPredictions.show()
    //@Patrick, Please use u._3 for Predicted Magnitude output
    val userInputPredictionAndLabel = userInputPredictions.select("latitude","longitude","depth", "prediction").rdd.map(x => (x.getDouble(0), x.getDouble(1), x.getDouble(2), x.getDouble(3)))
    userInputPredictionAndLabel.collect().foreach(u => println("Predictions for User Input:\n Latitude: "+u._1
      +"\n Longitude:"+u._2+"\nDepth:"+u._3+ "\nPredicted Magnitude:"+u._4))

    //Use case 2: Getting latitude, longitude, magnitude, radius  and Number of years from user and displaying the
    //the probability of at least one earthquake occurrence at the given location above the user given magnitude
    val noOfYears = 5.0 // Number of years for which probability needs to be calculated. Hardcoded for now. Need to get user input.
    val radius = 5.0 // Radius within user given location, where earthquake occurrences are picked up.Hardcoded for now. Need to get user input.
    val magnitude = 3.0 //Magnitude of earthquake is hardcoded for now. Need to get user input.
    val q = ForecasterUtil.getEarthquakes(data)
    val ql = ForecasterUtil.getLocationArea(q, Location(67.5132, -160.9215, ""), radius) //Latitude and Longitude are hardcoded for now.Need to get user input
    //val ql = ForecasterUtil.getLocation(q, Location(67.5132, -160.9215, ""))
    val qlm = ForecasterUtil.filterByMagnitude(ql,magnitude)
    val fEarthquakecount = qlm.count()
    val earthquakeFrequency = fEarthquakecount/11.0
    val probOfAtleast1Earthquake = 1 - scala.math.exp(-(earthquakeFrequency * noOfYears))

    println("The probability of having atleast one earthquake greater than magnitude of "+magnitude +" at user given location " +
      "in next "+noOfYears+" is: "+probOfAtleast1Earthquake)
    spark.stop()
}
