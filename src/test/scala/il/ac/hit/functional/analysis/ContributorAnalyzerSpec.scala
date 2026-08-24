package il.ac.hit.functional.analysis

import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.{File, PrintWriter}

/** Unit tests for the ContributorAnalyzer's topContributorsWithId and
  * contributorTimeline methods using a local Spark session.
  */
class ContributorAnalyzerSpec
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterAll {

  private var spark: SparkSession = _
  private val analyzer: IContributorAnalyzer = ContributorAnalyzer()
  private val testDir = new File("test-analyzer")
  private val testCsv = new File(testDir, "commits.csv")

  override def beforeAll(): Unit = {
    spark = SparkSession
      .builder()
      .appName("ContributorAnalyzerSpec")
      .master("local[*]")
      .getOrCreate()
    testDir.mkdirs()
    writeTestCsv()
  }

  override def afterAll(): Unit = {
    cleanup()
  }

  private def writeTestCsv(): Unit = {
    val writer = new PrintWriter(testCsv)
    try {
      writer.println(
        "hash,author_name,author_email,author_timestamp,committer_name,committer_email,commit_timestamp,subject"
      )
      writer.println(
        "aaa111,Alice,alice@example.com,1000,Bob,bob@example.com,1000,First commit"
      )
      writer.println(
        "bbb222,Bob,bob@example.com,2000,Alice,alice@example.com,2000,Second commit"
      )
      writer.println(
        "ccc333,Alice,alice@example.com,3000,Bob,bob@example.com,3000,Third commit"
      )
      writer.println(
        "ddd444,Charlie,charlie@example.com,4000,Bob,bob@example.com,4000,Fourth commit"
      )
      writer.println(
        "eee555,Alice,alice@example.com,5000,Bob,bob@example.com,5000,Fifth commit"
      )
      writer.println(
        "fff666,Bob,bob@example.com,6000,Alice,alice@example.com,6000,Sixth commit"
      )
    } finally {
      writer.close()
    }
  }

  private def cleanup(): Unit = {
    if (testCsv.exists()) testCsv.delete()
    if (testDir.exists()) testDir.delete()
  }

  "topContributorsWithId" should "return Left for null spark" in {
    analyzer.topContributorsWithId(null, "any.csv", 10) shouldBe Left(
      "spark session must not be null"
    )
  }

  it should "return Left for empty csvPath" in {
    analyzer.topContributorsWithId(spark, "", 10) shouldBe Left(
      "csvPath must not be empty"
    )
  }

  it should "return Left for non-positive topN" in {
    analyzer.topContributorsWithId(spark, testCsv.getPath, 0) shouldBe Left(
      "topN must be positive"
    )
  }

  it should "assign sequential IDs starting from 0" in {
    val result = analyzer.topContributorsWithId(spark, testCsv.getPath, 3)
    result shouldBe a[Right[_, _]]

    val dataset = result.getOrElse(fail("Expected Right"))
    val collected = dataset.collect()
    collected should have size 3

    collected.map(_.id).sorted shouldBe Array(0, 1, 2)
  }

  it should "order contributors by commit count descending" in {
    val result = analyzer.topContributorsWithId(spark, testCsv.getPath, 3)
    val collected = result.getOrElse(fail("Expected Right")).collect()

    collected(0).authorName shouldBe "Alice"
    collected(0).commitCount shouldBe 3L
    collected(1).authorName shouldBe "Bob"
    collected(1).commitCount shouldBe 2L
    collected(2).authorName shouldBe "Charlie"
    collected(2).commitCount shouldBe 1L
  }

  "contributorTimeline" should "return Left for null spark" in {
    analyzer.contributorTimeline(null, "any.csv", "Alice") shouldBe Left(
      "spark session must not be null"
    )
  }

  it should "return Left for empty csvPath" in {
    analyzer.contributorTimeline(spark, "", "Alice") shouldBe Left(
      "csvPath must not be empty"
    )
  }

  it should "return Left for empty authorName" in {
    analyzer.contributorTimeline(spark, testCsv.getPath, "") shouldBe Left(
      "authorName must not be empty"
    )
  }

  it should "return commits sorted by timestamp for a given author" in {
    val result = analyzer.contributorTimeline(spark, testCsv.getPath, "Alice")
    result shouldBe a[Right[_, _]]

    val collected = result.getOrElse(fail("Expected Right")).collect()
    collected should have size 3

    collected.map(_.hash) shouldBe Array("aaa111", "ccc333", "eee555")
    collected.map(_.authorTimestamp) shouldBe Array(1000L, 3000L, 5000L)
  }

  it should "return empty dataset for author with no commits" in {
    val result = analyzer.contributorTimeline(spark, testCsv.getPath, "Nobody")
    result shouldBe a[Right[_, _]]

    result.getOrElse(fail("Expected Right")).collect() shouldBe empty
  }
}
