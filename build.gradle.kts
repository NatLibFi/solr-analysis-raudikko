plugins {
    `java-library`
    `java-library-distribution`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

val baseVersion = System.getProperty("baseVersion") ?: "0.2.0"
val solrVersion = System.getProperty("solrVersion") ?: "9.1.1"
val raudikkoVersion = System.getProperty("raudikkoVersion") ?: "0.1.1"

group = "fi.nationallibrary.solr"
version = "$baseVersion-solr$solrVersion"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.apache.solr:solr-core:$solrVersion")
    compileOnly("org.apache.lucene:lucene-core")
    //compileOnly("org.apache.commons:commons-lang3")
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
