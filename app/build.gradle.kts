/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

plugins {
    id("com.android.application")
}

android {
    compileSdk = 34
    namespace = "com.netease.yunxin.app.im"

    defaultConfig {
        applicationId = "com.netease.yunxin.app.im"
        minSdk = 24
        versionCode = 1
        versionName = "10.3.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar","*.aar"))))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.multidex:multidex:2.0.1")
    //local module code
//    implementation(project(":contactkit-ui"))
//    implementation(project(":conversationkit-ui"))
//    implementation(project(":teamkit-ui"))
//    implementation(project(":chatkit-ui"))
//    implementation(project(":locationkit"))

    implementation("com.netease.yunxin.kit.contact:contactkit-ui:10.3.0")
    implementation("com.netease.yunxin.kit.conversation:conversationkit-ui:10.3.0")
    implementation("com.netease.yunxin.kit.team:teamkit-ui:10.3.0")
    implementation("com.netease.yunxin.kit.chat:chatkit-ui:10.3.0")
    implementation("com.netease.yunxin.kit.locationkit:locationkit:10.3.0")
    implementation("com.netease.yunxin.kit.call:call-ui:2.2.0") //呼叫组件 UI 包
    implementation("com.netease.nimlib:avsignalling:10.3.0-beta") //信令组件
    implementation("com.airbnb.android:lottie:6.3.0")
    implementation("com.github.bumptech.glide:glide:4.13.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.huawei.hms:push:6.10.0.300")
    implementation("com.meizu.flyme.internet:push-internal:4.1.0")
    implementation("com.huawei.agconnect:agconnect-core:1.7.2.300")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("commons-codec:commons-codec:1.11")
    implementation("androidx.annotation:annotation:1.7.1")

}
