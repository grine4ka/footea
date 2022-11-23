plugins {
    id("com.bykov.android-library")
}

android {
    namespace = "ru.bykov.footballteams.data"
}

dependencies {

    implementation(project(":domain"))

    // Network
    implementation("com.squareup.retrofit2:retrofit")
    implementation("com.squareup.retrofit2:converter-gson")

    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
}