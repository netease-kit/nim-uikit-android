/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */


pluginManagement {
    repositories {
        maven {
            setUrl("https://yunxin-maven-repo.netease.im/repository/maven-public/")
        }
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            setUrl("https://central.sonatype.com/repository/maven-snapshots/")
        }
        maven {
            setUrl("https://developer.huawei.com/repo/")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven {
            setUrl("https://yunxin-maven-repo.netease.im/repository/maven-public/")
        }
        google()
        mavenCentral()
        maven {
            setUrl("https://central.sonatype.com/repository/maven-snapshots/")
        }
        maven {
            setUrl("https://developer.huawei.com/repo/")
        }
    }
}

rootProject.name = "nim-uikit-android"
include(":app")
include(":common-ui")
include(":aisearchkit")
include(":conversationkit-ui")
include(":localconversationkit-ui")
include(":teamkit-ui")
include(":contactkit-ui")
include(":chatkit-ui")
include(":locationkit")
