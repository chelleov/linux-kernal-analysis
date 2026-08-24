package il.ac.hit.functional.model

/** A lightweight projection of a commit used for contributor timeline output.
  *
  * @param authorTimestamp
  *   the author timestamp as epoch seconds
  * @param hash
  *   the commit SHA hash
  * @param subject
  *   the first line of the commit message
  */
case class CommitSummary(authorTimestamp: Long, hash: String, subject: String)
