package il.ac.hit.functional.model

/** Minimal typed projection of a commit row, used only for the author's name.
  *
  * @param authorName
  *   the contributor's name
  */
case class AuthorRecord(authorName: String)
