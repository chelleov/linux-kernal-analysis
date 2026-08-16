package config

import java.io.File
import scala.io.Source

/** Loads key-value pairs from a .env file into a Map. */
object EnvLoader {

  /** Reads the .env file and returns a map of environment variables.
    * Lines starting with '#' and empty lines are ignored.
    */
  def load(path: String = ".env"): Map[String, String] = {
    val file = new File(path)
    if (!file.exists()) return Map.empty

    Source.fromFile(file).getLines().foldLeft(Map.empty[String, String]) { (acc, line) =>
      val trimmed = line.trim
      if (trimmed.isEmpty || trimmed.startsWith("#")) acc
      else trimmed.split("=", 2) match {
        case Array(key, value) => acc + (key.trim -> value.trim)
        case _ => acc
      }
    }
  }

  /** Retrieves a required environment variable, returning None if missing. */
  def require(key: String, env: Map[String, String]): Option[String] =
    env.get(key)
}
