package il.ac.hit.functional.extraction

/**
 * Trait defining the contract for fetching commit data from the GitHub REST API.
 */
trait IGitExtractor {

  /**
   * Fetches the last n commits for the given GitHub repo (owner/name).
   *
   * @param repo  the GitHub repository in "owner/name" format
   * @param count the number of commits to fetch
   * @return the raw JSON response wrapped in Some, or None on failure
   */
  def extractCommits(repo: String, count: Int): Option[String]
}
