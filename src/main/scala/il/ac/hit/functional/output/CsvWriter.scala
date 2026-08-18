package il.ac.hit.functional.output

import il.ac.hit.functional.model.Commit
import java.io.{File, PrintWriter}

/**
 * Writes parsed commits to a CSV file.
 */
class CsvWriter extends ICsvWriter {

  private val header =
    "hash,author_name,author_email,author_timestamp,committer_name,committer_email,commit_timestamp,subject"

  /**
   * Writes commits to a CSV file at the given path.
   * Creates parent directories if they don't exist.
   *
   * @param path    the file path to write to
   * @param commits the sequence of Commit objects to write
   * @return Some(()) on success, None on failure
   */
  override def write(path: String, commits: Seq[Commit]): Option[Unit] = {
    if (path == null || path.isEmpty) return None
    if (commits == null) return None

    val file = new File(path)
    Option(file.getParentFile).foreach { dir =>
      if (!dir.exists()) dir.mkdirs()
    }

    val writer = new PrintWriter(file)
    try {
      writer.println(header)
      commits.foreach(c => writer.println(formatRow(c)))
      Some(())
    } catch {
      case _: Exception => None
    } finally {
      writer.close()
    }
  }

  /**
   * Formats a single Commit as a CSV row.
   */
  private def formatRow(c: Commit): String =
    s"${escape(c.hash)},${escape(c.authorName)},${escape(c.authorEmail)},${c.authorTimestamp}," +
    s"${escape(c.committerName)},${escape(c.committerEmail)},${c.commitTimestamp},${escape(c.subject)}"

  /**
   * Escapes a value for safe CSV embedding.
   */
  private def escape(value: String): String =
    if (value.contains(",") || value.contains("\"") || value.contains("\n"))
      "\"" + value.replace("\"", "\"\"") + "\""
    else
      value
}

/**
 * Companion object for CsvWriter.
 */
object CsvWriter {

  /**
   * Creates a new CsvWriter instance.
   *
   * @return a new CsvWriter
   */
  def apply(): CsvWriter = new CsvWriter()
}
