
/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 34
    namespace = "com.netease.yunxin.kit.aisearchkit"
    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "versionName", "\"10.8.5\"")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":chatkit-ui"))
    api("com.netease.yunxin.kit.core:corekit-plugin:1.8.0")

    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.github.bumptech.glide:glide:4.13.1")
}

