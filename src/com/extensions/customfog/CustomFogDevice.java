package com.extensions.customfog;

import com.extensions.sysconstructor.eventdriver.EventTuple;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppModule;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a custom {@code FogDevice} which has the added ability to process event tuples along with regular
 * tuples. It extends iFogSim's {@code FogDevice} class.
 */
public class CustomFogDevice extends FogDevice {
    /**
     * The name of the fog device.
     */
    private String name;

    /**
     * Constructs a CustomFogDevice instance with the given parameters
     *
     * @param name The name of the fog device.
     * @param characteristics The characteristics of the fog device, such as CPU, RAM, and storage capabilities.
     * @param vmAllocationPolicy The policy for allocating virtual machines (VMs) within the fog device.
     * @param storageList A list of storage devices associated with the fog device.
     * @param schedulingInterval The interval at which the scheduler updates resource allocations.
     * @param uplinkBandwidth The maximum bandwidth available for uplink communication.
     * @param downLinkBandwidth The maximum bandwidth available for down link communication.
     * @param uplinkLatency The latency for uplink communication in milliseconds.
     * @param ratePerMips The cost rate per Million Instructions Per Second (MIPS).
     * @throws Exception If an error occurs during the creation of the fog device.
     */
    public CustomFogDevice(String name, FogDeviceCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval, double uplinkBandwidth, double downLinkBandwidth, double uplinkLatency, double ratePerMips) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downLinkBandwidth, uplinkLatency, ratePerMips);
        this.name = name;
    }

    /**
     * Retrieves the name of the fog device.
     * @return The name of the fog device.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the fog device.
     * @param name The new name of the fog device.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected void processTupleArrival(SimEvent ev) {
        Object data = ev.getData();

        // Check if it's an instance of EventTuple
        if (data instanceof EventTuple) {
            // Process as event
            processEventTupleArrival(ev);
        } else if (data instanceof Tuple) {
            // Default iFogSim Tuple Handling
            super.processTupleArrival(ev);
        } else {
            System.out.println("Warning: Received an unknown tuple type: " + data.getClass().getName());
        }
    }

    /**
     * Process incoming event tuples. This method is very similar to the processTupleArrival method (from the parent), with the
     * exception that it handles EVENT tuples rather than regular tuples.
     * @param ev The SimEvent the tuple comes form.
     */
    public void processEventTupleArrival(SimEvent ev) {
        // Only part of the code I changed from the parent method
        EventTuple eventTuple = (EventTuple) ev.getData();

        if (getName().equals("cloud")) {
            updateCloudTraffic();
        }

		/*if(getName().equals("d-0") && tuple.getTupleType().equals("_SENSOR")){
			System.out.println(++numClients);
		}*/
        Logger.debug(getName(), "Received tuple " + eventTuple.getCloudletId() + "with tupleType = " + eventTuple.getTupleType() + "\t| Source : " +
                CloudSim.getEntityName(ev.getSource()) + "|Dest : " + CloudSim.getEntityName(ev.getDestination()));

		/*if(CloudSim.getEntityName(ev.getSource()).equals("drone_0")||CloudSim.getEntityName(ev.getDestination()).equals("drone_0"))
			System.out.println(CloudSim.clock()+" "+getName()+" Received tuple "+tuple.getCloudletId()+" with tupleType = "+tuple.getTupleType()+"\t| Source : "+
		CloudSim.getEntityName(ev.getSource())+"|Dest : "+CloudSim.getEntityName(ev.getDestination()));*/

        send(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ACK);

        if (FogUtils.appIdToGeoCoverageMap.containsKey(eventTuple.getAppId())) {
        }

        if (eventTuple.getDirection() == Tuple.ACTUATOR) {
            sendTupleToActuator(eventTuple);
            return;
        }

        if (getHost().getVmList().size() > 0) {
            final AppModule operator = (AppModule) getHost().getVmList().getFirst();
            if (CloudSim.clock() > 0) {
                getHost().getVmScheduler().deallocatePesForVm(operator);
                getHost().getVmScheduler().allocatePesForVm(operator, new ArrayList<Double>() {
                    protected static final long serialVersionUID = 1L;

                    {
                        add((double) getHost().getTotalMips());
                    }
                });
            }
        }


        if (getName().equals("cloud") && eventTuple.getDestModuleName() == null) {
            sendNow(getControllerId(), FogEvents.TUPLE_FINISHED, null);
        }

        if (appToModulesMap.containsKey(eventTuple.getAppId())) {
            if (appToModulesMap.get(eventTuple.getAppId()).contains(eventTuple.getDestModuleName())) {
                int vmId = -1;
                for (Vm vm : getHost().getVmList()) {
                    if (((AppModule) vm).getName().equals(eventTuple.getDestModuleName()))
                        vmId = vm.getId();
                }
                if (vmId < 0
                        || (eventTuple.getModuleCopyMap().containsKey(eventTuple.getDestModuleName()) &&
                        eventTuple.getModuleCopyMap().get(eventTuple.getDestModuleName()) != vmId)) {
                    return;
                }
                eventTuple.setVmId(vmId);
                //Logger.error(getName(), "Executing tuple for operator " + moduleName);

                updateTimingsOnReceipt(eventTuple);

                executeTuple(ev, eventTuple.getDestModuleName());
            } else if (eventTuple.getDestModuleName() != null) {
                if (eventTuple.getDirection() == Tuple.UP)
                    sendUp(eventTuple);
                else if (eventTuple.getDirection() == Tuple.DOWN) {
                    for (int childId : getChildrenIds())
                        sendDown(eventTuple, childId);
                }
            } else {
                sendUp(eventTuple);
            }
        } else {
            if (eventTuple.getDirection() == Tuple.UP)
                sendUp(eventTuple);
            else if (eventTuple.getDirection() == Tuple.DOWN) {
                for (int childId : getChildrenIds())
                    sendDown(eventTuple, childId);
            }
        }
    }

    @Override
    protected void sendDown(Tuple tuple, int childId) {
        super.sendDown(tuple, childId);
    }
}
