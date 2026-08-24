package il.ac.hit.functional.analysis

import il.ac.hit.functional.model.ContributorCount

/** Combinator-style predicates for filtering contributors. Simple primitive
  * predicates are combined using `and`/`or` combinators to build more complex
  * conditions, demonstrating a custom combinator (not Function1's built-in
  * andThen/compose).
  */
object ContributorFilters {

  type ContributorPredicate = ContributorCount => Boolean

  /** Primitive: contributor has at least `min` commits. */
  def minCommits(min: Long): ContributorPredicate = _.commitCount >= min

  /** Primitive: contributor's name contains the given substring. */
  def nameContains(substring: String): ContributorPredicate =
    _.authorName.toLowerCase.contains(substring.toLowerCase)

  /** Custom combinator: both predicates must hold. */
  def and(
      p1: ContributorPredicate,
      p2: ContributorPredicate
  ): ContributorPredicate =
    c => p1(c) && p2(c)

  /** Custom combinator: either predicate may hold. */
  def or(
      p1: ContributorPredicate,
      p2: ContributorPredicate
  ): ContributorPredicate =
    c => p1(c) || p2(c)
}
