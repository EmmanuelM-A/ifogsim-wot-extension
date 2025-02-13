package com.extensions.customfog;

import com.extensions.sysconstructor.eventdriver.EventManager;
import com.extensions.sysconstructor.eventdriver.EventTuple;
import com.extensions.utils.FogEntityPrefixes;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;

import java.util.List;

public class CustomFogDevice extends FogDevice {
    public CustomFogDevice(String name, FogDeviceCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval, double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips);
    }

    @Override
    protected void processTupleArrival(SimEvent ev) {
        Object data = ev.getData();

        // Check if it's an instance of EventTuple
        if (data instanceof EventTuple eventTuple) {
            processEventTuple(eventTuple);
        } else if (data instanceof Tuple tuple) {
            // Default iFogSim Tuple Handling
            super.processTupleArrival(ev);
        } else {
            System.out.println("Warning: Received an unknown tuple type: " + data.getClass().getName());
        }
    }

    public void processEventTuple(EventTuple tuple) {
        System.out.println(getName() + " processing event tuple: " + tuple.getEventType());
        EventManager.getInstance().routeEvent(tuple);
    }

}
