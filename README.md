# TeamCity

**Thundra Foresight is a tool for debugging and troubleshooting test failures in no time and optimize build duration and performance in your CI pipeline.**

You can empower your TeamCity pipeline with Distributed Tracing and  Time-Travel Debugging.

Foresight's TeamCity Plugin automatically changes your build configurations to integrate with Thundra Foresight.

You can integrate your TeamCity pipeline in just 2 steps. First, you need to install the plugin. Then, configure your TeamCity Project or Build Configuration. After completing those steps, Foresight will capture your test runs automatically.


**Prerequisites for TeamCity**

1. [**Thundra Account**](https://start.thundra.io/) to record and manage all the process
2. [**Foresight project**](https://foresight.docs.thundra.io/core-concepts/creating-a-project/core-concepts/creating-a-project) to gather parameters
3. `Thundra API Key` to connect your pipeline with the Thundra Java agent. It can be obtained from the [**project settings page**](https://foresight.docs.thundra.io/core-concepts/managing-your-project-settings).
4. `Thundra Project ID` to connect your test runs with the Foresight project. It can be obtained from the [**project settings page**.](https://foresight.docs.thundra.io/core-concepts/managing-your-project-settings)
   
   **Installing Foresight TeamCity Plugin**
   
1. Go to the TeamCity Administration | Plugins List page
2. Download and enable Foresight plugin
3. Go to your TeamCity Project or Build Configuration and add the `THUNDRA_APIKEY` and `THUNDRA_AGENT_TEST_PROJECT_ID` as environment variables
   
   For further information please visit: [https://foresight.docs.thundra.io/integrations/teamcity](https://foresight.docs.thundra.io/integrations/teamcity)