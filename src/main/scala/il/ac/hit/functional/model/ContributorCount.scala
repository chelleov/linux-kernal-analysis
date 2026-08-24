package il.ac.hit.functional.model

/** Represents the total commit count for a single contributor.
  *
  * @param authorName
  *   the contributor's name
  * @param commitCount
  *   the total number of commits made by this contributor
  */
case class ContributorCount(authorName: String, commitCount: Long)
