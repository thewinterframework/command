# Winter Command

Integración de [Incendo Cloud](https://cloud.incendo.org/) con los componentes y la inyección de dependencias de Winter para **Paper** y **Velocity**. Requiere **Java 21**.

## Módulos

| Artefacto | Contenido | Remitente de comandos |
| --- | --- | --- |
| `com.thewinterframework:command-core:3.0.0` | `@CommandComponent`, extensiones genéricas y registro compartido | Genérico (`C`) |
| `com.thewinterframework:command-paper:3.0.0` | Gestor, parser de anotaciones y procesador de Winter para Paper | `org.incendo.cloud.paper.util.sender.Source` |
| `com.thewinterframework:command-velocity:3.0.0` | Gestor, parser de anotaciones y procesador de Winter para Velocity | `com.velocitypowered.api.command.CommandSource` |

Cada adaptador incluye `command-core` transitivamente. Usa un solo adaptador por plugin y por ruta de procesamiento de anotaciones. El núcleo y el adaptador de Velocity no dependen de Bukkit ni de Paper.

## Versiones

| Dependencia | Versión |
| --- | --- |
| Winter Core / Paper / Velocity | `2.2.0` |
| Cloud Core / Annotations | `2.1.0` |
| Cloud Paper / Velocity | `2.0.1` |
| Velocity API de compilación | `3.4.0-SNAPSHOT` |

Cloud mantiene versiones independientes para el [núcleo y las anotaciones](https://github.com/Incendo/cloud/releases) y las [integraciones de Minecraft](https://github.com/Incendo/cloud-minecraft/releases). Las versiones están centralizadas en `gradle.properties`.

## Instalación

Los siguientes ejemplos usan Gradle Kotlin DSL y las coordenadas de Maven Central para Winter `2.2.0` y Winter Command `3.0.0`.

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

El plugin debe extender `PaperWinterPlugin` y llevar `@WinterBootPlugin`. `PaperCommandModule` crea el gestor durante la habilitación del plugin mediante `PaperCommandManager.buildOnEnable`. El gestor moderno de Cloud requiere Paper **1.20.6 o posterior**; utiliza una versión compatible con tu distribución de Winter.

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

Se pueden inyectar `PaperCommandManager<Source>`, `CommandManager<Source>` y `AnnotationParser<Source>`; ambos tipos de gestor resuelven la misma instancia.

### Velocity

```kotlin
dependencies {
    implementation("com.thewinterframework:velocity:2.2.0")
    annotationProcessor("com.thewinterframework:velocity:2.2.0")

    implementation("com.thewinterframework:command-velocity:3.0.0")
    annotationProcessor("com.thewinterframework:command-velocity:3.0.0")
}
```

El plugin debe extender `VelocityWinterPlugin` y llevar `@WinterBootPlugin` junto con `@Plugin` de Velocity. Su constructor inyectado recibe el `Injector` de Velocity y lo pasa a `super(injector)`. Winter hereda las vinculaciones de `ProxyServer` y del `PluginContainer` del propio plugin. El adaptador crea `VelocityCommandManager` durante la habilitación de módulos y utiliza `CommandSource` directamente.

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

Se pueden inyectar `VelocityCommandManager<CommandSource>`, `CommandManager<CommandSource>` y `AnnotationParser<CommandSource>`; ambos tipos de gestor resuelven la misma instancia.

## Extensiones de comandos

Winter descubre las clases anotadas con `@CommandComponent` y resuelve sus dependencias mediante el inyector del plugin. Además de comandos con anotaciones de Cloud, admite:

- `ArgumentParser<C, T>`: registra un parser para `T`, incluyendo implementaciones heredadas.
- `ParameterInjector<C, T>`: registra la inyección de parámetros para `T`.
- `CommandManagerConfiguration<C>`: configura el gestor antes de analizar los comandos.
- `AnnotationParserDecorator<C>`: personaliza el parser de anotaciones antes de registrar los comandos.
- `SenderMapper<N, C>`: reemplaza el mapper predeterminado; `N` es `CommandSourceStack` en Paper y `CommandSource` en Velocity. `C` conserva el tipo de remitente del adaptador.

Por ejemplo, una configuración de Velocity implementa:

```java
@CommandComponent
public final class Commands implements CommandManagerConfiguration<CommandSource> {
    @Override
    public void configure(final CommandManager<CommandSource> manager) {
        // Registrar opciones, parsers o procesadores de Cloud.
    }
}
```

Los parsers, inyectores y decoradores se registran primero; después se aplican las configuraciones y finalmente se analizan los comandos. Usa inyección por constructor para las dependencias de cada componente. Los gestores mantienen `ExecutionCoordinator.simpleCoordinator()`; respeta las restricciones de ejecución de la plataforma dentro de los handlers.

## Migración desde 2.0.0

Esta es una versión mayor porque cambian los artefactos y los contratos de las extensiones:

1. Sustituye `com.thewinterframework:command:2.0.0` por `command-paper:3.0.0` o `command-velocity:3.0.0`, tanto en las dependencias como en `annotationProcessor`.
2. Conserva el import `com.thewinterframework.command.CommandComponent`.
3. Añade el tipo de remitente a `CommandManagerConfiguration<C>`, `CommandConfigurationRegistrar<C>` y `AnnotationParserDecorator<C>`.
4. Cambia el parámetro de `CommandManagerConfiguration.configure` de `PaperCommandManager<Source>` a `CommandManager<Source>` en Paper, o a `CommandManager<CommandSource>` en Velocity. Inyecta el gestor concreto si necesitas funciones específicas de la plataforma.
5. Si registrabas el módulo manualmente, sustituye `CommandModule` por `com.thewinterframework.command.paper.PaperCommandModule` o `com.thewinterframework.command.velocity.VelocityCommandModule`. El procesador del adaptador genera ese registro automáticamente.

## Compilación y publicación local

```powershell
.\gradlew.bat clean build
.\gradlew.bat publishToMavenLocal
```

En Linux/macOS usa `./gradlew`. El build genera los JAR, fuentes y Javadoc en `core/build/libs`, `paper/build/libs` y `velocity/build/libs`. La publicación utiliza los nombres `command-core`, `command-paper` y `command-velocity`.

Las pruebas comprueban la ejecución de comandos y sus extensiones, la generación de módulos para cada plataforma, la ruta independiente de procesamiento de anotaciones y la inyección del gestor real de Velocity. La ejecución dentro de un servidor Paper o proxy Velocity debe verificarse en el entorno de integración del plugin consumidor.
