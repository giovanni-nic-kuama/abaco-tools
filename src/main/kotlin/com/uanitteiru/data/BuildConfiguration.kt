package com.uanitteiru.data

data class BuildConfiguration(
    val configurationName: String,
    val pomFileName: String,
    val engineProjectPath: String,
    val engineJavaModulesNames: List<ModuleWithTenantConfiguration>,
    val bundlesProjectPath: String,
    val configFolderName: String,
    val engineKind: String,
    val bundleName: String,
    val bundleSubFolder: String,
    val autoPushEnabled: Boolean
)

data class ModuleWithTenantConfiguration(
    val moduleName: String,
    val tenant: String
)
