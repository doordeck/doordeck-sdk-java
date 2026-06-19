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
                when {
                    requested.group == "io.netty" -> {
                        useVersion(libs.versions.netty.get())
                        because("Various security fixes")
                    }
                    requested.group == "org.bouncycastle" -> {
                        useVersion(libs.versions.bouncycastle.get())
                        because("Timing channel (CVE-2026-5598), LDAP injection and broken-crypto fixes")
                    }
                    requested.group == "org.apache.commons" && requested.name == "commons-lang3" -> {
                        useVersion(libs.versions.commonsLang3.get())
                        because("Uncontrolled recursion fix (CVE-2025-48924)")
                    }
                    requested.group == "org.apache.httpcomponents" && requested.name == "httpclient" -> {
                        useVersion(libs.versions.httpclient.get())
                        because("Cross-site scripting fix (CVE-2020-13956)")
                    }
                }
            }
        }
    }
}

allprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            when {
                requested.group == "io.netty" -> {
                    useVersion(rootProject.libs.versions.netty.get())
                }
                requested.group == "org.bouncycastle" -> {
                    useVersion(rootProject.libs.versions.bouncycastle.get())
                }
                requested.group == "org.apache.commons" && requested.name == "commons-lang3" -> {
                    useVersion(rootProject.libs.versions.commonsLang3.get())
                }
                requested.group == "org.apache.httpcomponents" && requested.name == "httpclient" -> {
                    useVersion(rootProject.libs.versions.httpclient.get())
                }
            }
        }
    }
}