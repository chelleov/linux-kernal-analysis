package il.ac.hit.functional.extraction

import il.ac.hit.functional.model.Commit

/** Trait defining the contract for parsing GitHub API commit JSON.
  */
trait ICommitParser {

  /** Parses a JSON array string into a sequence of Commits. Malformed entries
    * are silently dropped.
    *
    * @param json
    *   the raw JSON string from the GitHub API
    * @return
    *   a sequence of parsed Commit objects
    */
  def parse(json: String): Option[Seq[Commit]]
}
