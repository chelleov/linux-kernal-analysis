package il.ac.hit.functional.extraction

import il.ac.hit.functional.model.Commit
import spray.json._
import DefaultJsonProtocol._

/** Parses GitHub API commit JSON into Commit case classes.
  */
class CommitParser extends ICommitParser {

  /** Parses a JSON array string into a sequence of Commits. Malformed entries
    * are silently dropped (parseCommit returns None).
    *
    * @param json
    *   the raw JSON string from the GitHub API
    * @return
    *   a sequence of parsed Commit objects
    */
  override def parse(json: String): Option[Seq[Commit]] = {
    if (json == null || json.isEmpty) return None

    Some(json.parseJson.convertTo[JsArray].elements.flatMap(parseCommit).toSeq)
  }

  /** Attempts to parse a single JSON commit object. Returns None if any
    * required field is missing or malformed.
    */
  private def parseCommit(elem: JsValue): Option[Commit] = {
    // Walk the nested GitHub commit JSON shape: sha at top level, the
    // message under "commit", person objects under commit/author and
    // commit/committer. Every step yields None when a field is missing,
    // so any gap fails this entry's parse (and it gets dropped upstream).
    for {
      sha <- elem.asJsObject.fields.get("sha").map(_.convertTo[String])
      commitObj <- elem.asJsObject.fields.get("commit").map(_.asJsObject)
      message <- commitObj.fields.get("message").map(_.convertTo[String])
      authorObj <- commitObj.fields.get("author").map(_.asJsObject)
      committerObj <- commitObj.fields.get("committer").map(_.asJsObject)
    } yield {
      val (subject, body) = splitMessage(message)
      // name/email/date are mandatory sub-fields of author/committer
      // objects, so direct access is safe once those objects exist
      Commit(
        hash = sha,
        authorName = authorObj.fields("name").convertTo[String],
        authorEmail = authorObj.fields("email").convertTo[String],
        authorTimestamp =
          parseTimestamp(authorObj.fields("date").convertTo[String]),
        committerName = committerObj.fields("name").convertTo[String],
        committerEmail = committerObj.fields("email").convertTo[String],
        commitTimestamp =
          parseTimestamp(committerObj.fields("date").convertTo[String]),
        subject = subject,
        body = body
      )
    }
  }

  /** Splits a commit message into subject (first line) and body (rest).
    */
  private def splitMessage(message: String): (String, String) =
    message.split("\n", 2) match {
      case Array(subject, body) => (subject, body.trim)
      case Array(subject)       => (subject, "")
    }

  /** Parses an ISO-8601 timestamp string into epoch seconds.
    */
  private def parseTimestamp(date: String): Long =
    java.time.Instant.parse(date).getEpochSecond
}

/** Companion object for CommitParser.
  */
object CommitParser {

  /** Creates a new CommitParser instance.
    *
    * @return
    *   a new CommitParser
    */
  def apply(): CommitParser = new CommitParser()
}
