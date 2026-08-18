package il.ac.hit.functional.output

import il.ac.hit.functional.model.ContributorCount
import org.apache.spark.sql.Dataset

/**
 * Trait defining the contract for writing a contributor Dataset to CSV.
 */
trait ITopContributorsWriter {

  /**
   * Writes a Dataset of ContributorCount to a CSV file using Spark's
   * own DataFrameWriter.
   *
   * @param dataset the Dataset to write
   * @param path    the output path
   * @return Right(()) on success, or Left with an error message on failure
   */
  def write(dataset: Dataset[ContributorCount], path: String): Either[String, Unit]
}