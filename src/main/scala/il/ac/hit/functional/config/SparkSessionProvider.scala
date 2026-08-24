package il.ac.hit.functional.config

import org.apache.spark.sql.SparkSession
import scala.util.Try

/** Provides a local SparkSession for the pipeline.
  */
class SparkSessionProvider extends ISparkSessionProvider {

  /** Creates a local SparkSession for the application.
    *
    * @param appName
    *   the Spark application name
    * @return
    *   Right with a configured SparkSession on success, or Left with an error
    *   message if appName is null or empty, or if session creation fails
    */
  override def create(
      appName: String = "linux-kernel-analysis"
  ): Either[String, SparkSession] = {
    if (appName == null || appName.isEmpty)
      return Left("appName must not be empty")

    Try {
      // local[*] runs Spark in local mode using all available machine cores
      SparkSession
        .builder()
        .appName(appName)
        .master("local[*]")
        .getOrCreate()
    } match {
      case scala.util.Success(session)   => Right(session)
      case scala.util.Failure(exception) =>
        Left(s"Failed to create SparkSession: ${exception.getMessage}")
    }
  }
}

/** Companion object for SparkSessionProvider.
  */
object SparkSessionProvider {

  /** Creates a new SparkSessionProvider instance.
    *
    * @return
    *   a new SparkSessionProvider
    */
  def apply(): SparkSessionProvider = new SparkSessionProvider()
}
