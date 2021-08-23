# thundra-teamcity-plugin
This plugin used for change your Maven and Gradle build configurations for send test informations to Thundra Foresight testing observability application.
## Usage

Information about available parameters is listed [below](#parameters). 

The required parameters are the Thundra API Key and the Thundra Project ID, which can be obtained from [foresight.thundra.io](https://foresight.thundra.io/). 
You can add this Configuration Parameters with 2 ways.
- **Click Project Name -> Edit Project Settings -> Parameters -> Add Parameter -> Configuration Parameter**
- **Click Build Configuration -> Edit Configuration Settings -> Parameters -> Add Parameter -> Configuration Parameter**

You can learn more about Thundra at [thundra.io](https://thundra.io)

Once you get the Thundra API Key, make sure to set it as a secret.

## Parameters

| Name                  | Requirement       | Description
| ---                   | ---               | ---
| foresightAPIKey                | Required          | Thundra API Key
| foresightProjectKey            | Required          | Your project id from Thundra Foresight. Will be used to filter and classify your testruns.
| thundraGradlePluginVersion        | Optional          | In the plugin itself, we use a Gradle plugin to run your tests. This plugin is released and versioned separately from the teamcity plugin. Hence, if there is some breaking change or specific version you want to use, you can use it by defining this parameter. You can see all the available version of our plugin [here](https://search.maven.org/artifact/io.thundra.agent/thundra-agent-gradle-test-instrumentation).

