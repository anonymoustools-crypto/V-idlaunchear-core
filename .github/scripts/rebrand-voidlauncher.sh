#!/usr/bin/env bash
set -euo pipefail

EXPECTED_BRANCH="feature/void-roadmap-completion"
OLD_PACKAGE="com.movtery.zalithlauncher"
NEW_PACKAGE="net.voidhub.voidlauncher"
OLD_PACKAGE_PATH="com/movtery/zalithlauncher"
NEW_PACKAGE_PATH="net/voidhub/voidlauncher"

if [[ "${GITHUB_REF_NAME:-$(git branch --show-current)}" != "$EXPECTED_BRANCH" ]]; then
  echo "Refusing to migrate outside $EXPECTED_BRANCH"
  exit 1
fi

python3 - <<'PY'
import os
import subprocess
from pathlib import Path

old_path = b"com/movtery/zalithlauncher"
new_path = b"net/voidhub/voidlauncher"
tracked = subprocess.check_output(["git", "ls-files", "-z"]).split(b"\0")
matches = [p for p in tracked if p and old_path in p]
for raw in matches:
    source = os.fsdecode(raw)
    target = source.replace(old_path.decode(), new_path.decode())
    Path(target).parent.mkdir(parents=True, exist_ok=True)
    subprocess.check_call(["git", "mv", source, target])
print(f"Moved {len(matches)} source files into the Vøid package path.")
PY

if [[ -d "ZalithLauncher" ]]; then
  git mv ZalithLauncher VoidLauncher
fi

python3 - <<'PY'
import subprocess
from pathlib import Path

replacements = [
    (b"com.movtery.zalithlauncher", b"net.voidhub.voidlauncher"),
    (b"ZalithLauncher", b"VoidLauncher"),
    (b"zalith_launcher.jks", b"void_launcher.jks"),
    (b"ZLApplication", b"VoidApplication"),
]
excluded = [
    ":(exclude).github/scripts/rebrand-voidlauncher.sh",
    ":(exclude).github/workflows/rebrand-voidlauncher.yml",
]
for old, new in replacements:
    result = subprocess.run(
        ["git", "grep", "-Ilz", "-F", old.decode(), "--", ".", *excluded],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    if result.returncode not in (0, 1):
        raise SystemExit(result.stderr.decode(errors="replace"))
    files = [p for p in result.stdout.split(b"\0") if p]
    for raw in files:
        path = Path(raw.decode())
        data = path.read_bytes()
        updated = data.replace(old, new)
        if updated != data:
            path.write_bytes(updated)
    print(f"Replaced {old.decode()} in {len(files)} tracked text files.")

old_class = Path("VoidLauncher/src/main/java/net/voidhub/voidlauncher/VoidApplication.kt")
if old_class.exists():
    # The class was renamed in-place above; its filename should match the class.
    pass
PY

git add -A
if git diff --cached --quiet; then
  echo "No rebrand changes were necessary."
  exit 0
fi

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git commit -m "refactor: migrate launcher package and module to Vøid identity"
git push origin "HEAD:$EXPECTED_BRANCH"
