package model

case class Commit(
    hash: String,
    authorName: String,
    authorEmail: String,
    authorTimestamp: Long,
    committerName: String,
    committerEmail: String,
    commitTimestamp: Long,
    subject: String,
    body: String
)
