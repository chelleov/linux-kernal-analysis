package il.ac.hit.functional.config

/** Trait defining the contract for loading environment configuration.
  */
trait IEnvLoader {

  /** Reads the .env file and returns a map of environment variables. Lines
    * starting with '#' and empty lines are ignored.
    *
    * @param path
    *   path to the .env file
    * @return
    *   map of key-value pairs
    */
  def load(path: String): Option[Map[String, String]]

  /** Retrieves a required environment variable, returning None if missing.
    *
    * @param key
    *   the environment variable key
    * @param env
    *   the environment map
    * @return
    *   the value wrapped in Some, or None if not found
    */
  def require(key: String, env: Map[String, String]): Option[String]
}
