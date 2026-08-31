package il.ac.hit.functional.analysis

import il.ac.hit.functional.model.{
  AuthorRecord,
  CommitSummary,
  ContributorCount,
  ContributorWithId
}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.sql.functions.{
  count,
  initcap,
  monotonically_increasing_id,
  regexp_replace,
  trim
}
import scala.util.Try

/** Computes contributor commit statistics using Spark's Dataset API. I/O
  * (reading CSV files) is kept in the public override methods; all
  * transformation logic is delegated to pure functions in the companion object,
  * which operate only on already-loaded data.
  */
class ContributorAnalyzer extends IContributorAnalyzer {

  /** Reads commits from a CSV file and returns the top N contributors, ordered
    * by commit count descending. Uses Spark operations: filter, map, groupBy,
    * agg, filter, orderBy, and limit.
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
  override def topContributors(
      spark: SparkSession,
      csvPath: String,
      topN: Int
  ): Either[String, Dataset[ContributorCount]] = {
    if (spark == null) return Left("spark session must not be null")
    if (csvPath == null || csvPath.isEmpty)
      return Left("csvPath must not be empty")
    if (topN <= 0) return Left("topN must be positive")

    Try {
      // I/O: reading the raw data from disk
      val rawDf = spark.read.option("header", "true").csv(csvPath)
      // Pure: all transformation logic lives in the companion object
      ContributorAnalyzer.computeTopContributors(rawDf, topN)(spark)
    } match {
      case scala.util.Success(dataset)   => Right(dataset)
      case scala.util.Failure(exception) =>
        Left(s"Failed to analyze contributors: ${exception.getMessage}")
    }
  }

  /** Returns the top N contributors with sequential IDs (0 to n-1). Uses
    * monotonically_increasing_id to assign IDs after ordering by commit count.
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
  override def topContributorsWithId(
      spark: SparkSession,
      csvPath: String,
      topN: Int
  ): Either[String, Dataset[ContributorWithId]] = {
    if (spark == null) return Left("spark session must not be null")
    if (csvPath == null || csvPath.isEmpty)
      return Left("csvPath must not be empty")
    if (topN <= 0) return Left("topN must be positive")

    topContributors(spark, csvPath, topN) match {
      case Right(dataset) =>
        Try {
          // Pure: ID assignment logic in the companion object
          ContributorAnalyzer.assignSequentialIds(dataset)(spark)
        } match {
          case scala.util.Success(ds) => Right(ds)
          case scala.util.Failure(ex) =>
            Left(s"Failed to assign IDs: ${ex.getMessage}")
        }
      case Left(err) => Left(err)
    }
  }

  /** Returns the commit timeline for a single contributor, sorted by timestamp.
    * Uses Spark filter, select, and orderBy to extract the timeline.
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
  override def contributorTimeline(
      spark: SparkSession,
      csvPath: String,
      authorName: String
  ): Either[String, Dataset[CommitSummary]] = {
    if (spark == null) return Left("spark session must not be null")
    if (csvPath == null || csvPath.isEmpty)
      return Left("csvPath must not be empty")
    if (authorName == null || authorName.isEmpty)
      return Left("authorName must not be empty")

    Try {
      // I/O: reading the raw data from disk
      val rawDf = spark.read.option("header", "true").csv(csvPath)
      // Pure: filtering/selecting/ordering logic in the companion object
      ContributorAnalyzer.computeContributorTimeline(rawDf, authorName)(spark)
    } match {
      case scala.util.Success(dataset)   => Right(dataset)
      case scala.util.Failure(exception) =>
        Left(s"Failed to build timeline: ${exception.getMessage}")
    }
  }
}

/** Companion object for ContributorAnalyzer. Contains only pure functions: none
  * of the members here perform I/O — they all operate on data that has already
  * been read by the caller.
  */
object ContributorAnalyzer {

  /** Creates a new ContributorAnalyzer instance. */
  def apply(): ContributorAnalyzer = new ContributorAnalyzer()

  /** Pure transformation: computes the top N contributors from an
    * already-loaded raw commits DataFrame. Performs no I/O.
    *
    * @param rawDf
    *   the raw commits DataFrame, already read from disk
    * @param topN
    *   number of top contributors to return
    * @param spark
    *   implicit SparkSession, required for Dataset encoders
    * @return
    *   the top N contributors, ordered by commit count descending
    */
  def computeTopContributors(rawDf: DataFrame, topN: Int)(implicit
      spark: SparkSession
  ): Dataset[ContributorCount] = {
    import spark.implicits._

    // Normalize author names using built-in Spark SQL functions:
    // regexp_replace collapses whitespace, trim removes edges, initcap capitalizes
    val authors: Dataset[AuthorRecord] = rawDf
      .filter($"author_name".isNotNull)
      .withColumn(
        "authorName",
        initcap(trim(regexp_replace($"author_name", "\\s+", " ")))
      )
      .as[AuthorRecord]

    authors
      .groupBy($"authorName")
      .agg(count("*").as("commitCount"))
      .as[ContributorCount]
      .filter(_.commitCount >= 1)
      .orderBy($"commitCount".desc)
      .limit(topN)
  }

  /** Pure transformation: assigns a sequential ID (0 to n-1) to each row in an
    * already-computed top contributors Dataset. Performs no I/O.
    *
    * @param dataset
    *   the top contributors Dataset to assign IDs to
    * @param spark
    *   implicit SparkSession, required for Dataset encoders
    * @return
    *   the same contributors with a sequential id column added
    */
  def assignSequentialIds(
      dataset: Dataset[ContributorCount]
  )(implicit spark: SparkSession): Dataset[ContributorWithId] = {
    import spark.implicits._
    dataset
      .withColumn("id", monotonically_increasing_id().cast("int"))
      .as[ContributorWithId]
  }

  /** Pure transformation: extracts a single contributor's commit timeline from
    * an already-loaded raw commits DataFrame. Performs no I/O.
    *
    * @param rawDf
    *   the raw commits DataFrame, already read from disk
    * @param authorName
    *   the contributor's name to filter by
    * @param spark
    *   implicit SparkSession, required for Dataset encoders
    * @return
    *   the contributor's commits, ordered chronologically
    */
  def computeContributorTimeline(rawDf: DataFrame, authorName: String)(implicit
      spark: SparkSession
  ): Dataset[CommitSummary] = {
    import spark.implicits._
    rawDf
      .filter($"author_name" === authorName)
      .select(
        $"author_timestamp".cast("long").as("authorTimestamp"),
        $"hash",
        $"subject"
      )
      .as[CommitSummary]
      .orderBy($"authorTimestamp".asc)
  }
}
