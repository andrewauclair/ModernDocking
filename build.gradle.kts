plugins {
	`java`
	`idea`
	`maven-publish`
	`signing`
	id ("com.github.ben-manes.versions") version "0.52.0"
	id ("io.deepmedia.tools.deployer") version "0.18.0"
}
java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}
subprojects {


	repositories {
		mavenLocal()
		mavenCentral()
	}

	version = "1.1.3"

	ext {

	}
}
buildscript {
	val flatLafVersion by extra("3.6")
}
//publishing {
//	repositories {
//		maven {
//			name = "OSSRH"
//			credentials {
//				username = System.getenv("MAVEN_USERNAME")
//				password = System.getenv("MAVEN_PASSWORD")
//			}
//
//			url = version.endsWith('SNAPSHOT') ?
//					'https://central.sonatype.com/repository/maven-snapshots/'
//					: 'https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/'
//		}
//	}
//}

deployer {
	content {
		kotlinComponents()
	}
	projectInfo {
		description = "Modern docking framework for Java Swing"
		url = "https://github.com/andrewauclair/ModernDocking"
		scm.fromGithub("andrewauclair", "ModernDocking")
		license(MIT)
		developer("Andrew Auclair", "mightymalakai33@gmail.com")
		groupId = "io.github.andrewauclair.moderndocking"
	}

	signing {
		key = secret("MAVEN_GPG_KEY_ID")
		password = secret("MAVEN_GPG_PASSPHRASE")
	}
	centralPortalSpec {
		auth.user = secret("MAVEN_USERNAME")
		auth.password = secret("MAVEN_PASSWORD")
	}
}
//deployer {
//	project.description = ""
//
//	centralPortalSpec("Modern Docking") {
//		//auth.user = System.getenv("MAVEN_USERNAME")
//		//auth.password = System.getenv("MAVEN_PASSWORD")
//
//
//		signing {
//			key = System.getenv("MAVEN_GPG_KEY_ID")
//			password = System.getenv("MAVEN_GPG_PASSPHRASE")
//		}
//	}
//}