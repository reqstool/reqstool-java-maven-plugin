# mypackage

Minimal test project for manual validation of `reqstool-java-maven-plugin`.

## Prerequisites

- Java 21+
- Maven 3.9+
- `reqstool` CLI: `pip install reqstool`

## Validation

Run all commands from `tests/fixtures/test_project/`.

### 1 — Build

```bash
mvn verify
```

This compiles (triggering the APT annotation processor), runs tests, and assembles the reqstool zip.

Expected output (key lines):
```
[INFO] Processing annotations: [io.github.reqstool.annotations.Requirements]
[INFO] Writing Requirements Annotations data to: target/generated-sources/annotations/resources/annotations.yml

[INFO] Processing annotations: [io.github.reqstool.annotations.SVCs]
[INFO] Writing Requirements Annotations data to: target/generated-test-sources/test-annotations/resources/annotations.yml

[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

[INFO] Assembling zip file: target/reqstool/mypackage-0.1.0-reqstool.zip
[INFO] BUILD SUCCESS
```

### 2 — Check artefacts

```bash
# zip must exist
ls target/reqstool/mypackage-0.1.0-reqstool.zip

# zip must contain all reqstool files + test results
unzip -l target/reqstool/mypackage-0.1.0-reqstool.zip
```

Expected entries in the zip:
- `mypackage-0.1.0-reqstool/requirements.yml`
- `mypackage-0.1.0-reqstool/software_verification_cases.yml`
- `mypackage-0.1.0-reqstool/annotations.yml`
- `mypackage-0.1.0-reqstool/test_results/TEST-io.github.reqstool.example.HelloTest.xml`
- `mypackage-0.1.0-reqstool/reqstool_config.yml`

### 3 — Run reqstool status

The zip is self-contained (test results included), so just extract and run:

```bash
unzip -o target/reqstool/mypackage-0.1.0-reqstool.zip -d /tmp/mypackage-reqstool
reqstool status local -p /tmp/mypackage-reqstool/mypackage-0.1.0-reqstool
```

Expected: all green — `REQ_001` implemented, `T1 P1`, no missing tests or SVCs.
