package il.ac.hit.functional.output

import il.ac.hit.functional.model.ContributorCount
import org.apache.spark.sql.Dataset
import scala.util.Try

/** Writes top contributor statistics to CSV using Spark's write API.
  */
class TopContributorsWriter extends ITopContributorsWriter {

  /** Writes a Dataset of ContributorCount to a CSV file using Spark's own
    * DataFrameWriter.
    *
    * @param dataset
    *   the Dataset to write
    * @param path
    *   the output path
    * @return
    *   Right(()) on success, or Left with an error message on failure
    */
  override def write(
      dataset: Dataset[ContributorCount],
      path: String
  ): Either[String, Unit] = {
    if (dataset == null) return Left("dataset must not be null")
    if (path == null || path.isEmpty) return Left("path must not be empty")

    Try {
      // coalesce(1) merges all partitions into one, producing a single
      // part-* CSV file instead of one per partition
      dataset
        .coalesce(1)
        .write
        .mode("overwrite")
        .option("header", "true")
        .csv(path)
    } match {
      case scala.util.Success(_)         => Right(())
      case scala.util.Failure(exception) =>
        Left(s"Failed to write contributors CSV: ${exception.getMessage}")
    }
  }
}

/** Companion object for TopContributorsWriter.
  */
object TopContributorsWriter {

  /** Creates a new TopContributorsWriter instance. */
  def apply(): TopContributorsWriter = new TopContributorsWriter()
}
