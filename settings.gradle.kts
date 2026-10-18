rootProject.name = "command"

include("core", "paper", "velocity")
project(":core").name = "command-core"
project(":paper").name = "command-paper"
project(":velocity").name = "command-velocity"

