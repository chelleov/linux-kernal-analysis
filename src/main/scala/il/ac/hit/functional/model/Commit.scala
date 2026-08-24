package il.ac.hit.functional.model

/** Domain model representing a single Git commit.
  *
  * @param hash
  *   the commit SHA hash
  * @param authorName
  *   the name of the commit author
  * @param authorEmail
  *   the email of the commit author
  * @param authorTimestamp
  *   the author timestamp as epoch seconds
  * @param committerName
  *   the name of the committer
  * @param committerEmail
  *   the email of the committer
  * @param commitTimestamp
  *   the committer timestamp as epoch seconds
  * @param subject
  *   the first line of the commit message
  * @param body
  *   the remaining lines of the commit message
  */
case class Commit(
    hash: String,
    authorName: String,
    authorEmail: String,
    authorTimestamp: Long,
    committerName: String,
    committerEmail: String,
    commitTimestamp: Long,
    subject: String,
    body: String
)
