// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}

buildscript {
    configurations.all {
        resolutionStrategy {
            force(libs.jdom)
            force(libs.jose4j)

            eachDependency {
                if (requested.group == "io.netty") {
                    useVersion(libs.versions.netty.get())
                    because("Various security fixes")
                }
            }
        }
    }
}

allprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "io.netty") {
                useVersion(rootProject.libs.versions.netty.get())
                because("Various security fixes")
            }
        }
    }
}