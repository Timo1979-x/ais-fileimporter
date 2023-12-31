plugins {
    id("org.jetbrains.kotlin.jvm") version Versions.KOTLIN
}

group = "by.gto.xchanger"
version = "1.13"

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
    implementation("com.zaxxer:HikariCP:${Versions.HIKARICP}")

    implementation("org.springframework:spring-jdbc:${Versions.SPRING}")
    implementation("org.springframework:spring-tx:${Versions.SPRING}")
    implementation("org.springframework:spring-context:${Versions.SPRING}")
    implementation("commons-io:commons-io:${Versions.COMMONS_IO}")

    implementation("com.sun.mail:javax.mail:1.5.6")
    implementation("by.gto.library:bto-library-xml:1.5.0") {
        exclude(group = "mysql")
    }
    implementation("by.gto.library:bto-library-common:1.8") {
        exclude(group = "log4j")
        exclude(group = "mysql")
        exclude(group = "commons-configuration")
        exclude(group = "org.mariadb.jdbc")
    }
    implementation("org.apache.commons:commons-compress:1.10")
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
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
                "Main-Class" to "by.gto.xchanger.Importer",
                "Class-Path" to configurations.runtimeClasspath.get().files.map { "lib/" + it.name }
                    .sorted().joinToString(" ")))
        }
    }
    doLast {
        File("$buildDir/libs/import-belto.sh").writer().use { writer ->
            writer.write(
                """
                #/bin/bash

                java -jar ${project.name}.jar --import-belto=/home/tim/belto
                """.trimIndent()
            )
        }
    }
}
