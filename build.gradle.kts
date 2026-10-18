plugins {
    base
    id("io.deepmedia.tools.deployer") version "0.16.0"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "io.deepmedia.tools.deployer")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        withJavadocJar()
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        systemProperty("command.test.classpath", project.extensions.getByType<SourceSetContainer>()["test"].runtimeClasspath.asPath)
        systemProperty("command.test.processorpath", project.extensions.getByType<SourceSetContainer>()["main"].runtimeClasspath.asPath)
    }

    dependencies {
        "testImplementation"("org.junit.jupiter:junit-jupiter:5.11.4")
        "testImplementation"("org.mockito:mockito-core:5.15.2")
        "testImplementation"("net.kyori:adventure-api:4.18.0")
        "testImplementation"("org.slf4j:slf4j-api:2.0.16")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    deployer {
        signing {
            key.set(secret("winterSigningKey"))
            password.set(secret("winterSigningPassphrase"))
        }
        content {
            component { fromJava() }
        }
        projectInfo {
            description.set("Winter command integration for Cloud on Paper and Velocity.")
            url.set("https://github.com/thewinterframework/command")
            license(MIT)
            groupId.set(project.group.toString())
            artifactId.set(project.name)
            scm.fromGithub("thewinterframework", "command")
            developer("Diego Cardenas", "diego.cardenas.v06@gmail.com")
        }
        centralPortalSpec {
            signing.key.set(secret("winterSigningKey"))
            signing.password.set(secret("winterSigningPassphrase"))
            auth.user.set(secret("winterRepositoryUsername"))
            auth.password.set(secret("winterRepositoryPassword"))
        }
    }

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") { from(components["java"]) }
        }
    }
}

configure(subprojects.filter { it.name != "command-core" }) {
    extensions.configure<SourceSetContainer> {
        named("test") { java.srcDir(rootProject.file("test-support/src/main/java")) }
    }
    tasks.withType<Test>().configureEach {
        systemProperty("command.test.platform", project.name.removePrefix("command-"))
    }
}

tasks.named("build") { dependsOn(subprojects.map { "${it.path}:build" }) }
tasks.named("check") { dependsOn(subprojects.map { "${it.path}:check" }) }
tasks.named("clean") { dependsOn(subprojects.map { "${it.path}:clean" }) }
