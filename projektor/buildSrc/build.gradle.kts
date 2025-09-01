plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.plugin)
    compileOnly(libs.build.config.plugin)
    compileOnly(libs.modrinth.minotaur.plugin)
    compileOnly(libs.fabric.loom.plugin)
    compileOnly(libs.forge.plugin)

    implementation(libs.ktor.http)
    implementation(libs.kotlin.utils)
    implementation(libs.kotlin.serialization)

    constraints {
        // Override vulnerable transitive dependency (Okio < 3.4.0, CVE-2023-3635)
        // com.modrinth.minotaur → Modrinth4J → Okio
        implementation(libs.okio)
    }
}

sourceSets.main {
    kotlin {
        srcDirs(
            "../src", // dogfooding trick: include the API code to configure yourself in the build script
            "../gradle/settings" // so IDEs treat these scripts as project sources and provide full IDE support
        )
    }
}
