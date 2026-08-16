import config.EnvLoader
import extraction.{CommitParser, GitExtractor}
import output.CsvWriter
import scala.util.Try

object Main {

  def main(args: Array[String]): Unit = {
    PopulateData()
  }

  def PopulateData(): Unit = {
    val env = EnvLoader.load()
    val result = for {
      repo <- EnvLoader.require("LINUX_REPO_PATH", env)
      count <- Try(env.getOrElse("COMMITS_COUNT", "100").toInt).toOption
      raw <- GitExtractor.extractCommits(repo, count)
      commits = CommitParser.parse(raw)
      _ <- CsvWriter.write("data/commits.csv", commits)
    } yield println(s"Saved ${commits.size} commits to data/commits.csv")

    if (result.isEmpty) println("Error: Pipeline failed")
  }
}
