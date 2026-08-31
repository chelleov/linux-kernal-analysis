package il.ac.hit.functional.output

import il.ac.hit.functional.analysis.IContributorAnalyzer
import il.ac.hit.functional.model.ContributorWithId
import org.apache.spark.sql.{Dataset, SparkSession}
import scala.util.Try

/** Writes per-contributor commit timelines to CSV using Spark's write API. For
 * each contributor, filters their commits from the source CSV and writes the
 * result to a numbered subdirectory under the base output path.
 */
class ContributorTimelineWriter extends IContributorTimelineWriter {

  /** Writes a CSV file for each contributor in the dataset.
   *
   * @param contributors
   *   the Dataset of top contributors with IDs
   * @param csvPath
   *   path to the source commits CSV file
   * @param spark
   *   the active SparkSession
   * @param analyzer
   *   the contributor analyzer to use for timeline extraction
   * @param basePath
   *   the base output directory for timeline files
   * @return
   *   Right(()) on success, or Left with an error message on failure
   */
  override def write(
                      contributors: Dataset[ContributorWithId],
                      csvPath: String,
                      spark: SparkSession,
                      analyzer: IContributorAnalyzer,
                      basePath: String
                    ): Either[String, Unit] = {
    if (contributors == null) return Left("contributors must not be null")
    if (csvPath == null || csvPath.isEmpty)
      return Left("csvPath must not be empty")
    if (spark == null) return Left("spark session must not be null")
    if (analyzer == null) return Left("analyzer must not be null")
    if (basePath == null || basePath.isEmpty)
      return Left("basePath must not be empty")

    // Collect the top contributors to the driver, then map each one to
    // either an error message or a successful write. No mutable state is
    // used: results are combined purely via collectFirst.
    val results: Seq[Either[String, Unit]] =
      contributors.collect().toSeq.map(writeOneContributor(spark, csvPath, analyzer, basePath))

    results.collectFirst { case Left(err) => Left(err) }.getOrElse(Right(()))
  }

  /** Writes a single contributor's timeline: builds it via the analyzer, then
   * writes it to a numbered subdirectory under basePath. Performs no
   * mutation — returns the outcome as an Either.
   */
  private def writeOneContributor(
                                   spark: SparkSession,
                                   csvPath: String,
                                   analyzer: IContributorAnalyzer,
                                   basePath: String
                                 )(contributor: ContributorWithId): Either[String, Unit] = {
    val outputPath = s"$basePath/${contributor.id}"

    analyzer.contributorTimeline(spark, csvPath, contributor.authorName) match {
      case Left(err) =>
        Left(s"Failed to get timeline for ${contributor.authorName}: $err")
      case Right(timeline) =>
        Try {
          timeline
            .coalesce(1)
            .write
            .mode("overwrite")
            .option("header", "true")
            .csv(outputPath)
        }.toEither.left.map(ex =>
          s"Failed to write timeline for ${contributor.authorName}: ${ex.getMessage}"
        )
    }
  }
}

/** Companion object for ContributorTimelineWriter.
 */
object ContributorTimelineWriter {

  /** Creates a new ContributorTimelineWriter instance. */
  def apply(): ContributorTimelineWriter = new ContributorTimelineWriter()
}