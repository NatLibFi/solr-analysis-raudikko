plugins {
    `java-library`
    `java-library-distribution`
}

val baseVersion = System.getProperty("baseVersion") ?: "0.2.0"
val solrVersion = System.getProperty("solrVersion") ?: "8.11.0"
val raudikkoVersion = System.getProperty("raudikkoVersion") ?: "0.1.1"

group = "fi.nationallibrary.solr"
version = "$baseVersion-solr$solrVersion"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.apache.solr:solr-core:$solrVersion")
    implementation("fi.evident.raudikko:raudikko:$raudikkoVersion")

    testImplementation("org.apache.solr:solr-core:$solrVersion")
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

distributions {
    main {
        distributionBaseName.set("solr-raudikko")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
