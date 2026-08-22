# linux-kernel-analysis

Analyzes the [Linux kernel](https://github.com/torvalds/linux) Git repository using Scala and Apache Spark (Dataset API).

Fetches commit history from the GitHub API, then computes the top contributors and their per-commit timelines.

## Requirements

- JDK 11+
- sbt

## Setup

Copy `.env.example` to `.env` and adjust:

| Variable | Description |
|---|---|
| `LINUX_REPO_PATH` | GitHub repo to analyze (required) |
| `COMMITS_COUNT` | Number of recent commits to fetch (default: 100) |
| `TOP_CONTRIBUTORS_COUNT` | Number of top contributors to report (default: 10) |
| `GITHUB_TOKEN` | Personal access token (optional, raises API rate limit) |

## Usage

```bash
sbt run    # writes data/commits.csv and data/top_contributors/
sbt test   # runs the test suite
```

## Output

- `data/commits.csv` — raw commit records
- `data/top_contributors/` — ranked contributor list plus one timeline CSV per contributor
