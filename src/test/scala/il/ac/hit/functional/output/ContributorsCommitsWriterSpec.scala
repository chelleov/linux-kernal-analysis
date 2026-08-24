package il.ac.hit.functional.output

import il.ac.hit.functional.model.Commit
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.File
import scala.io.Source

/** Unit tests for the ContributorsCommitsWriter class.
  */
class ContributorsCommitsWriterSpec extends AnyFlatSpec with Matchers {

  private val csvWriter: IContributorsCommitsWriter =
    ContributorsCommitsWriter()
  private val testDir = new File("test-csv")
  private val testFile = new File(testDir, "output.csv")

  private def cleanup(): Unit = {
    if (testFile.exists()) testFile.delete()
    if (testDir.exists()) testDir.delete()
  }

  private def sampleCommit(
      hash: String = "abc123",
      authorName: String = "John Doe",
      subject: String = "Test commit"
  ): Commit = Commit(
    hash = hash,
    authorName = authorName,
    authorEmail = "john@example.com",
    authorTimestamp = 1705312200L,
    committerName = "Jane Smith",
    committerEmail = "jane@example.com",
    commitTimestamp = 1705314000L,
    subject = subject,
    body = ""
  )

  "write" should "return None for null path" in {
    csvWriter.write(null, Seq.empty) shouldBe None
  }

  it should "return None for empty path" in {
    csvWriter.write("", Seq.empty) shouldBe None
  }

  it should "return None for null commits" in {
    csvWriter.write("test.csv", null) shouldBe None
  }

  it should "write header and data to file" in {
    testDir.mkdirs()
    try {
      val commits =
        Seq(sampleCommit(), sampleCommit(hash = "def456", subject = "Second"))
      val result = csvWriter.write(testFile.getPath, commits)
      result shouldBe Some(())

      val lines = Source.fromFile(testFile).getLines().toList
      lines should have size 3
      lines(0) should include("hash,author_name")
      lines(1) should include("abc123")
      lines(2) should include("def456")
    } finally {
      cleanup()
    }
  }

  it should "create parent directories if they don't exist" in {
    val nestedFile = new File("test-csv/nested/dir/output.csv")
    try {
      val result = csvWriter.write(nestedFile.getPath, Seq(sampleCommit()))
      result shouldBe Some(())
      nestedFile.exists() shouldBe true
    } finally {
      new File("test-csv/nested/dir/output.csv").delete()
      new File("test-csv/nested/dir").delete()
      new File("test-csv/nested").delete()
      cleanup()
    }
  }

  it should "escape values containing commas" in {
    testDir.mkdirs()
    try {
      val commits = Seq(sampleCommit(subject = "fix, bug"))
      csvWriter.write(testFile.getPath, commits)

      val lines = Source.fromFile(testFile).getLines().toList
      lines(1) should include("\"fix, bug\"")
    } finally {
      cleanup()
    }
  }

  it should "escape values containing quotes" in {
    testDir.mkdirs()
    try {
      val commits = Seq(sampleCommit(subject = "say \"hello\""))
      csvWriter.write(testFile.getPath, commits)

      val lines = Source.fromFile(testFile).getLines().toList
      lines(1) should include("\"say \"\"hello\"\"\"")
    } finally {
      cleanup()
    }
  }

  it should "return Some(()) for empty commits list" in {
    testDir.mkdirs()
    try {
      val result = csvWriter.write(testFile.getPath, Seq.empty)
      result shouldBe Some(())

      val lines = Source.fromFile(testFile).getLines().toList
      lines should have size 1
      lines(0) should include("hash")
    } finally {
      cleanup()
    }
  }
}
