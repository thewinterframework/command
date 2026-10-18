plugins { `java-library` }

dependencies {
    api(project(":command-core"))
    api("org.incendo:cloud-paper:${property("cloudMinecraftVersion")}")
    compileOnlyApi("com.thewinterframework:paper:${property("winterVersion")}")

    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")

    testImplementation("com.thewinterframework:paper:${property("winterVersion")}")
    testImplementation("io.papermc.paper:paper-api:${property("paperVersion")}")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}
