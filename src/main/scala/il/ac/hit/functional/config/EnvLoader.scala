package il.ac.hit.functional.config

import java.io.File
import scala.io.Source

/** Loads key-value pairs from a .env file into a Map.
  */
class EnvLoader extends IEnvLoader {

  /** Reads the .env file and returns a map of environment variables. Lines
    * starting with '#' and empty lines are ignored.
    *
    * @param path
    *   path to the .env file
    * @return
    *   map of key-value pairs
    */
  override def load(path: String = ".env"): Option[Map[String, String]] = {
    if (path == null || path.isEmpty) return None

    val file = new File(path)
    if (!file.exists()) return Some(Map.empty)

    val source = Source.fromFile(file)
    try {
      Some(
        source.getLines().foldLeft(Map.empty[String, String]) { (acc, line) =>
          val trimmed = line.trim
          if (trimmed.isEmpty || trimmed.startsWith("#")) acc
          else
            trimmed.split("=", 2) match {
              case Array(key, value) => acc + (key.trim -> value.trim)
              case _                 => acc
            }
        }
      )
    } finally {
      source.close()
    }
  }

  /** Retrieves a required environment variable, returning None if missing.
    *
    * @param key
    *   the environment variable key
    * @param env
    *   the environment map
    * @return
    *   the value wrapped in Some, or None if not found
    */
  override def require(
      key: String,
      env: Map[String, String]
  ): Option[String] = {
    if (key == null || key.isEmpty) return None

    env.get(key)
  }
}

/** Companion object for EnvLoader.
  */
object EnvLoader {

  /** Creates a new EnvLoader instance.
    *
    * @return
    *   a new EnvLoader
    */
  def apply(): EnvLoader = new EnvLoader()
}
