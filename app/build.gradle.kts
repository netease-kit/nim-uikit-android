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
        targetSdk = 34
        versionCode = 1
        versionName = "10.6.3"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
        }
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

    packagingOptions {
        jniLibs.pickFirsts.add("lib/arm64-v8a/libc++_shared.so")
        jniLibs.pickFirsts.add("lib/armeabi-v7a/libc++_shared.so")
    }

    splits {
        abi {
            reset()
            include("armeabi-v7a", "arm64-v8a")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar","*.aar"))))
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.multidex:multidex:2.0.1")
    //本地代码依赖
//    implementation(project(":contactkit-ui"))
//    implementation(project(":conversationkit-ui"))
//    implementation(project(":localconversationkit-ui"))
//    implementation(project(":teamkit-ui"))
//    implementation(project(":chatkit-ui"))
//    implementation(project(":locationkit"))
//    implementation(project(":aisearchkit"))

    //远端aar依赖
    implementation("com.netease.yunxin.kit.contact:contactkit-ui:10.6.3")
    implementation("com.netease.yunxin.kit.conversation:conversationkit-ui:10.6.3")
    implementation("com.netease.yunxin.kit.localconversation:localconversationkit-ui:10.6.3")
    implementation("com.netease.yunxin.kit.team:teamkit-ui:10.6.3")
    implementation("com.netease.yunxin.kit.chat:chatkit-ui:10.6.3")
    implementation("com.netease.yunxin.kit.locationkit:locationkit:10.6.3")
    implementation("com.netease.yunxin.kit.aisearchkit:aisearchkit:10.6.3")

    implementation("com.netease.yunxin.kit.call:call-ui:3.5.0") //呼叫组件 UI 包
    implementation("com.netease.nimlib:avsignalling:10.8.21") //信令组件
    implementation("com.airbnb.android:lottie:5.0.3")
    implementation("com.github.bumptech.glide:glide:4.13.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.huawei.hms:push:6.10.0.300")
    implementation("com.meizu.flyme.internet:push-internal:4.1.0")
    implementation("com.huawei.agconnect:agconnect-core:1.7.2.300")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("commons-codec:commons-codec:1.11")
    implementation("androidx.annotation:annotation:1.7.1")

}
