plugins {
    id("java")
}

group = "kuse.welbre.ctf"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-math4-legacy:4.0-beta1")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.jetbrains:annotations:24.0.0")
}

tasks.test {
    useJUnitPlatform()
}

//Download commons-math4-legacy and other Dependencies to build/libs
tasks.register<Copy>("copyDependencies") {
    val outputDir = layout.buildDirectory.dir("libs")
    from(configurations.runtimeClasspath)
    into(outputDir)
}