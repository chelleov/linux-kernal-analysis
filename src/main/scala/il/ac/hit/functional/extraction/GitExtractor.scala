package il.ac.hit.functional.extraction

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration
import scala.util.Try

/**
 * Fetches commit data from the GitHub REST API.
 */
class GitExtractor extends IGitExtractor {

  private val gitHubApiBase = "https://api.github.com/repos"

  /**
   * Fetches the last n commits for the given GitHub repo (owner/name).
   * Returns None on failure, Some(json) on success.
   *
   * @param repo  the GitHub repository in "owner/name" format
   * @param count the number of commits to fetch
   * @return the raw JSON response wrapped in Some, or None on failure
   */
  override def extractCommits(repo: String, count: Int): Option[String] = {
    if (repo == null || repo.isEmpty) return None
    if (count <= 0) return None

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
   */
  private def fetchPages(
      client: HttpClient,
      repo: String,
      perPage: Int,
      pages: Int,
      remaining: Int,
      acc: List[String]
  ): Option[List[String]] = {
    if (remaining <= 0 || acc.size >= pages) return Some(acc.reverse)

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

    Try(client.send(request, HttpResponse.BodyHandlers.ofString())).toOption
      .filter(_.statusCode() == 200)
      .flatMap { response =>
        val body = response.body().trim
        if (body == "[]" || body.isEmpty) Some(acc.reverse)
        else
          fetchPages(
            client,
            repo,
            perPage,
            pages,
            remaining - fetch,
            body :: acc
          )
      }
  }

  /**
   * Strips the outer JSON array brackets and merges page bodies into one JSON
   * array.
   */
  private def mergePages(pages: List[String]): String = {
    val inner = pages
      .map(_.stripPrefix("[").stripSuffix("]"))
      .mkString(",")
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
