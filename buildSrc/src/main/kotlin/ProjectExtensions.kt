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

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

fun Project.setupLibraryModule(androidExtBlock: LibraryExtension.() -> Unit = {}) {
    setupBaseModule<LibraryExtension> {
        testOptions {
            unitTests.isIncludeAndroidResources = true
        }

        androidExtBlock()
    }
}

fun Project.setupAppModule(androidExtBlock: BaseAppModuleExtension.() -> Unit = {}) {
    setupBaseModule<BaseAppModuleExtension> {
        buildFeatures {
            viewBinding = true
        }

        androidExtBlock()
    }
}

private inline fun <reified T : BaseExtension> Project.setupBaseModule(crossinline block: T.() -> Unit = {}) {
    extensions.configure<BaseExtension>("android") {
        compileSdkVersion(AndroidBuilds.COMPILE_SDK_VERSION)

        defaultConfig {
            minSdkVersion(AndroidBuilds.MIN_SDK_VERSION)
            targetSdkVersion(AndroidBuilds.TARGET_SDK_VERSION)
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        testOptions {
            unitTests.all {
                it.useJUnitPlatform()
            }
        }

        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }

        (this as T).block()
    }

    dependencies.addTestDependencies()
}

private fun DependencyHandler.addTestDependencies() {
    val implementations = listOf(
        Libraries.JUNIT4,
        Libraries.JUNIT5,
        Libraries.MOCKK,
        Libraries.TRUTH_ASSERT,
        Libraries.ANDROID_TEST_RUNNER,
    )

    implementations.forEach {
        add("testImplementation", it)
    }
}

private fun BaseExtension.kotlinOptions(block: KotlinJvmOptions.() -> Unit) {
    (this as ExtensionAware).extensions.configure("kotlinOptions", block)
}

fun Project.setupLint() {
    setupKtlint()
    setupDetekt()
}

private fun Project.setupKtlint() {
    apply(plugin = Plugins.KTLINT_ID)
}

private fun Project.setupDetekt(block: DetektExtension.() -> Unit = {}) {
    apply(plugin = Plugins.DETEKT_ID)

    extensions.configure<DetektExtension>("detekt") {
        config = files("$rootDir/lint/detekt-config.yml")
        reports {
            xml {
                enabled = true
                destination = file("build/reports/detekt/lint-results.xml")
            }
            html {
                enabled = false
            }
            txt {
                enabled = false
            }
        }
        autoCorrect = true
        parallel = true

        dependencies.add("detektPlugins", Libraries.DETEKT_FORMATTING)

        block()
    }
}
