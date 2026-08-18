package il.ac.hit.functional.analysis

import il.ac.hit.functional.model.ContributorCount
import org.apache.spark.sql.{Dataset, SparkSession}

/**
 * Trait defining the contract for computing contributor statistics using Spark.
 */
trait IContributorAnalyzer {

  /**
   * Reads commits from a CSV file and returns the top N contributors,
   * ordered by commit count descending.
   *
   * @param spark   the active SparkSession
   * @param csvPath path to the commits CSV file
   * @param topN    number of top contributors to return
   * @return Right with a Dataset of the top N contributors on success,
   *         or Left with an error message on failure
   */
  def topContributors(
                       spark: SparkSession,
                       csvPath: String,
                       topN: Int
                     ): Either[String, Dataset[ContributorCount]]
}