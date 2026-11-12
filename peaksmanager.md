# PeaksManager releases for Peakeor

PeaksManager reads the JSON block below to find compatible public Fabric JARs. PeaksManager selects the highest matching mod version. If versions are equal, it uses the first matching entry. Use a Fabric version predicate for `minecraft`, the exact version from `fabric.mod.json` for `version`, and the SHA-256 of the GitHub release asset.

```json
{
  "schema": 1,
  "id": "peakeor-client",
  "releases": [
    {
      "minecraft": "~26.3",
      "version": "26.4",
      "url": "https://github.com/Peaks2000/Peakeor/releases/download/v26.4/peakeor-client-26.4.jar",
      "sha256": "bfc930fa1493e7d2369b26f82747efcf203cd69f12dd7496b2f7d8864fe0c029"
    },
    {
      "minecraft": "~26.2",
      "version": "26.2-local",
      "url": "https://github.com/Peaks2000/Peakeor/releases/download/v26.2-9/peakeor-client-26.2-local.jar",
      "sha256": "779c5ffc0694f4da64cb3da83ab0af25bb0b66f2c2b88e6b72e6942e6ebab2d4"
    }
  ]
}
```
