import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
    `kotlin-dsl`
}
// Required since Gradle 4.10+.
repositories {
    jcenter()
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
}

dependencies {
    implementation("org.hidetake.ssh", "org.hidetake.ssh.gradle.plugin", "2.10.1")
}
