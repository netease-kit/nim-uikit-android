/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

plugins {
    id("com.android.application")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.netease.yunxin.app.im"
        minSdk = 21
        targetSdk = 30
        versionCode = 1
        versionName = "9.4.1"
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
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        jniLibs.pickFirsts.add("lib/arm64-v8a/libc++_shared.so")
        jniLibs.pickFirsts.add("lib/armeabi-v7a/libc++_shared.so")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar","*.aar"))))
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.multidex:multidex:2.0.1")
    //local module code
//    implementation(project(":contactkit-ui"))
//    implementation(project(":conversationkit-ui"))
//    implementation(project(":teamkit-ui"))
//    implementation(project(":chatkit-ui"))
//    implementation(project(":searchkit-ui"))
//    implementation(project(":locationkit"))
    implementation("com.netease.yunxin.kit.contact:contactkit-ui:9.4.1")
    implementation("com.netease.yunxin.kit.conversation:conversationkit-ui:9.4.1")
    implementation("com.netease.yunxin.kit.team:teamkit-ui:9.4.1")
    implementation("com.netease.yunxin.kit.chat:chatkit-ui:9.4.1")
    implementation("com.netease.yunxin.kit.search:searchkit-ui:9.4.1")
    implementation("com.netease.yunxin.kit.locationkit:locationkit:9.4.1")
    implementation("com.netease.yunxin.kit.call:call-ui:1.8.2") //呼叫组件 UI 包
    implementation("com.airbnb.android:lottie:5.0.3")
    implementation("com.github.bumptech.glide:glide:4.13.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.huawei.hms:push:6.3.0.302")
    implementation("com.meizu.flyme.internet:push-internal:4.1.0")
    implementation("com.huawei.agconnect:agconnect-core:1.6.5.300")

    implementation("com.google.code.gson:gson:2.9.0")
    implementation("commons-codec:commons-codec:1.10")
    implementation("androidx.annotation:annotation:1.3.0")

}
