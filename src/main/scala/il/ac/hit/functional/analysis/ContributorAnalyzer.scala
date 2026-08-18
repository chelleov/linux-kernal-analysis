package il.ac.hit.functional.analysis

import il.ac.hit.functional.model.{AuthorRecord, ContributorCount}
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.sql.functions.count
import scala.util.Try

/**
 * Computes contributor commit statistics using Spark's Dataset API.
 */
class ContributorAnalyzer extends IContributorAnalyzer {

  /**
   * Reads commits from a CSV file and returns the top N contributors,
   * ordered by commit count descending. Uses Spark operations: filter,
   * map, groupBy, agg, orderBy, and limit.
   *
   * @param spark   the active SparkSession
   * @param csvPath path to the commits CSV file
   * @param topN    number of top contributors to return
   * @return Right with a Dataset of the top N contributors on success,
   *         or Left with an error message on failure
   */
  override def topContributors(
                                spark: SparkSession,
                                csvPath: String,
                                topN: Int
                              ): Either[String, Dataset[ContributorCount]] = {
    if (spark == null) return Left("spark session must not be null")
    if (csvPath == null || csvPath.isEmpty) return Left("csvPath must not be empty")
    if (topN <= 0) return Left("topN must be positive")

    import spark.implicits._

    Try {
      val rawDf = spark.read.option("header", "true").csv(csvPath)

      // typed Dataset via map, matching the course's Dataset[Record] pattern
      val authors: Dataset[AuthorRecord] = rawDf
        .filter($"author_name".isNotNull)
        .map(row => AuthorRecord(ContributorAnalyzer.normalizeName(row.getAs[String]("author_name"))))

      authors
        .groupBy($"authorName")
        .agg(count("*").as("commitCount"))
        .as[ContributorCount]
        .orderBy($"commitCount".desc)
        .limit(topN)
    } match {
      case scala.util.Success(dataset) => Right(dataset)
      case scala.util.Failure(exception) => Left(s"Failed to analyze contributors: ${exception.getMessage}")
    }
  }
}

/**
 * Companion object for ContributorAnalyzer.
 */
object ContributorAnalyzer {

  /** Creates a new ContributorAnalyzer instance. */
  def apply(): ContributorAnalyzer = new ContributorAnalyzer()

  private val trimName: String => String = _.trim.replaceAll("\\s+", " ")
  private val capitalizeFirst: String => String = s =>
    if (s.isEmpty) s else s.charAt(0).toUpper + s.substring(1)

  /**
   * Normalizes an author name by trimming and capitalizing.
   * Composed via andThen, matching the course's Functions Composition material.
   */
  val normalizeName: String => String = trimName andThen capitalizeFirst
}