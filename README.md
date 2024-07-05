# Modern Docking

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.andrewauclair/modern-docking-api?label=modern-docking-api)](https://mvnrepository.com/artifact/io.github.andrewauclair/modern-docking-api)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.andrewauclair/modern-docking-single-app?label=modern-docking-single-app)](https://mvnrepository.com/artifact/io.github.andrewauclair/modern-docking-single-app)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.andrewauclair/modern-docking-multi-app?label=modern-docking-multi-app)](https://mvnrepository.com/artifact/io.github.andrewauclair/modern-docking-multi-app)

[![Maven Central](https://img.shields.io/maven-central/v/io.github.andrewauclair/modern-docking-ui?label=modern-docking-ui)](https://mvnrepository.com/artifact/io.github.andrewauclair/modern-docking-ui)


Modern Docking is a simple framework designed for adding docking features to Java Swing applications. 

There are many existing Java Swing docking frameworks, but they are outdated and no longer maintained. The existing frameworks also suffer from complexity due to customization features.

### Snapshots

Modern Docking snapshot binaries are available on
[Sonatype OSSRH](https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/andrewauclair/).
To access the latest snapshot, change the Modern Docking version in your dependencies
to `<version>-SNAPSHOT` (e.g. `0.11.0-SNAPSHOT`) and add the repository
`https://s01.oss.sonatype.org/content/repositories/snapshots/` to your build (see
[Maven](https://maven.apache.org/guides/mini/guide-multiple-repositories.html)
and
[Gradle](https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:declaring_custom_repository)
docs).


## Features
- Supports Java 11 and newer
- Docking Component Interface
- Docking Frames (Floating JFrame)
- Docking Ports (One per frame)
- Split Panels
- Tabbed Panels
- Visual Studio style drag floating and docking hints that show drop locations


## Modern Docking UI Extension
- Requires FlatLaf L&F

<!-- todo: we should limit this to the version Modern Docking actually uses, if we can -->
[![Maven Central](https://img.shields.io/maven-central/v/com.formdev/flatlaf)](https://mvnrepository.com/artifact/com.formdev/flatlaf)

# Building

Modern Docking uses Gradle and can be opened directly in IntelliJ IDEA or any other IDE that supports Gradle. It can also be built from the command line with `./gradlew build`.

## Examples
See basic-demo for a simple example in Example.java and a more complicated example in MainFrame.java.

## Support
<a href="https://www.buymeacoffee.com/andrewauclair" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>
