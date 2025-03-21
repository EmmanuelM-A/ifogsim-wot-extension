# Technical Support Document for iFogSim-WoT Extension

## 1. Introduction

### 1.1 Purpose of the Document

This document provides a detailed explanation of the code, key classes, and common errors in the project, along with 
their solutions. It is intended to assist developers and users in understanding how to use and troubleshoot the system.

### 1.2 Scope

This document covers the key components of the code, including class explanations, functions, and error handling. 
It also includes common errors, troubleshooting steps, and performance optimization tips.

## 2. Code Overview

### 2.1 System Architecture

[Component Diagram]

Key components:

- Virtual Device Creation - Responsible for converting the Thing Descriptions (TDs) into virtual devices (VDs).

- Node-RED Application Translation - Responsible for translating the Node-RED Application into an iFogSim acceptable
format (known as the application topology) which acts a blueprint to create the application.

- Automatic System Construction - Using the VDs and application topology, the application's physical topology and 
logical model.

- Custom Performance Metrics Manager - Handles all the creation and registration of custom performance metrics.


### 2.2 Key Classes and Functions

#### **CustomController**
- **Purpose**: Responsible for collecting all simulation data and outputting it once the simulation is complete. Very
similar to iFogSim's `Controller` class but incorporates the `CustomMetricsManager` class and its methods.

- **Key Methods/Functions**:
    - `recordSimulationData()`: Records all the simulation data and stores it in `SimulationData` instance for use
    elsewhere.
    - `processEvent(SimEvent ev)`: Processes few types of event. One in particular, is the `FogEvents.STOP_SIMULATION`
    sim event which on event will trigger the class to record all simulation results and evaluate any custom metrics.

#### **JsonToApplication**
- **Purpose**: Responsible for constructing the application from the application topology. Implements the
`ApplicationConstructor` interface.

- **Key Methods/Functions**:
  - `createApplicationModel(appId, userId)`: Creates the application/logical model for the application.

  - `createApplicationPhysicalTopology(virtualDevices)`: Constructs the application's physical topology base of two
  approaches depending on weather you use the thing quantities file or not.
    - Both approaches follow the structure CLOUD <----> EDGE NODES <----> VDs
    - (1) Structures the topology with the cloud node at
    level 1, the edge nodes defined in the thing quantities file at level 2 and finally the VDs at level 3 connected to
    their specific edge nodes. 
    - (2) Is the default topology, when no thing quantity file is present. It has a cloud node
    at level 1, a number of edge nodes (based of the number of VDs) at level 2 and finally the VDs distributed amongst the
    edge nodes.

#### **NodeREDGenerator**
- **Purpose**: Responsible for generating the application topology from the list parsed node red application.
- **Key Methods/Functions**:
  - `generateSubFlowTrees(...)`: Generates a JSON representation of the sub-flows into a D.S similar to a tree.

#### **NodeRedParser**
- **Purpose**: Parses the JSON file that represents the Node-RED Application.
- **Key Methods/Functions**:
  - `process(File file)`: Takes a JSON file and iterates through all nodes to produce a list `NodeRedNodes`.

#### **CustomMetricsManager**
- **Purpose**: Manages all custom metrics and their evaluation at the end of the simulation.
- **Key Methods/Functions**:
  - `registerMetric(CustomPerformanceMetric<?> metric)`: Registers a custom performance metric to the manager.
  - `evalauteMetrics(SimulationData simulationData)`: Runs all registered custom metrics and prints their results after
  the simulation has finished.

#### **ApplicationTopologyParser**
- **Purpose**: Parses the application topology file, extracting all the necessary information to form the application.
- **Key Methods/Functions**:
  - `process(File file)`: Parses a json file and returns all the required data to construct the application.

#### **VirtualDeviceConfigParser**
- **Purpose**: Parses any virtual device configuration files found in the application. The VD config files are used
to configure virtual devices.
- **Key Methods/Functions**:
  - `process(File file)`: Parses the json file and returns all the VD configs found in the file.

#### **ThingQuantityParser**
- **Purpose**: Parses the thing quantity file to extract the quantity of each thing (VD) used in the application and
also determines which edge nodes they belong to.
- **Key Methods/Functions**:
  - `process(File file)`: Parses the json file and returns the frequencies of all things and their connections to their
  respective edge nodes.

#### **Simulation**
- **Purpose**: Handles everything needed to create the application and its simulation.

- **Key Methods/Functions**:
    - `setupApplication(...)`: Sets up the application and all its components.
    - `run`: Runs the simulation
    - `registerMetric(...)`: Registers any custom metric

#### **VirtualDeviceFactory**
- **Purpose**: Responsible for creating all virtual devices from the folder that stores the thing descriptions.

- **Key Methods/Functions**:
  - `createVirtualDevices(...)`: Creates a list of virtual devices from the thing descriptions.

#### **Thing Description**
- **Purpose**: Represents a Thing Description for a WoT-compliant device, used as intermediately D.S to store the 
parsed metadata from a Thing Description JSON file.

#### **FileProcessor**
- **Purpose**: An generic interface designed to process files. All parsers implement this method.
- **Parameters**: Only has one parameter - A file of type File.
- **Return Type**: The type specified by the user upon implementation.

#### **All Presets**
- **Purpose**: Used as configurations to configure entities like sensors and actuators as well as the constructed
application and virtual devices. Allows users to easily modify all entities as needed. Located in the 
`utils/preset` folder.

## 3. Common Errors and Solutions

### 3.1 Error 1: Virtual Device Configuration Error
- **Description**: Likely a formatting issue with VD Config file or an attribute is missing from the file.
- **Solution**: Check the config to make sure its format is correct.

### 3.2 Error 2: Thing Quantity Error
- **Description**: The thing-quantity file is malformed, missing or has an invalid formatting.
- **Solution**: Check the config to make sure its format is correct.

### 3.3 Error 3: Event Sensor Error
- **Description**: The corresponding virtual device that represents the node red node that starts the event flow in
Node-RED is missing.
- **Solution**: In the `Simulation` class check that all event sensors are setting their application value or make sure
the VD that corresponds to that node is present (node should reference a thing and that thing's TD must be in thing repo
folder).

### 3.4 Error 3: Thing Description Error
- **Description**: When a TD is either malformed, missing attributes or not present.
- **Solution**: Ensure the TD conforms to the WoT standard and all actions, properties and events are present even if it
means they are empty.

### 3.5 Error 3: Application Construction Error
- **Description**: When the first node of a sub flow does not start with a read-prop, inject or a sub-event node or
the last node does not end in as a write-prop or an invoke-action node.
- **Solution**: Read over the Node-RED application design rules: [Node-RED Application Design](../../../../README.md#node-red-setup).

## 4. Troubleshooting

### 4.1 General Troubleshooting Steps
1. **Step 1**: Verify that all system dependencies and configurations are correctly set.
2. **Step 2**: Check that all required files are present and correctly formatted.
3. **Step 3**: Ensure that libraries and frameworks are correctly installed and imported.
4. **Step 4**: Look for additional error messages in log files.
5. **Step 5**: Review class and method logs for further debugging.

### 4.2 Debugging Tips
- Use print statements or a debugger to track variable values and flow.
- Check for common bugs such as uninitialized variables, missing imports, or incorrect file paths.
- Review the documentation for any functions/classes you are unsure about.


## 5 How to Extend the Code

### 5.1 Adding New Features
- **Step 1**: Review the current system to identify where the feature should be added.
- **Step 2**: Implement the feature using existing classes, interfaces and methods where possible.
- **Step 3**: Ensure new code follows the existing code style and structure.

### 5.2 Modifying Existing Functions or Classes
- Carefully modify functions/classes while maintaining compatibility with existing code.
- Update method signatures and document changes in comments.

