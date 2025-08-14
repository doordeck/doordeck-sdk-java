// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    configurations.all {
        // Transitive dependencies
        resolutionStrategy {
            eachDependency {
                when (requested.group) {
                    "io.netty" -> useVersion("4.1.123.Final")
                    "org.bouncycastle" -> useVersion("1.81")
                    "io.grpc" -> useVersion("1.74.0")
                }
            }
        }
    }
}