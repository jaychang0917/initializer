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

object Plugins {
    const val KOTLIN = "org.jetbrains.kotlin:kotlin-gradle-plugin:$KOTLIN_VERSION"
    const val KOTLIN_ID = "kotlin"
    const val KOTLIN_ANDROID_ID = "kotlin-android"

    const val ANDROID = "com.android.tools.build:gradle:4.2.1"
    const val ANDROID_APP_ID = "com.android.application"
    const val ANDROID_LIB_ID = "com.android.library"

    const val ANDROID_JUNIT5 = "de.mannodermaus.gradle.plugins:android-junit5:1.4.2.0"
    const val ANDROID_JUNIT5_ID = "de.mannodermaus.android-junit5"

    const val KTLINT = "org.jlleitschuh.gradle:ktlint-gradle:7.4.0"
    const val KTLINT_ID = "org.jlleitschuh.gradle.ktlint"

    const val DETEKT = "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$DETEKT_VERSION"
    const val DETEKT_ID = "io.gitlab.arturbosch.detekt"

    const val MAVEN_PUBLISH = "com.vanniktech:gradle-maven-publish-plugin:0.17.0"
    const val MAVEN_PUBLISH_ID = "com.vanniktech.maven.publish"
}
