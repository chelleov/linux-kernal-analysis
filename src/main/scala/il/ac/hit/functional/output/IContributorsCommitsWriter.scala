package il.ac.hit.functional.output

import il.ac.hit.functional.model.Commit

/** Trait defining the contract for writing contributors' commits to a CSV file.
  */
trait IContributorsCommitsWriter {

  /** Writes commits to a CSV file at the given path.
    *
    * @param path
    *   the file path to write to
    * @param commits
    *   the sequence of Commit objects to write
    * @return
    *   Some(()) on success, None on failure
    */
  def write(path: String, commits: Seq[Commit]): Option[Unit]
}
