# Winter Command

Integrates [Incendo Cloud](https://cloud.incendo.org/) with Winter components and dependency injection for **Paper** and **Velocity**. Requires **Java 21**.

## Modules

| Artifact | Contents | Command sender |
| --- | --- | --- |
| `com.thewinterframework:command-core:3.0.0` | `@CommandComponent`, generic extensions, and shared registration | Generic (`C`) |
| `com.thewinterframework:command-paper:3.0.0` | Command manager, annotation parser, and Winter processor for Paper | `org.incendo.cloud.paper.util.sender.Source` |
| `com.thewinterframework:command-velocity:3.0.0` | Command manager, annotation parser, and Winter processor for Velocity | `com.velocitypowered.api.command.CommandSource` |

Each adapter includes `command-core` transitively. Use one adapter per plugin and annotation processor path. The core module and Velocity adapter have no dependencies on Bukkit or Paper.

## Versions

| Dependency | Version |
| --- | --- |
| Winter Core / Paper / Velocity | `2.2.0` |
| Cloud Core / Annotations | `2.1.0` |
| Cloud Paper / Velocity | `2.0.1` |
| Velocity API used for compilation | `3.4.0-SNAPSHOT` |

Cloud versions its [core and annotations](https://github.com/Incendo/cloud/releases) independently from its [Minecraft integrations](https://github.com/Incendo/cloud-minecraft/releases). Dependency versions are centralized in `gradle.properties`.

## Installation

The following examples use Gradle Kotlin DSL and Maven Central coordinates for Winter `2.2.0` and Winter Command `3.0.0`.

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
}
```

### Paper

```kotlin
dependencies {
    implementation("com.thewinterframework:paper:2.2.0")
    annotationProcessor("com.thewinterframework:paper:2.2.0")

    implementation("com.thewinterframework:command-paper:3.0.0")
    annotationProcessor("com.thewinterframework:command-paper:3.0.0")
}
```

The plugin must extend `PaperWinterPlugin` and declare `@WinterBootPlugin`. `PaperCommandModule` creates the command manager during plugin enable using `PaperCommandManager.buildOnEnable`. Cloud's modern command manager requires Paper **1.20.6 or later**; choose a version compatible with your Winter distribution.

```java
import com.thewinterframework.command.CommandComponent;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.paper.util.sender.Source;

@CommandComponent
public final class HelloCommand {
    @Command("hello")
    public void hello(final Source source) {
        source.source().sendMessage("Hello from Paper!");
    }
}
```

You can inject `PaperCommandManager<Source>`, `CommandManager<Source>`, and `AnnotationParser<Source>`. Both command manager types resolve to the same instance.

### Velocity

```kotlin
dependencies {
    implementation("com.thewinterframework:velocity:2.2.0")
    annotationProcessor("com.thewinterframework:velocity:2.2.0")

    implementation("com.thewinterframework:command-velocity:3.0.0")
    annotationProcessor("com.thewinterframework:command-velocity:3.0.0")
}
```

The plugin must extend `VelocityWinterPlugin` and declare both `@WinterBootPlugin` and Velocity's `@Plugin`. Its injected constructor receives Velocity's `Injector` and passes it to `super(injector)`. Winter inherits the bindings for `ProxyServer` and the plugin's own `PluginContainer`. The adapter creates `VelocityCommandManager` during module enable and uses `CommandSource` directly.

```java
import com.thewinterframework.command.CommandComponent;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.annotations.Command;

@CommandComponent
public final class HelloCommand {
    @Command("hello")
    public void hello(final CommandSource source) {
        source.sendMessage(Component.text("Hello from Velocity!"));
    }
}
```

You can inject `VelocityCommandManager<CommandSource>`, `CommandManager<CommandSource>`, and `AnnotationParser<CommandSource>`. Both command manager types resolve to the same instance.

## Command extensions

Winter discovers classes annotated with `@CommandComponent` and resolves their dependencies through the plugin injector. In addition to commands declared with Cloud annotations, it supports:

- `ArgumentParser<C, T>`: registers a parser for `T`, including inherited implementations.
- `ParameterInjector<C, T>`: registers parameter injection for `T`.
- `CommandManagerConfiguration<C>`: configures the command manager before annotated commands are parsed.
- `AnnotationParserDecorator<C>`: customizes the annotation parser before commands are registered.
- `SenderMapper<N, C>`: replaces the default sender mapper. `N` is `CommandSourceStack` on Paper and `CommandSource` on Velocity. `C` remains the adapter's command sender type.

For example, a Velocity configuration implements:

```java
@CommandComponent
public final class Commands implements CommandManagerConfiguration<CommandSource> {
    @Override
    public void configure(final CommandManager<CommandSource> manager) {
        // Register Cloud options, parsers, or processors.
    }
}
```

Parsers, parameter injectors, and decorators are registered first. Configurations are then applied, followed by annotation parsing for commands. Use constructor injection for component dependencies. Command managers use `ExecutionCoordinator.simpleCoordinator()`; handlers must respect the platform's threading requirements.

## Migration from 2.0.0

This is a major release because artifact coordinates and extension contracts have changed:

1. Replace `com.thewinterframework:command:2.0.0` with `command-paper:3.0.0` or `command-velocity:3.0.0` in both your dependencies and `annotationProcessor` configuration.
2. Keep the `com.thewinterframework.command.CommandComponent` import.
3. Add the command sender type to `CommandManagerConfiguration<C>`, `CommandConfigurationRegistrar<C>`, and `AnnotationParserDecorator<C>`.
4. Change the parameter of `CommandManagerConfiguration.configure` from `PaperCommandManager<Source>` to `CommandManager<Source>` on Paper, or to `CommandManager<CommandSource>` on Velocity. Inject the concrete command manager when you need platform-specific features.
5. If you registered the module manually, replace `CommandModule` with `com.thewinterframework.command.paper.PaperCommandModule` or `com.thewinterframework.command.velocity.VelocityCommandModule`. The adapter's annotation processor generates this registration automatically.

## Building and publishing locally

```powershell
.\gradlew.bat clean build
.\gradlew.bat publishToMavenLocal
```

On Linux and macOS, use `./gradlew`. The build generates binary, source, and Javadoc JARs in `core/build/libs`, `paper/build/libs`, and `velocity/build/libs`. Published artifacts are named `command-core`, `command-paper`, and `command-velocity`.

Tests cover command execution and extensions, module generation for each platform, the separate annotation processor path, and injection of the real Velocity command manager. Verify execution on a running Paper server or Velocity proxy in the consuming plugin's integration environment.
