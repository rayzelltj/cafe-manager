plugins {
    id("java")
    id("application")
}

group = "org.payroll"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    implementation("com.opencsv:opencsv:5.9")
    implementation("org.apache.pdfbox:pdfbox:3.0.3")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("org.payroll.Main")
}

tasks.test {
    useJUnitPlatform()
}