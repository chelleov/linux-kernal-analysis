package il.ac.hit.functional.analysis

import il.ac.hit.functional.model.{
  CommitSummary,
  ContributorCount,
  ContributorWithId
}
import org.apache.spark.sql.{Dataset, SparkSession}

/** Trait defining the contract for computing contributor statistics using
  * Spark.
  */
trait IContributorAnalyzer {

  /** Reads commits from a CSV file and returns the top N contributors, ordered
    * by commit count descending.
    *
    * @param spark
    *   the active SparkSession
    * @param csvPath
    *   path to the commits CSV file
    * @param topN
    *   number of top contributors to return
    * @return
    *   Right with a Dataset of the top N contributors on success, or Left with
    *   an error message on failure
    */
  def topContributors(
      spark: SparkSession,
      csvPath: String,
      topN: Int
  ): Either[String, Dataset[ContributorCount]]

  /** Returns the top N contributors with sequential IDs (0 to n-1).
    *
    * @param spark
    *   the active SparkSession
    * @param csvPath
    *   path to the commits CSV file
    * @param topN
    *   number of top contributors to return
    * @return
    *   Right with a Dataset of ContributorWithId on success, or Left with an
    *   error message on failure
    */
  def topContributorsWithId(
      spark: SparkSession,
      csvPath: String,
      topN: Int
  ): Either[String, Dataset[ContributorWithId]]

  /** Returns the commit timeline for a single contributor, sorted by timestamp.
    *
    * @param spark
    *   the active SparkSession
    * @param csvPath
    *   path to the commits CSV file
    * @param authorName
    *   the contributor's name to filter by
    * @return
    *   Right with a Dataset of CommitSummary on success, or Left with an error
    *   message on failure
    */
  def contributorTimeline(
      spark: SparkSession,
      csvPath: String,
      authorName: String
  ): Either[String, Dataset[CommitSummary]]
}
