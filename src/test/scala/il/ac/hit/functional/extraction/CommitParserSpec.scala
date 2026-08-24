package il.ac.hit.functional.extraction

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Unit tests for the CommitParser class.
  */
class CommitParserSpec extends AnyFlatSpec with Matchers {

  private val commitParser: ICommitParser = CommitParser()

  private val validCommitJson =
    """[
      |  {
      |    "sha": "abc123",
      |    "commit": {
      |      "message": "Subject line\n\nBody paragraph",
      |      "author": {
      |        "name": "John Doe",
      |        "email": "john@example.com",
      |        "date": "2024-01-15T10:30:00Z"
      |      },
      |      "committer": {
      |        "name": "Jane Smith",
      |        "email": "jane@example.com",
      |        "date": "2024-01-15T11:00:00Z"
      |      }
      |    }
      |  }
      |]""".stripMargin

  "parse" should "return None for null input" in {
    commitParser.parse(null) shouldBe None
  }

  it should "return None for empty string" in {
    commitParser.parse("") shouldBe None
  }

  it should "parse a valid single commit" in {
    val result = commitParser.parse(validCommitJson)
    result shouldBe defined
    result.get should have size 1

    val commit = result.get.head
    commit.hash shouldBe "abc123"
    commit.authorName shouldBe "John Doe"
    commit.authorEmail shouldBe "john@example.com"
    commit.committerName shouldBe "Jane Smith"
    commit.committerEmail shouldBe "jane@example.com"
    commit.subject shouldBe "Subject line"
    commit.body shouldBe "Body paragraph"
  }

  it should "split message into subject and body correctly" in {
    val json =
      """[{
        |  "sha": "def456",
        |  "commit": {
        |    "message": "Only subject",
        |    "author": {"name": "A", "email": "a@b.com", "date": "2024-01-01T00:00:00Z"},
        |    "committer": {"name": "B", "email": "b@b.com", "date": "2024-01-01T00:00:00Z"}
        |  }
        |}]""".stripMargin
    val result = commitParser.parse(json)
    result.get.head.subject shouldBe "Only subject"
    result.get.head.body shouldBe ""
  }

  it should "drop malformed entries silently" in {
    val json =
      """[
        |  {
        |    "sha": "valid123",
        |    "commit": {
        |      "message": "Valid commit",
        |      "author": {"name": "A", "email": "a@b.com", "date": "2024-01-01T00:00:00Z"},
        |      "committer": {"name": "B", "email": "b@b.com", "date": "2024-01-01T00:00:00Z"}
        |    }
        |  },
        |  {
        |    "sha": "missing_commit"
        |  }
        |]""".stripMargin
    val result = commitParser.parse(json)
    result shouldBe defined
    result.get should have size 1
    result.get.head.hash shouldBe "valid123"
  }

  it should "parse timestamps into epoch seconds" in {
    val result = commitParser.parse(validCommitJson)
    val commit = result.get.head
    commit.authorTimestamp shouldBe 1705314600L
    commit.commitTimestamp shouldBe 1705316400L
  }
}
