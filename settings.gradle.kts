pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven {
            // TODO: Replace the repository group and credentials before publishing publicly.
            val issuerRepoGroup = "mea-guest-group"
            val user = "mea-guest-user"
            val pass = "t35tMeN0W:)"

            url = uri("https://nexus.ext.meawallet.com/repository/$issuerRepoGroup/")

            credentials {
                username = user
                password = pass
            }
        }
    }
}

rootProject.name = "issuer-pay-ui-sample"
