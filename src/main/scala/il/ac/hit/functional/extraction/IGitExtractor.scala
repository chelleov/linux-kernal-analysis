package il.ac.hit.functional.extraction

/**
 * Trait defining the contract for fetching commit data from GitHub.
 */
trait IGitExtractor {

  /**
   * Fetches the last n commits for the given GitHub repo (owner/name).
   *
   * @param repo  the GitHub repository in "owner/name" format
   * @param count the number of commits to fetch
   * @return Right with the raw JSON response, or Left with an error message on failure
   */
  def extractCommits(repo: String, count: Int): Either[String, String]
}