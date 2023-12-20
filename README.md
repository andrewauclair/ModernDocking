# Modern Docking

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Modern Docking is a simple framework designed for adding docking features to Java Swing applications. 

There are many existing Java Swing docking frameworks, but they are outdated and no longer maintained. The existing frameworks also suffer from complexity due to customization features.

## Download
Modern Docking binaries are available on <b>Maven Central.</b> In order to use Modern Docking you will need the `modern-docking-api` package along with either `modern-docking-single-app` or `modern-docking-multi-app`.
`modern-docking-single-app` is used by applications that create a single instance of the application per JVM instance. `modern-docking-multi-app` is used by applications that create multiple instances of the application per JVM, for example IntelliJ IDEA works this way.

If you use Maven or Gradle, add a dependency with the following coordinates to your build script:

```
    groupId:     io.github.andrewauclair
    artifactId:  modern-docking-api
    version:     (see button below)
```
```
    groupId:     io.github.andrewauclair
    artifactId:  modern-docking-single-app
    version:     (see button below)
```
```
    groupId:     io.github.andrewauclair
    artifactId:  modern-docking-multi-app
    version:     (see button below)
```

`modern-docking-api`: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.andrewauclair/modern-docking-api/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.github.andrewauclair/modern-docking-api)

`modern-docking-single-app`: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.andrewauclair/modern-docking-single-app/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.github.andrewauclair/modern-docking-single-app)

`modern-docking-multi-app`: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.andrewauclair/modern-docking-multi-app/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.github.andrewauclair/modern-docking-multi-app)

### Snapshots

Modern Docking snapshot binaries are available on
[Sonatype OSSRH](https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/andrewauclair/).
To access the latest snapshot, change the Modern Docking version in your dependencies
to `<version>-SNAPSHOT` (e.g. `0.1.0-SNAPSHOT`) and add the repository
`https://s01.oss.sonatype.org/content/repositories/snapshots/` to your build (see
[Maven](https://maven.apache.org/guides/mini/guide-multiple-repositories.html)
and
[Gradle](https://docs.gradle.org/current/userguide/declaring_repositories.html#sec:declaring_custom_repository)
docs).


## Features
- Docking Component Interface
- Docking Frames (Floating JFrame)
- Docking Ports (One per frame)
- Split Panels
- Tabbed Panels
- Visual Studio style drag floating and docking hints that show drop locations


## Modern Docking UI Extension
- Requires FlatLaf L&F

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.formdev/flatlaf/badge.svg?style=flat-square&color=007ec6&version=3.2)](https://maven-badges.herokuapp.com/maven-central/com.formdev/flatlaf)

### Download
Modern Docking UI binaries are available on <b>Maven Central.</b>

If you use Maven or Gradle, add a dependency with the following coordinates to your build script:

    groupId:     io.github.andrewauclair
    artifactId:  modern-docking-ui
    version:     (see button below)

Otherwise download `ModernDockingUI-<version>.jar` here:

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.andrewauclair/modern-docking-ui/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/io.github.andrewauclair/modern-docking-ui)


## Examples
See basic-demo for a simple example in Example.java and a more complicated example in MainFrame.java.
