/*
 *  Copyright (C) 2021. Jay Chang
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath(Plugins.ANDROID)
        classpath(Plugins.KOTLIN)
        classpath(Plugins.KTLINT)
        classpath(Plugins.DETEKT)
        classpath(Plugins.ANDROID_JUNIT5)
        classpath(Plugins.MAVEN_PUBLISH)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    // Uses s01.oss.sonatype.org host
    plugins.withId(Plugins.MAVEN_PUBLISH_ID) {
        configure<com.vanniktech.maven.publish.MavenPublishPluginExtension> {
            this.sonatypeHost = com.vanniktech.maven.publish.SonatypeHost.S01
        }
    }
}

subprojects {
    setupLint()
}

configurations.all {
    resolutionStrategy.eachDependency {
        // Force all kotlin artifacts to use the same version.
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(KOTLIN_VERSION)
        }
    }
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}
