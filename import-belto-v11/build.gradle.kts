plugins {
//    id("org.jetbrains.kotlin.jvm") version Versions.KOTLIN
    java
}

group = "by.gto.import-belto-v11"
version = "1.0.0"

java.sourceCompatibility = JavaVersion.VERSION_12
java.targetCompatibility = JavaVersion.VERSION_12

repositories {
    mavenCentral()
    maven {
        url = uri("sftp://git.gto.by:22002/var/mvnroot/")
        credentials {
            username =
                System.getProperty("mvn.deploy.username") // systemProp.mvn.deploy.username=name in c:\Users\<user>\.gradle\gradle.properties
            password = System.getProperty("mvn.deploy.password")
        }
    }
}

dependencies {
    implementation("by.gto.ais:ais-commons:${Versions.AIS_COMMONS}")
    implementation("by.gto.ais:ais-model2:${Versions.AIS_MODEL2}") {
        exclude(group = "com.fasterxml.jackson.core")
    }
    // javax.xml.bind и com.sun.xml.bind нужны вместо выпиленных из java 9 модулей
    implementation("javax.xml.bind:jaxb-api:${Versions.JAVAX_XML_BIND}")
    implementation("com.sun.xml.bind:jaxb-core:${Versions.JAVAX_XML_BIND}")
    implementation("com.sun.xml.bind:jaxb-impl:${Versions.JAVAX_XML_BIND}")

//    compile "org.firebirdsql.jdbc:jaybird-jdk18:${Versions.JAYBIRD}"
    implementation("org.mariadb.jdbc:mariadb-java-client:${Versions.MARIADB}")
    implementation("commons-io:commons-io:${Versions.COMMONS_IO}")
    implementation("javax.activation:javax.activation-api:1.2.0")

    implementation("by.gto.library:bto-library-common:1.10") {
        exclude(group = "log4j")
        exclude(group = "mysql")
        exclude(group = "commons-configuration")
        exclude(group = "org.mariadb.jdbc")
    }
    implementation("org.apache.commons:commons-compress:1.10")

    testImplementation("junit:junit:${Versions.JUNIT}")
}

task<Copy>("copyToLib") {
    group = "Build"
    description = "copies transitive dependencies"
    from(configurations.runtimeClasspath)
    into("$buildDir/libs/lib")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    systemProperty("file.encoding", "UTF-8")
}

tasks.withType<Jar> {
    archiveFileName.set(project.name + ".jar")
    val copyToLib by tasks.existing
    dependsOn(copyToLib)

    doFirst {
        manifest {
            attributes(mapOf("Implementation-Title" to "Gradle",
                "Implementation-Version" to "${archiveVersion.get()}.${buildNumber}",
                "Main-Class" to "main.Main",
                "Class-Path" to configurations.runtimeClasspath.get().files.map { "lib/" + it.name }
                    .sorted().joinToString(" ")))
        }
    }
}
