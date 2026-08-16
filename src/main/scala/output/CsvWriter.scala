package output

import java.io.{File, PrintWriter}
import model.Commit

/** Writes parsed commits to a CSV file. */
object CsvWriter {

  private val Header =
    "hash,author_name,author_email,author_timestamp,committer_name,committer_email,commit_timestamp,subject"

  /** Writes commits to a CSV file at the given path.
    * Creates parent directories if they don't exist.
    */
  def write(path: String, commits: Seq[Commit]): Option[Unit] = {
    val file = new File(path)
    Option(file.getParentFile).foreach { dir =>
      if (!dir.exists()) dir.mkdirs()
    }

    val writer = new PrintWriter(file)
    try {
      writer.println(Header)
      commits.foreach(c => writer.println(formatRow(c)))
      Some(())
    } catch {
      case _: Exception => None
    } finally {
      writer.close()
    }
  }

  private def formatRow(c: Commit): String =
    s"${escape(c.hash)},${escape(c.authorName)},${escape(c.authorEmail)},${c.authorTimestamp}," +
    s"${escape(c.committerName)},${escape(c.committerEmail)},${c.commitTimestamp},${escape(c.subject)}"

  /** Escapes a value for safe CSV embedding. */
  private def escape(value: String): String =
    if (value.contains(",") || value.contains("\"") || value.contains("\n"))
      "\"" + value.replace("\"", "\"\"") + "\""
    else
      value
}
