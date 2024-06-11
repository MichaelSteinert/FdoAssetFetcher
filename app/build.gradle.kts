plugins {
    application
}

repositories {
    mavenCentral()
    // Custom GitLab repository for 'FDO Manager SDK' dependency
    maven {
        url = uri("https://gitlab.indiscale.com/api/v4/projects/229/packages/maven")
    }
    // Handle.net repository for the 'handle' dependency in 'FDO Manager SDK' dependency
    maven {
        url = uri("https://handle.net/maven/")
    }
}

dependencies {
    implementation(libs.edc.connector.client)
    implementation(libs.guava)
    implementation(libs.fdo.manager.sdk)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

application {
    mainClass.set("fdoassetfetcher.FdoAssetFetcher")
}
