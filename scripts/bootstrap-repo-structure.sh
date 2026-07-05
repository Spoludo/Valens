#!/usr/bin/env bash
set -euo pipefail

mkdir -p app
mkdir -p docs/ADR
mkdir -p exercise-packs/isometric-foundations/exercises
mkdir -p exercise-packs/isometric-foundations/assets
mkdir -p assets/{muscles,joints,illustrations,sounds}
mkdir -p external
mkdir -p .github/{workflows,ISSUE_TEMPLATE}

touch assets/muscles/.gitkeep
touch assets/joints/.gitkeep
touch assets/illustrations/.gitkeep
touch assets/sounds/.gitkeep
touch external/.gitkeep

cat > exercise-packs/isometric-foundations/exercise-pack.json <<'JSON'
{
  "id": "isometric-foundations",
  "version": "0.1.0",
  "name": "Isometric Foundations",
  "schemaVersion": "0.1.0"
}
JSON

cat > exercise-packs/isometric-foundations/movement-patterns.json <<'JSON'
{
  "patterns": []
}
JSON

echo "Valens repository structure initialized."