package il.ac.hit.functional

import il.ac.hit.functional.config.{EnvLoader, IEnvLoader, ISparkSessionProvider, SparkSessionProvider}
import il.ac.hit.functional.extraction.{CommitParser, GitExtractor, ICommitParser, IGitExtractor}
import il.ac.hit.functional.output.{CsvWriter, ICsvWriter, ITopContributorsWriter, TopContributorsWriter}
import il.ac.hit.functional.analysis.{ContributorAnalyzer, IContributorAnalyzer}
import scala.util.Try

/**
 * Application entry point for the Linux Kernel Analysis pipeline.
 */
object Main {

  /**
   * Main entry point. Runs the data population pipeline, followed by
   * the Spark-based contributor analysis.
   *
   * @param args command-line arguments (unused)
   */
  def main(args: Array[String]): Unit = {
    populateData()
    populateTopContributors()
  }

  /**
   * Orchestrates the full pipeline: load config, extract commits,
   * parse JSON, and write CSV output.
   */
  def populateData(): Unit = {
    val envLoader: IEnvLoader = EnvLoader()
    val commitParser: ICommitParser = CommitParser()
    val gitExtractor: IGitExtractor = GitExtractor()
    val csvWriter: ICsvWriter = CsvWriter()

    val result = for {
      env <- envLoader.load(".env")
      repo <- envLoader.require("LINUX_REPO_PATH", env)
      count <- Try(env.getOrElse("COMMITS_COUNT", "100").toInt).toOption
      raw <- gitExtractor.extractCommits(repo, count)
      commits <- commitParser.parse(raw)
      _ <- csvWriter.write("data/commits.csv", commits)
    } yield println(s"Saved ${commits.size} commits to data/commits.csv")

    if (result.isEmpty) println("Error: Pipeline failed")
  }

  /**
   * Computes the top contributors from commits.csv using Spark,
   * and writes the result to a CSV file. The number of top
   * contributors is read from the TOP_CONTRIBUTORS_COUNT environment
   * variable, defaulting to 10 if not set.
   */
  def populateTopContributors(): Unit = {
    val envLoader: IEnvLoader = EnvLoader()
    val sparkProvider: ISparkSessionProvider = SparkSessionProvider()
    val analyzer: IContributorAnalyzer = ContributorAnalyzer()
    val writer: ITopContributorsWriter = TopContributorsWriter()

    val spark = sparkProvider.create("linux-kernel-analysis")

    val result = for {
      env <- envLoader.load(".env").toRight("Could not load .env file")
      topN <- Try(env.getOrElse("TOP_CONTRIBUTORS_COUNT", "10").toInt).toOption.toRight("Invalid TOP_CONTRIBUTORS_COUNT value")
      dataset <- analyzer.topContributors(spark, "data/commits.csv", topN)
      _ <- writer.write(dataset, "data/top_contributors")
    } yield println(s"Saved top $topN contributors to data/top_contributors")

    if (result.isLeft) println(s"Error: ${result.left.get}")

    spark.stop()
  }
}