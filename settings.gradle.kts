/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */


pluginManagement {
    repositories {
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

rootProject.name = "im-uikit"
include(":app")
include(":common-ui")
include(":aisearchkit")
include(":conversationkit-ui")
include(":localconversationkit-ui")
include(":teamkit-ui")
include(":contactkit-ui")
include(":chatkit-ui")
include(":locationkit")
