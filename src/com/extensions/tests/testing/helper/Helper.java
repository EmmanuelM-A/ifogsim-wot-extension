package com.extensions.tests.testing.helper;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.distribution.DeterministicDistribution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Helper {
    public static FogDevice createCloudNode(
            String cloudNodeName,
            long mips,
            int ram,
            long upBw,
            long downBw,
            int level,
            double ratePerMips,
            double busyPower,
            double idlePower,
            List<FogDevice> fogDevices
    ) {
        // Create the cloud node
        FogDevice cloudNode = createFogDevice(
                cloudNodeName, mips, ram, upBw, downBw, level, ratePerMips, busyPower, idlePower
        );
        cloudNode.setParentId(-1);
        fogDevices.add(cloudNode);

        return cloudNode;
    }

    public static FogDevice createEdgeNode(
            String edgeNodeName,
            long mips,
            int ram,
            long upBw,
            long downBw,
            int level,
            double ratePerMips,
            double busyPower,
            double idlePower,
            int parentID,
            List<FogDevice> fogDevices
    ) {
        // Create the edge device
        FogDevice edgeNode = createFogDevice(
                edgeNodeName, mips, ram, upBw, downBw, level, ratePerMips, busyPower, idlePower
        );
        edgeNode.setParentId(parentID);
        fogDevices.add(edgeNode);

        return edgeNode;
    }

    public static void createEndDevices(
            String endDeviceName,
            long mips,
            int ram,
            long upBw,
            long downBw,
            int level,
            double ratePerMips,
            double busyPower,
            double idlePower,
            int quantity,
            String appID,
            int userID,
            int parentID,
            List<FogDevice> fogDevices,
            List<Sensor> sensors,
            List<Actuator> actuators
    ) {
        for (int id = 0; id < quantity; id++) {
            // Create the end device
            FogDevice endDevice = createFogDevice(
                    endDeviceName + "-" + id, mips, ram, upBw, downBw, level, ratePerMips, busyPower, idlePower
            );
            endDevice.setParentId(parentID);
            fogDevices.add(endDevice);

            // Create sensor for the end device and connect it to the fog device
            String sensorName = endDeviceName + "_SENSOR";
            Sensor endDeviceSensor = new Sensor(sensorName, sensorName, userID, appID, new DeterministicDistribution(100));
            endDeviceSensor.setGatewayDeviceId(endDevice.getId());
            endDeviceSensor.setLatency(2.0);
            sensors.add(endDeviceSensor);

            // Create an actuator for the end device and connect it to the fog device
            String actuatorName = endDeviceName + "_ACTUATOR";
            Actuator endDeviceActuator = new Actuator(actuatorName, userID, appID, actuatorName);
            endDeviceActuator.setGatewayDeviceId(endDevice.getId());
            endDeviceActuator.setLatency(2.0);
            actuators.add(endDeviceActuator);
        }
    }

    public static FogDevice createFogDevice(String nodeName, long mips, int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {
        List<Pe> peList = new ArrayList<Pe>();

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

        int hostId = FogUtils.generateEntityId();
        long storage = 1000000; // host storage
        int bw = 10000;

        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerOverbooking(bw),
                storage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(busyPower, idlePower)
        );

        List<Host> hostList = new ArrayList<Host>();
        hostList.add(host);

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by now

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        FogDevice fogdevice = null;
        try {
            fogdevice = new FogDevice(nodeName, characteristics,
                    new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
        } catch (Exception e) {
            e.printStackTrace();
        }

        fogdevice.setLevel(level);
        return fogdevice;
    }
}
