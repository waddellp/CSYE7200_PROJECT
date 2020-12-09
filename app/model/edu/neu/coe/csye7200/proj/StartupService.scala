package model.edu.neu.coe.csye7200.proj

import controllers.routes
import javax.inject.Singleton
import model.edu.neu.coe.csye7200.proj.MLSpark.sc
import model.edu.neu.coe.csye7200.proj.SparkTest.spark
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col

@Singleton
class StartupService {
/*
  private val spark = SparkSession.builder().appName("Analyze").master("local[*]").getOrCreate()
  private val sc = spark.sparkContext

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
  val filteredDF = renamedDF.where(col("eventtype") === "earthquake").toDF()

  val assembler1 = new VectorAssembler().
    setInputCols(Array("latitude", "longitude", "depth", "depthError")).
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
    .setRegParam(0.001)
    .setElasticNetParam(0.0001)
    .setMaxIter(100)
    .setTol(1E-24)

  val lrModel = lir.fit(trainingDF)

*/
}
