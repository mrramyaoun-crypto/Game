import java.net.URI

plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.ramy.mindgame"
    compileSdk = 35
    defaultConfig { applicationId = "com.ramy.mindgame"; minSdk = 26; targetSdk = 35; versionCode = 6; versionName = "0.5.1-theme-polish" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    testOptions { unitTests.isIncludeAndroidResources = true; unitTests.all { it.maxHeapSize = "2g"; it.systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2"); System.getenv("HTTPS_PROXY")?.let { proxy -> val uri=URI(proxy); it.systemProperty("https.proxyHost",uri.host); it.systemProperty("https.proxyPort",uri.port.toString()); it.systemProperty("http.proxyHost",uri.host); it.systemProperty("http.proxyPort",uri.port.toString()) } } }
    signingConfigs { getByName("debug") { storeFile = rootProject.file("development-signing/mindgame-test.jks"); storePassword = "android"; keyAlias = "androiddebugkey"; keyPassword = "android" } }
    buildTypes { release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
}
dependencies { testImplementation("junit:junit:4.13.2"); testImplementation("org.json:json:20240303"); testImplementation("org.robolectric:robolectric:4.14.1") }
