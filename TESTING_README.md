# Chess SUT — Testing Guide

How to run the full test suite and reproduce the results presented in the final demo.

---

## Prerequisites

| Requirement | Version | Notes |
|---|---|---|
| Java (JDK) | **11 or higher** | JavaFX 12 is pulled from Maven; Java 8's bundled JavaFX is too old (`Platform.startup` requires Java 9+) |
| Maven | 3.6+ | Used to compile, test, and generate reports |
| Internet (first run only) | — | Maven downloads ~15 MB of dependencies on first run |

> **Recommended JDK:** Eclipse Temurin 11 — [adoptium.net](https://adoptium.net)

Set `JAVA_HOME` before running any command:
```bash
export JAVA_HOME=/path/to/your/jdk-11
```

---

## Project Layout

```
chess/
├── src/                   # SUT source code (com.chess.*)
├── test/
│   ├── com/chess/blackbox/       # Black-box tests (EP, BA, EG)
│   ├── com/chess/mutation/       # Mutation-killing tests
│   ├── com/chess/GUITesting/     # GUI tests (TestFX + Monocle)
│   └── com/chess/WhiteBoxTesting/# White-box / integration tests
├── pom.xml                # Maven config (Surefire, JaCoCo, PITest)
└── TESTING_README.md      # This file
```

---

## Run All Tests

This single command compiles everything, runs all test classes, and generates the JaCoCo coverage report.

```bash
mvn test
```

**Expected result:**
```
Tests run: 737, Failures: 0, Errors: 0, Skipped: 3
BUILD SUCCESS
```

The 3 skipped tests are intentional — they document real bugs found in the SUT:
- `PgnParserTest#testGetEventEmptyPgnCrashesInGetMetaTag` — crashes SUT with `StringIndexOutOfBoundsException`
- `PgnParserTest#testParseMovesNullNoException` — crashes SUT with `NullPointerException`
- `SettingsTest#colorChoiceWhiteThenBlackSelected` — UX/UI suggestion; not actual error in interface

---

## Run a Specific Test Suite

```bash
# Black-box tests only
mvn test -Dtest="com.chess.blackbox.*"

# Mutation-killing tests only  
mvn test -Dtest="com.chess.mutation.*"

# GUI tests only (opens no window — headless via Monocle)
mvn test -Dtest=SettingsTest

# White-box / integration tests only
mvn test -Dtest="com.chess.WhiteBoxTesting.*"
```

> **Note:** Surefire 2.22.2 does not support package wildcards in `-Dtest`. If the above wildcard forms fail on your machine, run `mvn test` (all suites together) or list class names individually, e.g. `-Dtest=CheckTest,CheckmateTest`.

---

## JaCoCo Coverage Report

JaCoCo runs automatically as part of `mvn test`. After the build completes, open:

```
target/site/jacoco/index.html
```

This shows line and branch coverage for the SUT. The report is regenerated on every `mvn test` run.

---

## Mutation Testing (PITest)

Mutation testing is run separately from the normal test suite because it is slow (~5–15 minutes depending on machine speed).

```bash
mvn org.pitest:pitest-maven:mutationCoverage
```

**Expected result:** approximately **77% mutation score** (mutations killed / total mutations).

The HTML report is written to:
```
target/pit-reports/index.html
```

The XML report (for scripted parsing) is at:
```
target/pit-reports/mutations.xml
```

> PITest uses the `STRONGER` mutator set and forks its own JVM with the same Monocle headless flags used by Surefire, so no display is required.

---

## Real Bugs Found in the SUT

Two confirmed faults were discovered during testing. Both are documented with `@Ignore`d tests so they do not block the green suite but are clearly visible in the test output.

### Fault 1 — `PgnParser.getMetaTag()`: `StringIndexOutOfBoundsException`

**Location:** `src/com/chess/root/PgnParser.java`, line 240

```java
result = s.split(" ")[0].replaceAll("\n", "").substring(1);
```

**Trigger:** `Setting.addPgn("")` splits on `]` and produces an empty-string element.  
When `getMetaTag("")` is called: `"".split(" ")[0]` returns `""`, and `"".substring(1)` throws.

**Documented by:** `PgnParserTest#testGetEventEmptyPgnCrashesInGetMetaTag` (`@Ignore`d)

---

### Fault 2 — `Board` constructor: `NullPointerException` when `hasFen=true` and `hasPgn=true`

**Location:** `src/com/chess/root/Board.java` constructor

**Trigger:** Constructing a `Board` with both `hasFen=true` and `hasPgn=true` in the `Setting`.  
The constructor skips `validateBoard()` for PGN replay, but `isKingVictim()` is then called on the uninitialized (`null`) `currentMoves` list.

**Documented by:** The `createBoardWithPgn()` helper in `BoardMutationTest.java`, which works around the fault by initializing the board from FEN first and replaying PGN separately.

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `Platform.startup` compile error | You are using Java 8. Switch to Java 11+ and re-run. |
| `mvn: command not found` | Either install Maven or use the full path to a bundled Maven (e.g., IntelliJ's at `.../plugins/maven/lib/maven3/bin/mvn`). |
| `No tests were executed` with `-Dtest="com.chess.blackbox.*"` | Surefire 2.22.2 does not support package wildcards; use exact class names or just run `mvn test`. |
| GUI tests fail outside Maven (e.g., in IDE) | The IDE does not pass the Monocle JVM flags. Run GUI tests via `mvn test -Dtest=SettingsTest` instead. |
| PITest runs out of heap | Add `-Xmx2g` to your Maven JVM args, or reduce `<timeoutFactor>` in `pom.xml`. |
