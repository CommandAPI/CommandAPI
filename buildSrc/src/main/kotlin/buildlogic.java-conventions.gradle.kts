plugins {
    `java-library`
    `maven-publish`
}

repositories {
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    maven {
        url = uri("https://libraries.minecraft.net")
    }
    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/nms/")
    }
	maven {
		url = uri("https://repo.papermc.io/repository/maven-public/")
	}
	maven {
		url = uri("https://central.sonatype.com/repository/maven-snapshots/")
	}
	mavenCentral()
}

group = "dev.jorel"
version = "11.2.0"
java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

java {
    withSourcesJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
	options.release = 17
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

configurations.all {
	if (isCanBeResolved) {
		attributes {
			attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
		}
	}
}
