package il.ac.hit.functional

import il.ac.hit.functional.config.{EnvLoader, IEnvLoader}
import il.ac.hit.functional.extraction.{CommitParser, GitExtractor, ICommitParser, IGitExtractor}
import il.ac.hit.functional.output.{CsvWriter, ICsvWriter}
import scala.util.Try

/**
 * Application entry point for the Linux Kernel Analysis pipeline.
 */
object Main {

  /**
   * Main entry point. Launches the data population pipeline.
   *
   * @param args command-line arguments (unused)
   */
  def main(args: Array[String]): Unit = {
    populateData()
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
}
