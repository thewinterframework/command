plugins { `java-library` }

dependencies {
    api("com.thewinterframework:core:${property("winterVersion")}")
    api("org.incendo:cloud-core:${property("cloudVersion")}")
    api("org.incendo:cloud-annotations:${property("cloudVersion")}")
}
