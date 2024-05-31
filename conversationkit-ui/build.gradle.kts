
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
    namespace = "com.netease.yunxin.kit.conversationkit.ui"

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
        buildConfigField("String", "versionName", "\"10.1.1\"")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets["main"].res.srcDirs("src/main/res","src/main/res-fun","src/main/res-normal")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api("com.netease.yunxin.kit.chat:chatkit:10.1.1")
    api("com.netease.yunxin.kit.common:common-ui:1.3.5")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")
    implementation("androidx.appcompat:appcompat:1.5.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.bumptech.glide:glide:4.13.1") 

}

