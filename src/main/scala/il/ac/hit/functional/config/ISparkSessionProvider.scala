package il.ac.hit.functional.config

import org.apache.spark.sql.SparkSession

/**
 * Trait defining the contract for providing a SparkSession.
 */
trait ISparkSessionProvider {

  /**
   * Creates a local SparkSession for the application.
   *
   * @param appName the Spark application name
   * @return a configured SparkSession
   */
  def create(appName: String): SparkSession
}