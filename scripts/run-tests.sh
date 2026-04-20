#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

PRIORITY="all"
SCOPE="all"
DO_CLEAN="true"

usage() {
  cat <<'EOF'
Usage:
  ./scripts/run-tests.sh [all|p0|p1] [--unit-only|--integration-only] [--no-clean]

Examples:
  ./scripts/run-tests.sh
  ./scripts/run-tests.sh p0
  ./scripts/run-tests.sh p1 --unit-only
  ./scripts/run-tests.sh all --integration-only --no-clean

Options:
  all|p0|p1          Priority level (default: all)
  --unit-only        Only run unit tests (exclude *IntegrationTest)
  --integration-only Only run integration tests (*IntegrationTest)
  --no-clean         Skip Maven clean step
  -h, --help         Show this help message
EOF
}

if [[ $# -gt 0 && "$1" != -* ]]; then
  PRIORITY="$(printf '%s' "$1" | tr '[:upper:]' '[:lower:]')"
  shift
fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --unit-only)
      SCOPE="unit"
      shift
      ;;
    --integration-only)
      SCOPE="integration"
      shift
      ;;
    --no-clean)
      DO_CLEAN="false"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $1"
      usage
      exit 1
      ;;
  esac
done

case "$PRIORITY" in
  all|p0|p1)
    ;;
  *)
    echo "Unsupported priority: $PRIORITY"
    usage
    exit 1
    ;;
esac

if [[ -x "$PROJECT_ROOT/mvnw" ]]; then
  MVN_CMD=("$PROJECT_ROOT/mvnw")
else
  if ! command -v mvn >/dev/null 2>&1; then
    echo "mvn command not found. Please install Maven or add mvnw wrapper."
    exit 1
  fi
  MVN_CMD=("mvn")
fi

MVN_ARGS=()
if [[ "$DO_CLEAN" == "true" ]]; then
  MVN_ARGS+=("clean")
fi
MVN_ARGS+=("test")

case "$PRIORITY" in
  p0)
    MVN_ARGS+=("-Dgroups=P0")
    ;;
  p1)
    MVN_ARGS+=("-Dgroups=P1")
    ;;
  all)
    ;;
esac

case "$SCOPE" in
  unit)
    MVN_ARGS+=("-Dtest=*Test,!*IntegrationTest")
    ;;
  integration)
    MVN_ARGS+=("-Dtest=*IntegrationTest")
    ;;
  all)
    ;;
esac

TIMESTAMP="$(date +"%Y%m%d_%H%M%S")"
REPORT_BASE="$PROJECT_ROOT/target/test-reports"
RUN_DIR="$REPORT_BASE/${TIMESTAMP}_${PRIORITY}_${SCOPE}"
RUN_LOG="$RUN_DIR/mvn-test.log"

mkdir -p "$RUN_DIR"

echo "Project root : $PROJECT_ROOT"
echo "Priority     : $PRIORITY"
echo "Scope        : $SCOPE"
echo "Maven args   : ${MVN_ARGS[*]}"
echo "Run log      : $RUN_LOG"

cd "$PROJECT_ROOT"

set +e
"${MVN_CMD[@]}" "${MVN_ARGS[@]}" | tee "$RUN_LOG"
MVN_EXIT_CODE=${PIPESTATUS[0]}
set -e

if [[ -d "$PROJECT_ROOT/target/surefire-reports" ]]; then
  cp -R "$PROJECT_ROOT/target/surefire-reports" "$RUN_DIR/"
fi

extract_attr() {
  local line="$1"
  local attr="$2"
  local regex="${attr}=\"([0-9]+)\""
  if [[ "$line" =~ $regex ]]; then
    echo "${BASH_REMATCH[1]}"
  else
    echo 0
  fi
}

write_summary() {
  local report_dir="$1"
  local summary_file="$2"
  local total=0
  local failures=0
  local errors=0
  local skipped=0

  if [[ ! -d "$report_dir" ]]; then
    cat > "$summary_file" <<EOF
status=NO_REPORT
message=Surefire report directory not found.
EOF
    return
  fi

  local suites
  suites="$(grep -h '<testsuite ' "$report_dir"/TEST-*.xml 2>/dev/null || true)"
  if [[ -z "$suites" ]]; then
    cat > "$summary_file" <<EOF
status=NO_XML
message=No XML test suites were generated.
EOF
    return
  fi

  while IFS= read -r suite_line; do
    [[ -z "$suite_line" ]] && continue
    total=$((total + $(extract_attr "$suite_line" "tests")))
    failures=$((failures + $(extract_attr "$suite_line" "failures")))
    errors=$((errors + $(extract_attr "$suite_line" "errors")))
    skipped=$((skipped + $(extract_attr "$suite_line" "skipped")))
  done <<< "$suites"

  local passed=$((total - failures - errors - skipped))
  cat > "$summary_file" <<EOF
status=OK
tests=$total
passed=$passed
failures=$failures
errors=$errors
skipped=$skipped
EOF
}

SUMMARY_FILE="$RUN_DIR/summary.txt"
write_summary "$PROJECT_ROOT/target/surefire-reports" "$SUMMARY_FILE"

echo "Summary file : $SUMMARY_FILE"
if [[ -f "$SUMMARY_FILE" ]]; then
  cat "$SUMMARY_FILE"
fi

if [[ $MVN_EXIT_CODE -ne 0 ]]; then
  echo "Test execution finished with failures."
else
  echo "Test execution finished successfully."
fi

exit "$MVN_EXIT_CODE"
