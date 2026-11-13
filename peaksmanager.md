# PeaksManager releases for Peakeor

PeaksManager reads this file to find public Fabric JARs. It selects the most recently published release whose `minecraft` predicate matches the client. Use the exact mod `version` from `fabric.mod.json`, the GitHub release's `published_at` timestamp, the JAR download URL, and its SHA-256. Keep earlier entries so PeaksManager can recognize installed versions.

```json
{
  "schema": 1,
  "id": "peakeor-client",
  "releases": [
    {
      "minecraft": "~26.3",
      "version": "5.1.4",
      "publishedAt": "2026-11-13T21:59:00Z",
      "url": "https://github.com/Peaks2000/Peakeor/releases/download/v5.1.4/peakeor-client-5.1.4.jar",
      "sha256": "59c173e43424f90bfb6292eff992b840ec3f89abfeffc8955ac8666e1d20451b"
    },
    {
      "minecraft": "~26.3",
      "version": "26.4",
      "publishedAt": "2026-11-12T19:55:36Z",
      "url": "https://github.com/Peaks2000/Peakeor/releases/download/v26.4/peakeor-client-26.4.jar",
      "sha256": "bfc930fa1493e7d2369b26f82747efcf203cd69f12dd7496b2f7d8864fe0c029"
    },
    {
      "minecraft": "~26.3",
      "version": "26.3-5",
      "publishedAt": "2026-11-03T20:22:55Z",
      "url": "https://github.com/Peaks2000/Peakeor/releases/download/v26.3-5/peakeor-client-26.3-5.jar",
      "sha256": "13b078b57b65b5df13be10ea7f3a885576a9bc6bf68d3600288a504c3838028f"
    },
    {
      "minecraft": "~26.2",
      "version": "26.2-local",
      "publishedAt": "2026-08-28T20:41:52Z",
      "url": "https://github.com/Peaks2000/Peakeor/releases/download/v26.2-9/peakeor-client-26.2-local.jar",
      "sha256": "779c5ffc0694f4da64cb3da83ab0af25bb0b66f2c2b88e6b72e6942e6ebab2d4"
    }
  ]
}
```
