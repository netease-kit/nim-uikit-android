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
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            setUrl("https://developer.huawei.com/repo/")
        }

        maven { setUrl( "https://www.jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            setUrl("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            setUrl("https://developer.huawei.com/repo/")
        }
        maven { setUrl( "https://www.jitpack.io") }
    }
}

rootProject.name = "im-uikit"
include(":app")
include(":conversationkit-ui")
include(":teamkit-ui")
include(":contactkit-ui")
include(":chatkit-ui")
include(":locationkit")
