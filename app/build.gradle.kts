plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.dessertrip"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.dessetripandroid"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // 에뮬레이터에서 내 PC의 서버(localhost:8080)에 접속할 때는 10.0.2.2 를 쓴다
            buildConfigField("String", "BASE_URL", "\"http://192.168.123.109:8080/\"")
            manifestPlaceholders["usesCleartext"] = "true"
        }
        release {
            optimization {
                enable = false
            }
            // TODO: 서버 담당자에게 운영 서버 주소 확인 후 교체
            buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
            manifestPlaceholders["usesCleartext"] = "false"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // 도감/기록하기 코드가 java.time(API 26+)을 쓴다 → minSdk 24 에서도 동작하도록 desugaring 사용
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.fragment)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    // Navigation 2.8+ 는 Kotlin 의 KSerializer 를 API 시그니처에 쓴다. Java 컴파일에도 클래스 파일이 필요해서 따로 추가한다.
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.glide)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.guava) // CameraX ListenableFuture (Java)
    implementation(libs.mlkit.subject.segmentation) // 배경 제거(기기 안에서 처리, 모델은 Google Play 서비스가 제공)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}