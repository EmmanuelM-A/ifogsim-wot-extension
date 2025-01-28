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
        EventTuple tuple = (EventTuple) ev.getData();

        // Check if the tuple is an event tuple
        if(tuple != null) {
            // Handle event tuple
            processEventTuple(tuple);
        } else {
            super.processTupleArrival(ev); // Regular tuple handling
        }

    }

    public void processEventTuple(EventTuple tuple) {
        System.out.println(getName() + " processing event tuple: " + tuple.getEventType());
        EventManager.getInstance().routeEvent(tuple);
    }

}
