@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.library")
    id("com.bykov.android")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33
    }
}

// Configure common test runtime dependencies for *all* android projects
dependencies {
    // Can't declare dependency from the platform in com.bykov.android convention plugin
    // cause com.android.base doesn't support it
    implementation(platform("com.bykov.footea:platform"))
    androidTestImplementation(platform("com.bykov.footea:platform"))

    // (Required) Writing and executing Unit Tests on the JUnit Platform
    testImplementation("io.kotest:kotest-runner-junit5")
    testImplementation("io.kotest:kotest-assertions-core")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    androidTestRuntimeOnly("androidx.test.espresso:espresso-core")
}
