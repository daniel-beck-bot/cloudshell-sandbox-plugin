# Sandbox-Jenkins-Plugin

[![Stories in Ready](https://badge.waffle.io/QualiSystems/Sandbox-Jenkins-Plugin.svg?label=ready&title=Ready)](http://waffle.io/QualiSystems/Sandbox-Jenkins-Plugin)
[![Dependency Status](https://dependencyci.com/github/QualiSystems/Sandbox-Jenkins-Plugin/badge)](https://dependencyci.com/github/QualiSystems/Sandbox-Jenkins-Plugin)

##Prerequisite

1) CloudShell 7.0 and above, 'CloudShell Sandbox API' component must be installed.

2) Jenkins server 2.0 and above.

##Architecture

1) open port between Jenkins Slaves and the CloudShell Web Server (82 by default but configurable)

Distributed architecture:

![Alt text](Pics/Jenkinspluginarchitecture.jpg?raw=true)

## Installation
1) Download the hpi package from the releases tab

2) Navigate to the advanced section under the plugins tab in jenkins

3) Upload the hpi file into the "upload plugin" section

4) Restart jenkins

## Configuring CloudShell in Jenkins
1) Navigate to the main Jenkins settings page

2) Fill all fields under "cloudshell configuration" section.

![Alt text](Pics/mainsetting.png?raw=true)

### Adding build steps
Use a pre-scm step to start a sandbox and a post-build step for stopping running sandboxes.

node: make sure to check the "Fail the build on error" when using the pre-scm step, this will fail the build in case the sandbox will fail to create.

Pre-scm step:

![Alt text](Pics/PreSCM.png?raw=true)

Build step with parameters and the option to choose specific domain for the blueprint
![Alt text](Pics/startblueprint80.png?raw=true)

Post build step:

![Alt text](Pics/postBuild.png?raw=true)

### Pipeline support (Workflow) - New!
The "startSandbox" and "stopSandbox" steps provide an easy way to control the lifecycle of CloudShell 
sandboxes. You can use these steps to start a sandbox, execute some test code on it, then end it.
![Alt text](Pics/pipeline.png?raw=true)

### Pipeline Scope Example:
The "WithSandbox" step provides an alternative syntax which makes it easy to execute some code in the context of a Sandbox.
The code passed in the closure will be guaranteed to run after the sandbox is up and ready and the sandbox teardown will be taken care
of automatically upon exiting the scope.
![Alt text](Pics/PipelineScope.png?raw=true)


Enjoy
Tomer
