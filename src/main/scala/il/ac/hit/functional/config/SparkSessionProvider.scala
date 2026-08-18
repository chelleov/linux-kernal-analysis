package il.ac.hit.functional.config

import org.apache.spark.sql.SparkSession

/**
 * Provides a local SparkSession for the pipeline.
 */
class SparkSessionProvider extends ISparkSessionProvider {

  /**
   * Creates a local SparkSession for the application.
   *
   * @param appName the Spark application name
   * @return a configured SparkSession
   */
  override def create(appName: String = "linux-kernel-analysis"): SparkSession = {
    if (appName == null || appName.isEmpty)
      throw new IllegalArgumentException("appName must not be empty")

    SparkSession
      .builder()
      .appName(appName)
      .master("local[*]")
      .getOrCreate()
  }
}

/**
 * Companion object for SparkSessionProvider.
 */
object SparkSessionProvider {

  /**
   * Creates a new SparkSessionProvider instance.
   *
   * @return a new SparkSessionProvider
   */
  def apply(): SparkSessionProvider = new SparkSessionProvider()
}