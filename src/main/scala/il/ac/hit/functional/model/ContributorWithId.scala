package il.ac.hit.functional.model

/** Represents a top contributor with a sequential numeric ID.
  *
  * @param authorName
  *   the contributor's name
  * @param commitCount
  *   the total number of commits made by this contributor
  * @param id
  *   a sequential identifier (0 to n-1) assigned to the contributor
  */
case class ContributorWithId(authorName: String, commitCount: Long, id: Int)
