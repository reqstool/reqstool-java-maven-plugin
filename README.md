[![Commit Activity](https://img.shields.io/github/commit-activity/m/reqstool/reqstool-java-maven-plugin?label=commits&style=for-the-badge)](https://github.com/reqstool/reqstool-java-maven-plugin/pulse)
[![GitHub Issues](https://img.shields.io/github/issues/reqstool/reqstool-java-maven-plugin?style=for-the-badge&logo=github)](https://github.com/reqstool/reqstool-java-maven-plugin/issues)
[![License](https://img.shields.io/github/license/reqstool/reqstool-java-maven-plugin?style=for-the-badge&logo=opensourceinitiative)](https://opensource.org/license/mit/)
[![Build](https://img.shields.io/github/actions/workflow/status/reqstool/reqstool-java-maven-plugin/build.yml?style=for-the-badge&logo=github)](https://github.com/reqstool/reqstool-java-maven-plugin/actions/workflows/build.yml)
[![Documentation](https://img.shields.io/badge/Documentation-blue?style=for-the-badge&link=docs)](https://reqstool.github.io)

# Reqstool Maven Plugin

Maven build plugin for [reqstool](https://github.com/reqstool/reqstool-client) that assembles requirements traceability artifacts.

## Overview

Collects `@Requirements` and `@SVCs` annotations from compiled Java code, combines them with test results, and packages everything into a ZIP artifact for analysis by the reqstool CLI.

## Installation

Add the plugin to your `pom.xml`:

```xml
<plugin>
    <groupId>io.github.reqstool</groupId>
    <artifactId>reqstool-maven-plugin</artifactId>
    <version>1.0.4</version>
    <executions>
        <execution>
            <goals>
                <goal>assemble-and-attach-zip-artifact</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <datasetPath>${project.basedir}/docs/reqstool</datasetPath>
    </configuration>
</plugin>
```

## Usage

```bash
mvn clean verify
```

The plugin generates a ZIP artifact in `target/reqstool/` containing requirements, annotations, and test results.

## Documentation

Full documentation can be found [here](https://reqstool.github.io).

## Contributing

See the organization-wide [CONTRIBUTING.md](https://github.com/reqstool/.github/blob/main/CONTRIBUTING.md).

## License

MIT License.
