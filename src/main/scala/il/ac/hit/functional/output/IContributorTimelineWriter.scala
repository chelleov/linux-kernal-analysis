package il.ac.hit.functional.output

import il.ac.hit.functional.analysis.IContributorAnalyzer
import il.ac.hit.functional.model.ContributorWithId
import org.apache.spark.sql.{Dataset, SparkSession}

/**
 * Trait defining the contract for writing per-contributor commit timelines to CSV.
 */
trait IContributorTimelineWriter {

  /**
   * Writes a CSV file for each contributor in the dataset, containing
   * their commits over time.
   *
   * @param contributors the Dataset of top contributors with IDs
   * @param csvPath      path to the source commits CSV file
   * @param spark        the active SparkSession
   * @param analyzer     the contributor analyzer to use for timeline extraction
   * @param basePath     the base output directory for timeline files
   * @return Right(()) on success, or Left with an error message on failure
   */
  def write(
             contributors: Dataset[ContributorWithId],
             csvPath: String,
             spark: SparkSession,
             analyzer: IContributorAnalyzer,
             basePath: String
           ): Either[String, Unit]
}
