package io.github.diskria.projektor.gradle.extensions

import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.modrinth.minotaur.ModrinthExtension
import io.github.diskria.projektor.gradle.extensions.common.gradleError
import io.github.diskria.projektor.gradle.extensions.mappers.toInt
import io.github.diskria.projektor.gradle.extensions.mappers.toJvmTarget
import io.github.diskria.projektor.gradle.utils.VersionCatalogUtils
import io.github.diskria.projektor.licenses.License
import io.github.diskria.projektor.licenses.MitLicense
import io.github.diskria.projektor.minecraft.*
import io.github.diskria.projektor.minecraft.config.FabricModConfig
import io.github.diskria.projektor.minecraft.config.MixinsConfig
import io.github.diskria.projektor.owner.AndroidOrganization
import io.github.diskria.projektor.owner.GithubOwner
import io.github.diskria.projektor.owner.LibrariesOrganization
import io.github.diskria.projektor.owner.MainDeveloper
import io.github.diskria.projektor.owner.MinecraftOrganization
import io.github.diskria.projektor.projekt.GradlePlugin
import io.github.diskria.projektor.projekt.IProjekt
import io.github.diskria.projektor.projekt.Projekt
import io.github.diskria.projektor.projekt.PublishingTarget
import io.github.diskria.projektor.projekt.Secrets
import io.github.diskria.utils.kotlin.BracketsType
import io.github.diskria.utils.kotlin.Constants
import io.github.diskria.utils.kotlin.delegates.toAutoNamedProperty
import io.github.diskria.utils.kotlin.extensions.capitalizeFirstChar
import io.github.diskria.utils.kotlin.extensions.common.className
import io.github.diskria.utils.kotlin.extensions.common.failWithUnsupportedType
import io.github.diskria.utils.kotlin.extensions.common.unsupportedOperation
import io.github.diskria.utils.kotlin.extensions.setCase
import io.github.diskria.utils.kotlin.extensions.wrap
import io.github.diskria.utils.kotlin.extensions.wrapWithBrackets
import io.github.diskria.utils.kotlin.poet.Property
import io.github.diskria.utils.kotlin.words.*
import kotlinx.serialization.json.Json
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.fabricapi.FabricApiExtension
import net.minecraftforge.gradle.MinecraftExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.file.Directory
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmImplementation
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.jvm.optionals.getOrNull

typealias IdeaExt = IdeaModel
typealias BaseExt = BasePluginExtension
typealias JavaExt = JavaPluginExtension
typealias KotlinExt = KotlinProjectExtension
typealias SourceSetsExt = SourceSetContainer
typealias GradlePluginExt = GradlePluginDevelopmentExtension
typealias PublishingExt = PublishingExtension
typealias SigningExt = SigningExtension
typealias BuildConfigExt = BuildConfigExtension
typealias FabricExt = LoomGradleExtensionAPI
typealias FabricApiExt = FabricApiExtension
typealias ForgeExt = MinecraftExtension.ForProject
typealias ModrinthExt = ModrinthExtension

fun Project.getProjectDirectory(path: String): Directory =
    layout.projectDirectory.dir(path)

fun Project.getBuildDirectory(path: String): Provider<Directory> =
    layout.buildDirectory.dir(path)

fun Project.getBuildFile(path: String): Provider<RegularFile> =
    layout.buildDirectory.file(path)

fun Project.requirePlugins(vararg ids: String) {
    ids.forEach { id ->
        require(plugins.hasPlugin(id)) {
            gradleError("Plugin ${id.wrap(Constants.Char.SINGLE_QUOTE)} required but not applied.")
        }
    }
}

inline fun <reified T : Any> Project.getExtensionOrThrow(pluginId: String): T {
    requirePlugins(pluginId)
    val clazz = T::class
    return extensions.findByType(clazz.java) ?: failWithUnsupportedType(clazz)
}

fun <R> Project.idea(block: Any.() -> R): R =
    getExtensionOrThrow<IdeaModel>("idea").block()

fun <R> Project.base(block: Any.() -> R): R =
    getExtensionOrThrow<BaseExt>("base").block()

fun <R> Project.java(block: Any.() -> R): R =
    getExtensionOrThrow<JavaExt>("java").block()

fun <R> Project.kotlin(block: Any.() -> R): R =
    getExtensionOrThrow<KotlinExt>("kotlin").block()

fun <R> Project.sourceSets(block: Any.() -> R): R =
    getExtensionOrThrow<SourceSetsExt>("java").block()

fun <R> Project.gradlePlugin(block: Any.() -> R): R =
    getExtensionOrThrow<GradlePluginExt>("maven-publish").block()

fun <R> Project.publishing(block: Any.() -> R): R =
    getExtensionOrThrow<PublishingExt>("publishing").block()

fun <R> Project.signing(block: Any.() -> R): R =
    getExtensionOrThrow<SigningExt>("signing").block()

fun <R> Project.buildConfig(block: Any.() -> R): R =
    getExtensionOrThrow<BuildConfigExt>("com.github.gmazzo.buildconfig").block()

fun <R> Project.fabricApi(block: Any.() -> R): R =
    getExtensionOrThrow<FabricApiExt>("fabric-loom").block()

fun <R> Project.fabric(block: Any.() -> R): R =
    getExtensionOrThrow<FabricExt>("fabric-loom").block()

fun Project.forge(block: Any.() -> Unit) =
    extensions.configure<ForgeExt>("minecraft", block)

fun <R> Project.modrinth(block: Any.() -> R): R =
    getExtensionOrThrow<ModrinthExt>("com.modrinth.minotaur").block()

fun DependencyHandler.implementation(dependencyNotation: Any): Dependency? =
    add("implementation", dependencyNotation)

fun DependencyHandler.minecraft(dependencyNotation: Any): Dependency? =
    add("minecraft", dependencyNotation)

fun DependencyHandler.mappings(dependencyNotation: Any): Dependency? =
    add("mappings", dependencyNotation)

fun DependencyHandler.modImplementation(dependencyNotation: Any): Dependency? =
    add("modImplementation", dependencyNotation)

fun Project.versionCatalogs(): VersionCatalogsExtension =
    extensions.findByType(VersionCatalogsExtension::class.java) ?: unsupportedOperation()

fun Project.getCatalogVersion(alias: String, catalog: String = VersionCatalogUtils.DEFAULT_CATALOG_NAME): String? =
    versionCatalogs().named(catalog).findVersion(alias).getOrNull()?.requiredVersion

fun Project.getCatalogVersionOrThrow(
    alias: String,
    catalog: String = VersionCatalogUtils.DEFAULT_CATALOG_NAME,
): String =
    getCatalogVersion(alias, catalog) ?: gradleError(
        "Missing alias ${alias.wrap(Constants.Char.SINGLE_QUOTE)} in " +
                "${VersionCatalogUtils.CATALOG_VERSIONS.wrapWithBrackets(BracketsType.SQUARE)} of " +
                VersionCatalogUtils.buildCatalogFileName(catalog)
    )

fun Project.resolveCatalogVersion(
    aliasShort: String,
    catalog: String = VersionCatalogUtils.DEFAULT_CATALOG_NAME,
    formatShort: (String) -> String,
): String =
    getCatalogVersion("$aliasShort-full", catalog) ?: formatShort(getCatalogVersionOrThrow(aliasShort, catalog))

fun Project.getProjectFileNamesFrom(path: String): List<String> =
    getProjectDirectory(path).asFile.listFiles { it.isFile && !it.isHidden }?.map { it.nameWithoutExtension }.orEmpty()

fun Project.configureBuildConfig(packageName: String, className: String, fields: () -> List<Property<String>>) {
    buildConfig {
        this as BuildConfigExt
        packageName(packageName)
        className(className)
        fields().forEach { field ->
            buildConfigField(field.name, field.value)
        }
        useKotlinOutput {
            internalVisibility = false
            topLevelConstants = false
        }
    }
}

fun Project.projekt(owner: GithubOwner, license: License, jvmTarget: JvmTarget? = null): Projekt {
    val projectName: String by rootProject
    val projectDescription: String by rootProject
    val projectVersion: String by rootProject
    val javaVersion = getCatalogVersionOrThrow("java").toInt()
    return Projekt(
        owner = owner,
        license = license,
        name = projectName,
        description = projectDescription,
        version = projectVersion,
        slug = projectName.setCase(SpaceCase, KebabCase).lowercase(),
        packageName = owner.namespace + Constants.Char.DOT + projectName.setCase(SpaceCase, DotCase),
        javaVersion = javaVersion,
        jvmTarget = jvmTarget ?: javaVersion.toJvmTarget(),
        kotlinVersion = getCatalogVersionOrThrow("kotlin"),
    )
}

fun Project.configureProjekt(
    projekt: IProjekt,
    baseArtifactName: String = projekt.slug,
    artifactVersion: String = projekt.version,
) {
    group = projekt.owner.namespace
    version = projekt.version
    base {
        this as BaseExt
        archivesName = baseArtifactName
    }
    java {
        this as JavaExt
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(projekt.javaVersion))
            vendor.set(JvmVendorSpec.ADOPTIUM)
            implementation.set(JvmImplementation.VENDOR_SPECIFIC)
        }
        withSourcesJar()
        withJavadocJar()
    }
    kotlin {
        this as KotlinExt
        jvmToolchain(projekt.javaVersion)
    }
    tasks.withType<JavaCompile>().configureEach {
        with(options) {
            release.set(projekt.jvmTarget.toInt())
            encoding = Charsets.UTF_8.toString()
        }
    }
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(projekt.jvmTarget)
        }
    }
    tasks.named<Jar>("jar") {
        from("LICENSE") {
            rename { oldName ->
                oldName + Constants.Char.UNDERSCORE + projekt.slug
            }
        }
        archiveVersion.set(artifactVersion)
    }
    sourceSets {
        this as SourceSetsExt
        named("main") {
            val generatedDirectory = "src/main/generated"

            resources.srcDirs(generatedDirectory)
            java.srcDirs("$generatedDirectory/java")
        }
    }
}

fun Project.configureGradlePlugin(
    owner: GithubOwner = MainDeveloper,
    publishingTarget: PublishingTarget?,
    tags: Set<String> = emptySet(),
    license: License = MitLicense,
): GradlePlugin {
    val plugin = projekt(owner, license).toGradlePlugin()
    gradlePlugin {
        this as GradlePluginExt
        website.set(owner.getRepositoryUrl(plugin.slug))
        vcsUrl.set(owner.getRepositoryUrl(plugin.slug, isVcsUrl = true))
        plugins.create(plugin.id) {
            id = plugin.id
            implementationClass = plugin.packageName + Constants.Char.DOT + plugin.className + "GradlePlugin"

            displayName = plugin.name
            description = plugin.description

            if (tags.isNotEmpty()) {
                this.tags.set(tags)
            }
        }
    }
    configureProjekt(plugin)
    configurePublishing(plugin, publishingTarget)
    return plugin
}

fun Project.configureLibrary(license: License = MitLicense): IProjekt {
    val library = projekt(LibrariesOrganization, license).toLibrary()
    configureProjekt(library)
    configurePublishing(library, PublishingTarget.MAVEN_CENTRAL)
    return library
}

fun Project.configureMinecraftMod(
    minecraftVersion: String,
    environment: ModEnvironment,
    loader: ModLoader,
    isFabricApiRequired: Boolean,
    modrinthProjectId: String,
    license: License = MitLicense,
): IProjekt {
    requirePlugins("org.jetbrains.kotlin.plugin.serialization")
    if (loader == ModLoader.FABRIC) {
        requirePlugins("fabric-loom")
    }
    idea {
        this as IdeaExt
        module {
            isDownloadJavadoc = true
            isDownloadSources = true
        }
    }
    val mod = projekt(MinecraftOrganization, license, MinecraftJvmHelper.getJvmTarget(minecraftVersion)).toMinecraftMod(
        modrinthProjectUrl = ModrinthUtils.getProjectUrl(modrinthProjectId),
    )
    configureBuildConfig(mod.packageName, "MinecraftMod") {
        val modId by mod.id.toAutoNamedProperty(ScreamingSnakeCase)
        val modName by mod.name.toAutoNamedProperty(ScreamingSnakeCase)
        listOf(modId, modName)
    }
    if (loader == ModLoader.FABRIC) {
        val mixins = environment
            .getSourceSets()
            .mapNotNull { sourceSet ->
                val pathBase = "fabric/src/${sourceSet.logicalName}/java/${mod.packagePath}/mixins"
                val files = getProjectFileNamesFrom(
                    if (sourceSet == SourceSet.MAIN) pathBase
                    else "$pathBase/${sourceSet.logicalName}"
                ).ifEmpty { null }
                files?.let { sourceSet to it }
            }
            .toMap()
        val dataGenerators = getProjectFileNamesFrom("fabric/src/datagen/kotlin/${mod.packagePath}").map {
            mod.packageName + Constants.Char.DOT + it
        }
        dependencies {
            minecraft("com.mojang:minecraft:$minecraftVersion")
            modImplementation("net.fabricmc:fabric-loader:${getCatalogVersionOrThrow("fabric-loader")}")

            val yarnVersion = resolveCatalogVersion("fabric-yarn") { "$minecraftVersion+build.$it" }
            mappings("net.fabricmc:yarn:$yarnVersion:v2")

            val kotlinModVersion = resolveCatalogVersion("fabric-kotlin") { "$it+kotlin.${mod.kotlinVersion}" }
            modImplementation("net.fabricmc:fabric-language-kotlin:$kotlinModVersion")

            if (isFabricApiRequired) {
                val apiVersion = resolveCatalogVersion("fabric-api") { "$it+$minecraftVersion" }
                modImplementation("net.fabricmc.fabric-api:fabric-api:$apiVersion")
            }
        }
        fabric {
            this as FabricExt
            splitEnvironmentSourceSets()
            mods {
                create(mod.id) {
                    sourceSets {
                        this as SourceSetsExt
                        environment.getSourceSets().forEach { sourceSet ->
                            sourceSet(getByName(sourceSet.logicalName))
                        }
                    }
                }
            }
            runs {
                ModSide.entries.forEach { side ->
                    named(side.logicalName) {
                        val hasSide = environment.sides.contains(side)
                        ideConfigGenerated(hasSide)

                        if (hasSide) {
                            name = "Fabric ${side.logicalName.capitalizeFirstChar()}"
                            runDir = "fabric/run/${side.logicalName}"
                            when (side) {
                                ModSide.CLIENT -> client()
                                ModSide.SERVER -> server()
                            }
                            programArgs("--username", "${MainDeveloper.name}-${side.logicalName}")
                            vmArgs("-Xms2G", side.getMaxMemoryJvmArgument())
                        }
                    }
                }
            }
            accessWidenerPath.set(file("fabric/src/main/resources/${mod.id}.accesswidener"))
        }
        if (dataGenerators.isNotEmpty()) {
            fabric {
                this as FabricExt
                runs {
                    create("data") {
                        name = "Data Generation"
                        runDir = "fabric/datagen"
                        environment("server")
                        vmArgs(
                            "-Dfabric-api.datagen",
                            "-Dfabric-api.datagen.output-dir=${file("src/main/generated")}",
                            "-Dfabric-api.datagen.modid=${mod.id}",
                            "-Xms2G",
                            "-Xmx4G",
                        )
                    }
                }
            }
            fabricApi {
                this as FabricApiExt
                configureDataGeneration {
                    client = true
                }
            }
        }
        val generateFabricConfigTask by tasks.registering {
            val modConfigFile = getBuildFile("generated/resources/fabric/fabric.mod.json")
            val mixinsConfigFile = getBuildFile("generated/resources/fabric/${mod.id}.mixins.json")
            outputs.files(modConfigFile, mixinsConfigFile)
            doLast {
                modConfigFile.get().asFile.apply {
                    parentFile.mkdirs()
                    writeText(
                        Json { prettyPrint = true }.encodeToString(
                            FabricModConfig.of(
                                mod = mod,
                                environment = environment,
                                minecraftVersion = minecraftVersion,
                                loaderVersion = getCatalogVersionOrThrow("fabric-loader"),
                                isApiRequired = isFabricApiRequired,
                                dataGenerators = dataGenerators,
                            )
                        )
                    )
                }
                mixinsConfigFile.get().asFile.apply {
                    parentFile.mkdirs()
                    writeText(
                        Json { prettyPrint = true }.encodeToString(
                            MixinsConfig.of(
                                mod = mod,
                                mixins = mixins,
                            )
                        )
                    )
                }
            }
        }
        tasks.named<ProcessResources>("processResources") {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            from(generateFabricConfigTask)
        }
    } else if (loader == ModLoader.FORGE) {
        dependencies {
            implementation("net.minecraftforge:forge:$minecraftVersion-${getCatalogVersionOrThrow("minecraft-forge")}")
        }
        forge {
            this as ForgeExt
            mappings("parchment", "$minecraftVersion-${getCatalogVersionOrThrow("parchment")}")
            runs {
                configureEach {
                    systemProperty("forge.logging.console.level", "debug")
                    systemProperty("eventbus.api.strictRuntimeChecks", "true")
                }
                environment.sides.forEach { side ->
                    create(side.logicalName) {
                        workingDir.set(layout.projectDirectory.dir("forge/run/${side.logicalName}"))
                        args("--username", "${MainDeveloper.name}-${side.logicalName}")

                        jvmArgs("-Xms2G", side.getMaxMemoryJvmArgument())

                        if (side == ModSide.SERVER) {
                            args("--nogui")
                        }
                    }
                }
                create("data") {
                    workingDir.set(layout.projectDirectory.dir("forge/datagen"))
                    args(
                        "--mod", mod.id,
                        "--all",
                        "--output", file("src/generated/resources").toString(),
                        "--existing", file("src/main/resources").toString(),
                    )
                    jvmArgs("-Xms2G", "-Xmx8G")
                }
            }
        }
        sourceSets {
            this as SourceSetsExt
            configureEach {
                val directory = getBuildDirectory("sourcesSets/$name")
                output.setResourcesDir(directory.get().asFile)
                java.destinationDirectory.set(directory)
            }
        }
    }
    configureProjekt(
        projekt = mod,
        artifactVersion = buildString {
            append(loader.logicalName)
            append(Constants.Char.HYPHEN)
            append(mod.version)
            append(Constants.Char.PLUS)
            append(MinecraftConstants.SHORT_NAME)
            append(minecraftVersion)
        }
    )
    configurePublishing(mod, PublishingTarget.MODRINTH)
    return mod
}

fun Project.configureAndroidApp(license: License = MitLicense): IProjekt {
    val app = projekt(AndroidOrganization, license).toAndroidApp()
    configureProjekt(app)
    configurePublishing(app, PublishingTarget.GOOGLE_PLAY)
    return app
}

fun Project.configurePublishing(projekt: IProjekt, target: PublishingTarget?) {
    if (target == null) {
        println("Publishing target is null, skip.")
        return
    }
    when (target) {
        PublishingTarget.GITHUB_PACKAGES -> configureGithubPackagesPublishing(projekt)
        PublishingTarget.MAVEN_CENTRAL -> configureMavenCentralPublishing(projekt)
        PublishingTarget.MODRINTH -> configureModrinthPublishing(projekt)
        PublishingTarget.GRADLE_PLUGIN_PORTAL -> {}
        PublishingTarget.GOOGLE_PLAY -> {}
    }
}

private fun Project.configureGithubPackagesPublishing(project: IProjekt) {
    val githubOwner = project.owner as? GithubOwner ?: gradleError(
        "Attempted to configure publishing to GitHub Packages, " +
                "but the owner type is ${project.owner::class.className()}"
    )
    val githubPackagesToken = Secrets.githubPackagesToken
    if (githubPackagesToken == null) {
        println("Skipping Github Packages publishing configuration: token is missing")
        return
    }
    publishing {
        this as PublishingExt
        publications.withType<MavenPublication> {
            artifactId = project.slug
        }
        repositories {
            maven(githubOwner.getPackagesMavenUrl(project.slug)) {
                credentials {
                    username = MainDeveloper.name
                    password = githubPackagesToken
                }
            }
        }
    }
}

private fun Project.configureMavenCentralPublishing(project: IProjekt) {
    val gpgKey = Secrets.gpgKey
    val gpgPassphrase = Secrets.gpgPassphrase
    if (gpgKey == null || gpgPassphrase == null) {
        println("Skipping Maven Central publishing configuration: GPG keys are missing")
        return
    }
    publishing {
        this as PublishingExt
        repositories {
            maven(getBuildDirectory("staging-repo").get().asFile)
        }
    }
    val publication = publishing {
        this as PublishingExt
        publications.create<MavenPublication>(project.slug) {
            artifactId = project.slug
            from(components["java"])
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set(project.owner.getRepositoryUrl(project.slug))
                licenses {
                    license {
                        project.license.let {
                            name.set(it.displayName)
                            url.set(it.getUrl())
                        }
                    }
                }
                developers {
                    developer {
                        project.owner.name.let {
                            id.set(it)
                            name.set(it)
                        }
                        email.set(project.owner.email)
                    }
                }
                scm {
                    url.set(project.owner.getRepositoryUrl(project.slug))
                    connection.set(
                        project.scm.buildUri(project.owner.getRepositoryUrl(project.slug, isVcsUrl = true))
                    )
                    developerConnection.set(
                        project.scm.buildUri(
                            project.softwareForge.getSshAuthority(),
                            project.owner.getRepositoryPath(project.slug, isVcsUrl = true)
                        )
                    )
                }
            }
        }
    }
    signing {
        this as SigningExt
        useInMemoryPgpKeys(gpgKey, gpgPassphrase)
        sign(publication)
    }
}

fun Project.configureModrinthPublishing(project: IProjekt) {
    modrinth {
        this as ModrinthExt
        projectId.set(project.slug)
    }
}
