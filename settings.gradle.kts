pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
//        maven{ url = 'https://jitpack.io' }
//        jcenter(){
//            content{
//                includeModule("com.threatofdev.edmodo","android-image-cropper")
//            }
//        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
//        maven{url= 'https://jitpack.io'}
//        jcenter(){
//            content{
//                includeModule("com.threatofdev.edmodo","android-image-cropper")
//            }
//        }
    }
}

rootProject.name = "eCommerce"
include(":app")
