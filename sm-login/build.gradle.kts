import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
//    alias(libs.plugins.compose.compiler)
    `maven-publish`
}

var gArtifactId = "common-account-ui"
var groupId = "com.superacme.android"
// .\gradlew.bat :sm-login:assembleRelease;.\gradlew.bat  :sm-login:pMJPTMR
var gVersion = "0.0.4-SNAPSHOT"

android {
    namespace = "com.acme.login"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        buildConfigField("String", "gVersion", "\"$gVersion\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    libraryVariants.configureEach {
        outputs.configureEach {
            val output = this as BaseVariantOutputImpl
            if (output.outputFileName.endsWith(".aar")) {
                output.outputFileName = "${gArtifactId}_${gVersion}.aar"
            }
        }
    }
}

dependencies {



    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.1")

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation(platform("androidx.compose:compose-bom:2024.02.02"))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-runtime-ktx:2.8.4")
    implementation("androidx.fragment:fragment-ktx:1.8.5")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.alibaba:fastjson:1.2.40")
    debugImplementation(libs.androidx.ui.tooling)
    api(project(":lib-core"))
}

sourceSets {
    create("main") {
        java.srcDirs("src/main/java")
    }

}

val sourceJar by tasks.registering(Jar::class) {
//    classifier = "sources"
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(sourceSets["main"].allSource)
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {

            println("andymao->" + components.asMap)

            val gArtifactId = "common-account-ui"
            val groupId = "com.superacme.android"
            this.artifactId = gArtifactId
            this.groupId = groupId
            this.version = gVersion
//            artifact(tasks["overseaReleaseSourcesJar"])//打包 jar
            artifact(sourceJar.get())
            artifact("$buildDir/outputs/aar/${gArtifactId}_${version}.aar")
//            artifact source: sourceJar, classifier: 'src', extension: 'zip'

//            from(components["overseadebug"])
            //打包 aar
//            afterEvaluate { artifact("$buildDir/outputs/aar/${gArtifactId}_${version}.aar") }
            pom {
                name.set("My Library")
                description.set("A concise description of my library")
                url.set("http://www.example.com/library")
            }
        }
    }

}