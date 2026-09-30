plugins {
    alias(wip.plugins.composeMultiplatform) apply false
    alias(wip.plugins.composeCompiler) apply false
    alias(wip.plugins.kotlinMultiplatform) apply false
    // The build conventions: the coordinate, the version, the toolchain, the jvm floor, the style
    // and the test platform, with the numbers in `gradle.properties`. Declared here and applied per
    // module, `apply false` like the rest.
    alias(libs.plugins.sborkaKmp) apply false
    alias(libs.plugins.sborkaLint) apply false
    alias(libs.plugins.sborkaPublish) apply false
}
