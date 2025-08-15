// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    configurations.all {
        // Transitive dependencies
        resolutionStrategy {
            force("org.apache.commons:commons-compress:1.28.0")

            eachDependency {
                when (requested.group) {
                    "io.netty" -> useVersion("4.1.124.Final")
                    "org.bouncycastle" -> useVersion("1.81")
                    "io.grpc" -> useVersion("1.74.0")
                    "com.google.protobuf" -> useVersion("3.25.5")
                }
            }
        }
    }
}

allprojects {
    configurations.all {
        // Transitive dependencies
        resolutionStrategy {
            force("org.apache.commons:commons-compress:1.28.0")

            eachDependency {
                when (requested.group) {
                    "io.netty" -> useVersion("4.1.124.Final")
                    "org.bouncycastle" -> useVersion("1.81")
                    "io.grpc" -> useVersion("1.74.0")
                    "com.google.protobuf" -> useVersion("3.25.5")
                }
            }
        }
    }
}