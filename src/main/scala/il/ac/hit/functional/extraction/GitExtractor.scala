package il.ac.hit.functional.extraction

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration
import scala.annotation.tailrec
import scala.util.{Try, Success, Failure}

/**
 * Fetches commit data from the GitHub REST API.
 */
class GitExtractor extends IGitExtractor {

  private val gitHubApiBase = "https://api.github.com/repos"

  override def extractCommits(repo: String, count: Int, token: Option[String] = None): Either[String, String] = {
    if (repo == null || repo.isEmpty) return Left("repo must not be empty")
    if (count <= 0) return Left("count must be positive")

    val perPage = math.min(count, 100)
    val pages = math.ceil(count.toDouble / perPage).toInt

    val client = HttpClient
      .newBuilder()
      .connectTimeout(Duration.ofSeconds(10))
      .build()

    fetchPages(client, repo, perPage, pages, count, token, Nil)
      .map(mergePages)
  }

  @tailrec
  private def fetchPages(
                          client: HttpClient,
                          repo: String,
                          perPage: Int,
                          pages: Int,
                          remaining: Int,
                          token: Option[String],
                          acc: List[String]
                        ): Either[String, List[String]] = {
    if (remaining <= 0 || acc.size >= pages) return Right(acc.reverse)

    val page = acc.size + 1
    val fetch = math.min(remaining, perPage)
    val url = s"$gitHubApiBase/$repo/commits?per_page=$fetch&page=$page"

    val baseBuilder = HttpRequest
      .newBuilder()
      .uri(URI.create(url))
      .header("Accept", "application/vnd.github+json")
      .header("User-Agent", "linux-kernel-analysis")

    val requestBuilder = token match {
      case Some(t) => baseBuilder.header("Authorization", s"Bearer $t")
      case None    => baseBuilder
    }

    val request = requestBuilder.GET().build()

    Try(client.send(request, HttpResponse.BodyHandlers.ofString())) match {
      case Failure(exception) =>
        Left(s"Request to GitHub API failed: ${exception.getMessage}")

      case Success(response) if response.statusCode() != 200 =>
        Left(s"GitHub API returned status ${response.statusCode()} for page $page")

      case Success(response) =>
        val body = response.body().trim
        if (body == "[]" || body.isEmpty) Right(acc.reverse)
        else fetchPages(client, repo, perPage, pages, remaining - fetch, token, body :: acc)
    }
  }

  private def mergePages(pages: List[String]): String = {
    val inner = pages.map(_.stripPrefix("[").stripSuffix("]")).mkString(",")
    s"[$inner]"
  }
}

/**
 * Companion object for GitExtractor.
 */
object GitExtractor {
  def apply(): GitExtractor = new GitExtractor()
}