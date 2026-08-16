package extraction

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration
import scala.util.Try

/** Fetches commit data from the GitHub REST API. */
object GitExtractor {

  private val GitHubApiBase = "https://api.github.com/repos"

  /** Fetches the last `count` commits for the given GitHub repo (owner/name).
    * Returns None on failure, Some(json) on success.
    */
  def extractCommits(repo: String, count: Int): Option[String] = {
    val perPage = math.min(count, 100)
    val pages = math.ceil(count.toDouble / perPage).toInt

    val client = HttpClient
      .newBuilder()
      .connectTimeout(Duration.ofSeconds(10))
      .build()

    fetchPages(client, repo, perPage, pages, count, Nil)
      .map(mergePages)
  }

  /** Recursively fetches paginated results from the GitHub API. */
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
    val url = s"$GitHubApiBase/$repo/commits?per_page=$fetch&page=$page"

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

  /** Strips the outer JSON array brackets and merges page bodies into one JSON
    * array.
    */
  private def mergePages(pages: List[String]): String = {
    val inner = pages
      .map(_.stripPrefix("[").stripSuffix("]"))
      .mkString(",")
    s"[$inner]"
  }
}
