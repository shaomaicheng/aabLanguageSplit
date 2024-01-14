

pluginManagement {
    repositories {
        maven {
            url = java.net.URI("https://maven.aliyun.com/repository/google")
        }
        maven {
            url = java.net.URI("https://maven.aliyun.com/repository/public")
        }
        maven {
            url = java.net.URI("https://maven.aliyun.com/repository/central")
        }
        maven {
            url = java.net.URI("https://maven.aliyun.com/repository/jcenter")
        }
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            url = java.net.URI("https://maven.aliyun.com/repository/google")
        }
        maven {
            url = java.net.URI("https://maven.aliyun.com/repository/public")
        }
        maven {
            url = java.net.URI("https://maven.aliyun.com/repository/central")
        }
        maven {
            url = java.net.URI("https://maven.aliyun.com/repository/jcenter")
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "languageSplit"
include(":app")
