pluginManagement {
    repositories {
        maven { setUrl("https://maven.aliyun.com/repository/public/") }
        maven { setUrl("https://maven.aliyun.com/repository/gradle-plugin/") }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.alibaba.arouter") {
                useModule("com.github.acme.ARouter:arouter-register:1.0.1")
            }
        }
    }
}
dependencyResolutionManagement {
    repositories {
        maven { setUrl("https://maven.aliyun.com/repository/public/") }
        maven { setUrl("https://maven.aliyun.com/repository/google/") }

        maven {
            setUrl("https://nexus.superacme.com/repository/maven-releases/")
            credentials {
                username = "cloudapp"
                password = "x^5Crp7CZ9qPUeyf"
            }
        }

        maven {
            setUrl("https://nexus.superacme.com/repository/maven-snapshots/")
            credentials {
                username = "cloudapp"
                password = "x^5Crp7CZ9qPUeyf"
            }
        }

        google()
        mavenCentral()
    }
}

rootProject.name = "OpenSdk-Demo"
include(":app")
include(":sm-login")
include(":biz-bind")
include(":home")
include(":settings")
include(":lib-core")
include(":player")