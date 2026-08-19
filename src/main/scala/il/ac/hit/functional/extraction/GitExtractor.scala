package il.ac.hit.functional.extraction

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration
import scala.annotation.tailrec
import scala.util.{Try, Success, Failure}

/**
 * Fetches commit data from the GitHub REST API.
 *
 * Note: I/O and pagination logic are intentionally combined in
 * fetchPages, since each page request depends on the result of the
 * previous one. Fully separating I/O from logic here would require
 * a more advanced effect system (e.g. an IO monad), which is out of
 * scope for this project. The purely functional part of this class
 * (mergePages) is kept separate.
 */
class GitExtractor extends IGitExtractor {

  private val gitHubApiBase = "https://api.github.com/repos"

  /**
   * Fetches the last n commits for the given GitHub repo (owner/name).
   *
   * @param repo  the GitHub repository in "owner/name" format
   * @param count the number of commits to fetch
   * @return Right with the raw JSON response, or Left with an error message on failure
   */
  override def extractCommits(repo: String, count: Int): Either[String, String] = {
    if (repo == null || repo.isEmpty) return Left("repo must not be empty")
    if (count <= 0) return Left("count must be positive")

    val perPage = math.min(count, 100)
    val pages = math.ceil(count.toDouble / perPage).toInt

    val client = HttpClient
      .newBuilder()
      .connectTimeout(Duration.ofSeconds(10))
      .build()

    fetchPages(client, repo, perPage, pages, count, Nil)
      .map(mergePages)
  }

  /**
   * Recursively fetches paginated results from the GitHub API.
   * Implemented as a tail-recursive function so the call stack does
   * not grow with the number of pages fetched.
   */
  @tailrec
  private def fetchPages(
                          client: HttpClient,
                          repo: String,
                          perPage: Int,
                          pages: Int,
                          remaining: Int,
                          acc: List[String]
                        ): Either[String, List[String]] = {
    if (remaining <= 0 || acc.size >= pages) return Right(acc.reverse)

    val page = acc.size + 1
    val fetch = math.min(remaining, perPage)
    val url = s"$gitHubApiBase/$repo/commits?per_page=$fetch&page=$page"

    val request = HttpRequest
      .newBuilder()
      .uri(URI.create(url))
      .header("Accept", "application/vnd.github+json")
      .header("User-Agent", "linux-kernel-analysis")
      .GET()
      .build()

    Try(client.send(request, HttpResponse.BodyHandlers.ofString())) match {
      case Failure(exception) =>
        Left(s"Request to GitHub API failed: ${exception.getMessage}")

      case Success(response) if response.statusCode() != 200 =>
        Left(s"GitHub API returned status ${response.statusCode()} for page $page")

      case Success(response) =>
        val body = response.body().trim
        if (body == "[]" || body.isEmpty) Right(acc.reverse)
        else fetchPages(client, repo, perPage, pages, remaining - fetch, body :: acc)
    }
  }

  /**
   * Strips the outer JSON array brackets and merges page bodies into one JSON
   * array.
   */
  private def mergePages(pages: List[String]): String = {
    val inner = pages.map(_.stripPrefix("[").stripSuffix("]")).mkString(",")
    s"[$inner]"
  }
}

/**
 * Companion object for GitExtractor.
 */
object GitExtractor {

  /**
   * Creates a new GitExtractor instance.
   *
   * @return a new GitExtractor
   */
  def apply(): GitExtractor = new GitExtractor()
}