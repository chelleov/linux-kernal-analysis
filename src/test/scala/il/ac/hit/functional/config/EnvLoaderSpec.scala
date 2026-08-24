package il.ac.hit.functional.config

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.io.{File, PrintWriter}

/** Unit tests for the EnvLoader class.
  */
class EnvLoaderSpec extends AnyFlatSpec with Matchers {

  private val envLoader: IEnvLoader = EnvLoader()
  private val testDir = new File("test-env")
  private val testFile = new File(testDir, "test.env")

  private def createEnvFile(content: String): Unit = {
    testDir.mkdirs()
    val writer = new PrintWriter(testFile)
    writer.print(content)
    writer.close()
  }

  private def cleanupEnvFile(): Unit = {
    if (testFile.exists()) testFile.delete()
    if (testDir.exists()) testDir.delete()
  }

  "load" should "return None for null path" in {
    envLoader.load(null) shouldBe None
  }

  it should "return None for empty path" in {
    envLoader.load("") shouldBe None
  }

  it should "return Some(Map.empty) for non-existent file" in {
    envLoader.load("nonexistent.env") shouldBe Some(Map.empty)
  }

  it should "parse key-value pairs correctly" in {
    createEnvFile("KEY1=VALUE1\nKEY2=VALUE2")
    try {
      val result = envLoader.load(testFile.getPath)
      result shouldBe Some(Map("KEY1" -> "VALUE1", "KEY2" -> "VALUE2"))
    } finally {
      cleanupEnvFile()
    }
  }

  it should "ignore comments and empty lines" in {
    createEnvFile("# comment\n\nKEY1=VALUE1\n# another comment\nKEY2=VALUE2\n")
    try {
      val result = envLoader.load(testFile.getPath)
      result shouldBe Some(Map("KEY1" -> "VALUE1", "KEY2" -> "VALUE2"))
    } finally {
      cleanupEnvFile()
    }
  }

  it should "handle values with equals signs" in {
    createEnvFile("KEY=VALUE=WITH=EQUALS")
    try {
      val result = envLoader.load(testFile.getPath)
      result shouldBe Some(Map("KEY" -> "VALUE=WITH=EQUALS"))
    } finally {
      cleanupEnvFile()
    }
  }

  it should "trim whitespace from keys and values" in {
    createEnvFile("  KEY1  =  VALUE1  ")
    try {
      val result = envLoader.load(testFile.getPath)
      result shouldBe Some(Map("KEY1" -> "VALUE1"))
    } finally {
      cleanupEnvFile()
    }
  }

  "require" should "return None for null key" in {
    envLoader.require(null, Map("KEY" -> "VALUE")) shouldBe None
  }

  it should "return None for empty key" in {
    envLoader.require("", Map("KEY" -> "VALUE")) shouldBe None
  }

  it should "return Some(value) when key exists" in {
    envLoader.require("KEY", Map("KEY" -> "VALUE")) shouldBe Some("VALUE")
  }

  it should "return None when key does not exist" in {
    envLoader.require("MISSING", Map("KEY" -> "VALUE")) shouldBe None
  }
}
