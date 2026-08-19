package il.ac.hit.functional.analysis

import il.ac.hit.functional.model.{AuthorRecord, CommitSummary, ContributorCount, ContributorWithId}
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.sql.functions.{count, monotonically_increasing_id}
import scala.util.Try

/**
 * Computes contributor commit statistics using Spark's Dataset API.
 */
class ContributorAnalyzer extends IContributorAnalyzer {

  /**
   * Reads commits from a CSV file and returns the top N contributors,
   * ordered by commit count descending. Uses Spark operations: filter,
   * map, groupBy, agg, filter, orderBy, and limit.
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

    // Combinator: two primitive predicates combined via the `and` combinator
    val validContributor = ContributorFilters.and(
      ContributorFilters.minCommits(1),
      ContributorFilters.nameContains("")
    )

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
        // closure over validContributor, captured from the enclosing scope inside a Spark transformation
        .filter(c => validContributor(c))
        .orderBy($"commitCount".desc)
        .limit(topN)
    } match {
      case scala.util.Success(dataset) => Right(dataset)
      case scala.util.Failure(exception) => Left(s"Failed to analyze contributors: ${exception.getMessage}")
    }
  }

  /**
   * Returns the top N contributors with sequential IDs (0 to n-1).
   * Uses monotonically_increasing_id to assign IDs after ordering by commit count.
   *
   * @param spark   the active SparkSession
   * @param csvPath path to the commits CSV file
   * @param topN    number of top contributors to return
   * @return Right with a Dataset of ContributorWithId on success,
   *         or Left with an error message on failure
   */
  override def topContributorsWithId(
                                      spark: SparkSession,
                                      csvPath: String,
                                      topN: Int
                                    ): Either[String, Dataset[ContributorWithId]] = {
    if (spark == null) return Left("spark session must not be null")
    if (csvPath == null || csvPath.isEmpty) return Left("csvPath must not be empty")
    if (topN <= 0) return Left("topN must be positive")

    topContributors(spark, csvPath, topN) match {
      case Right(dataset) =>
        import spark.implicits._
        Try {
          dataset
            .withColumn("id", monotonically_increasing_id().cast("int"))
            .as[ContributorWithId]
        } match {
          case scala.util.Success(ds) => Right(ds)
          case scala.util.Failure(ex) => Left(s"Failed to assign IDs: ${ex.getMessage}")
        }
      case Left(err) => Left(err)
    }
  }

  /**
   * Returns the commit timeline for a single contributor, sorted by timestamp.
   * Uses Spark filter, select, and orderBy to extract the timeline.
   *
   * @param spark      the active SparkSession
   * @param csvPath    path to the commits CSV file
   * @param authorName the contributor's name to filter by
   * @return Right with a Dataset of CommitSummary on success,
   *         or Left with an error message on failure
   */
  override def contributorTimeline(
                                    spark: SparkSession,
                                    csvPath: String,
                                    authorName: String
                                  ): Either[String, Dataset[CommitSummary]] = {
    if (spark == null) return Left("spark session must not be null")
    if (csvPath == null || csvPath.isEmpty) return Left("csvPath must not be empty")
    if (authorName == null || authorName.isEmpty) return Left("authorName must not be empty")

    import spark.implicits._

    Try {
      val rawDf = spark.read.option("header", "true").csv(csvPath)

      rawDf
        .filter($"author_name" === authorName)
        .select($"author_timestamp".cast("long").as("authorTimestamp"), $"hash", $"subject")
        .as[CommitSummary]
        .orderBy($"authorTimestamp".asc)
    } match {
      case scala.util.Success(dataset) => Right(dataset)
      case scala.util.Failure(exception) => Left(s"Failed to build timeline: ${exception.getMessage}")
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