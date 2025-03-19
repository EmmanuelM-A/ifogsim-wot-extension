# iFogSim Web of Thing Extension

## Overview
The Internet of Things (IoT) is expanding rapidly, with new devices continuously introduced to enhance 
capabilities. However, this growth presents significant challenges for IoT developers, who must integrate, 
manage, and test a vast and diverse range of devices. IoT systems rely on connecting devices via APIs 
(Application Programming Interfaces) to enable communication and data exchange. As these systems scale, 
manually configuring APIs becomes increasingly complex, time-consuming, and prone to errors. Additionally, 
the lack of standardization across manufacturers results in compatibility issues, making integration even 
more challenging. These inefficiencies slow down development, increase the risk of errors, and complicate 
large-scale deployments.

This project introduces a novel tool designed to automate the integration and testing of IoT devices within 
an application workflow. It leverages IoT Thing Descriptions (TDs) from the Web of Things (WoT) standard, 
which enhances interoperability by providing a structured way to describe devices and their capabilities. 
The tool utilizes Node-RED to construct IoT applications based on an initial design, ensuring that device 
interactions are efficiently managed and logically structured. Once the system is built, it is validated 
through simulation, allowing developers to test, optimize, and refine their applications before real-world 
deployment.

## Features

- **Node-RED Application Translation**: The designed IOT application in Node-RED is translated into an 
iFogSim acceptable format, known as the application topology file. This file is later used as a blueprint to 
construct the application.

- **Virtual Device Creation**: Converts WoT Thing Descriptions into virtual devices (VDs).

- **Automatic Construction of IoT Systems**: Using the application topology file and the VDs, the application's physical
topology ang logical model are formed and then simulated.

- **Custom Performance Checks**: Allows developers to define their own performance metrics which will added to the
simulation and evaluated once its has been completes.

- **Analytical Support**: A written document which explains how to use the simulation data to optimise your application.
This document can be found here: [Analytical Support Document](src/com/extensions/simulation/analytical-support.md)

## Getting Started

### Prerequisites

Before setting up the project, ensure you have the following installed:

- **Java Development Kit (JDK) 8 or later** – Required to compile and run the simulation.

- **IntelliJ IDEA (or any preferred Java IDE)** – For project development.

- **Git (optional)** – To manage version control.

### Downloading the Project

1. Clone the Repository 

```shell
git clone https://github.com/EmmanuelM-A/ifogsim-wot-extension.git
```

```shell
cd ifogsim-wot-extension
```

2. Download the Source Code:

- Visit the repository or shared source location.

- Download the ZIP file and extract it to your preferred directory.

### Adding JAR Dependencies

1. Opening the project in IntelliJ IDEA

2. Navigate to `File` --> `Project Structure` --> `Modules` --> `Dependencies`

3. Click the `+` icon and select JARs or directories.

4. Locate and add the following JAR files:
   - `jackson-core-2.x.x.jar`
   - `jackson-databind-2.x.x.jar`
   - `iFogSim.jar`

5. Click **Apply** and **Ok**.

### Verifying the Installation

1. Create a simple Java file to test imports:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fog.application.AppModule;

public class TestSetup {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("Jackson and iFogSim JARs are loaded successfully!");
    }
}
```

2. Compile and run the file in your IDE.

3. If no errors occur, your setup is complete.

### Node-RED Setup

1. Install Node-RED

Before proceeding, ensure that Node-RED is installed on your system. If it is not installed, you can install it using 
the following command:

```shell
npm install -g --unsafe-perm node-red
```

For more detailed installation instructions, visit the official [Node-RED installation guide](https://nodered.org/docs/getting-started/).

2. Install the WoT Extension

Next, install the node-red-node-wot extension, which enables Web of Things (WoT) capabilities within Node-RED.

To install, use the following command:

```shell
npm install @thingweb/node-red-node-wot
```
For additional details on using this extension, visit its official page: [@thingweb/node-red-node-wot](https://flows.nodered.org/node/@thingweb/node-red-node-wot).

## Usage

### Node-RED Application Design

Given you have set up Node-RED and the `node-red-node-wot` extension (and gained some familiarity) you can now build
your application. If not visit here [Node-RED Setup]() for instructions for installing Node-RED and all the required
extensions.

When designing your application, you must adhere to these constraints:

1. Single Flow Requirement - Your entire application must be contained within on flow. Multiple flows are not supported.

2. Single input connection - Each node must have exactly one input connection. Nodes with multiple incoming connections
will cause parsing errors.

3. iFogSim Compatibility - All sub-flows must start with a read-property or sub-event node and end with an invoke-action or write-property
node.

4. Sub-flow starting points - All sub-flows must begin with one of the following:
    - Read-property node
    - Subscribe-event node
    - Inject node

5. Once the application is completed, you must export (download) the entire flow (current flow) as a JSON file.

For more detail about these constraints, view the [Technical Support Document](src/com/extensions/simulation/technical-support.md).

### Input Data

1. Thing Descriptions (TDs): JSON files conforming to the WoT standard, representing IoT devices. To be placed in the
`input/things` folder. To check if your TD conforms to the WoT standard, you can use this website
[TD Playground](https://playground.thingweb.io/).

2. Node-RED Application Design: 

### Output Data



## Documentation

For more detailed explanations and technical documentation, check the [Technical Support Document](src/com/extensions/simulation/technical-support.md).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE.txt) file for details.

## Acknowledgements

- **iFogSim**: Simulation framework used for IoT simulation. Check here [iFogSim Toolkit](https://github.com/Cloudslab/iFogSim)
for more details.

- **Web of Things (WoT)**: Standard followed for IoT Thing Descriptions. Check here [Web of Things](https://www.w3.org/WoT/)
for more details.
