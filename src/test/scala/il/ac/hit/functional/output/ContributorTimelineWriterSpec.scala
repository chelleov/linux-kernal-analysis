package il.ac.hit.functional.output

import il.ac.hit.functional.analysis.{ContributorAnalyzer, IContributorAnalyzer}
import il.ac.hit.functional.model.ContributorWithId
import org.apache.spark.sql.{Dataset, Encoder, Encoders, SparkSession}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.{File, PrintWriter}

/** Unit tests for the ContributorTimelineWriter class using a local Spark
  * session.
  */
class ContributorTimelineWriterSpec
    extends AnyFlatSpec
    with Matchers
    with BeforeAndAfterAll {

  private var spark: SparkSession = _
  private val analyzer: IContributorAnalyzer = ContributorAnalyzer()
  private val writer: IContributorTimelineWriter = ContributorTimelineWriter()
  private val testDir = new File("test-timeline-writer")
  private val testCsv = new File(testDir, "commits.csv")
  private val outputDir = new File(testDir, "output")

  override def beforeAll(): Unit = {
    spark = SparkSession
      .builder()
      .appName("ContributorTimelineWriterSpec")
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
    } finally {
      writer.close()
    }
  }

  private def cleanup(): Unit = {
    deleteRecursive(outputDir)
    if (testCsv.exists()) testCsv.delete()
    if (testDir.exists()) testDir.delete()
  }

  private def deleteRecursive(file: File): Unit = {
    if (file.isDirectory) {
      file.listFiles().foreach(deleteRecursive)
    }
    file.delete()
  }

  private def createContributorDs(
      contributors: (String, Long, Int)*
  ): Dataset[ContributorWithId] = {
    val encoder: Encoder[ContributorWithId] =
      Encoders.product[ContributorWithId]
    spark.createDataset(contributors.map { case (name, count, id) =>
      ContributorWithId(name, count, id)
    })(encoder)
  }

  "write" should "return Left for null contributors" in {
    writer.write(null, "any.csv", spark, analyzer, "out") shouldBe Left(
      "contributors must not be null"
    )
  }

  it should "return Left for empty csvPath" in {
    val ds = createContributorDs(("Alice", 2L, 0))
    writer.write(ds, "", spark, analyzer, "out") shouldBe Left(
      "csvPath must not be empty"
    )
  }

  it should "return Left for null spark" in {
    val ds = createContributorDs(("Alice", 2L, 0))
    writer.write(ds, "any.csv", null, analyzer, "out") shouldBe Left(
      "spark session must not be null"
    )
  }

  it should "return Left for null analyzer" in {
    val ds = createContributorDs(("Alice", 2L, 0))
    writer.write(ds, "any.csv", spark, null, "out") shouldBe Left(
      "analyzer must not be null"
    )
  }

  it should "return Left for empty basePath" in {
    val ds = createContributorDs(("Alice", 2L, 0))
    writer.write(ds, "any.csv", spark, analyzer, "") shouldBe Left(
      "basePath must not be empty"
    )
  }

  it should "create timeline CSV files for each contributor" in {
    val ds = createContributorDs(("Alice", 2L, 0), ("Bob", 1L, 1))
    val basePath = outputDir.getPath

    val result = writer.write(ds, testCsv.getPath, spark, analyzer, basePath)
    result shouldBe Right(())

    // Check that output directories were created
    val output0 = new File(basePath, "0")
    val output1 = new File(basePath, "1")
    output0.exists() shouldBe true
    output1.exists() shouldBe true

    // Check that part files exist inside each directory
    output0.listFiles().exists(_.getName.startsWith("part-")) shouldBe true
    output1.listFiles().exists(_.getName.startsWith("part-")) shouldBe true
  }

  it should "return Right for empty contributors dataset" in {
    val ds = createContributorDs()
    val basePath = outputDir.getPath

    val result = writer.write(ds, testCsv.getPath, spark, analyzer, basePath)
    result shouldBe Right(())
  }
}
