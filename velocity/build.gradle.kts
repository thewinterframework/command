plugins { `java-library` }

dependencies {
    api(project(":command-core"))
    api("org.incendo:cloud-velocity:${property("cloudMinecraftVersion")}")
    compileOnlyApi("com.thewinterframework:velocity:${property("winterVersion")}")

    compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")

    testImplementation("com.velocitypowered:velocity-api:${property("velocityVersion")}")
    testImplementation("com.thewinterframework:velocity:${property("winterVersion")}")
}
