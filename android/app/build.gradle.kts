plugins {
    id("com.android.application")
}

android {
    namespace = "br.com.agendafacil.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.com.agendafacil.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Endereço padrão da API. Emulador: 10.0.2.2 = localhost do computador.
        // Para gerar um APK já apontando para o servidor online:
        //   ./gradlew assembleDebug -PAPI_URL=https://meu-servidor.com/
        // (o usuário também pode trocar na tela de login, em "Servidor")
        val apiUrl = (project.findProperty("API_URL") as String?) ?: "http://10.0.2.2:8080/"
        buildConfigField("String", "API_BASE_URL", "\"$apiUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
