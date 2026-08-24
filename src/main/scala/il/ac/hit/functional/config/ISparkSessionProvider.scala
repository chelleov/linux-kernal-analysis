package il.ac.hit.functional.config

import org.apache.spark.sql.SparkSession

/** Trait defining the contract for providing a SparkSession.
  */
trait ISparkSessionProvider {

  /** Creates a local SparkSession for the application.
    *
    * @param appName
    *   the Spark application name
    * @return
    *   Right with a configured SparkSession on success, or Left with an error
    *   message if appName is invalid
    */
  def create(appName: String): Either[String, SparkSession]
}
