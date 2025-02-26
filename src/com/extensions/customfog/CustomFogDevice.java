package com.extensions.customfog;

import com.extensions.sysconstructor.eventdriver.EventManager;
import com.extensions.sysconstructor.eventdriver.EventTuple;
import com.extensions.utils.FogEntityPrefixes;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Tuple;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.Logger;
import org.fog.utils.TimeKeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomFogDevice extends FogDevice {
    public CustomFogDevice(String name, FogDeviceCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval, double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval, uplinkBandwidth, downlinkBandwidth, uplinkLatency, ratePerMips);
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

    public void processEventTupleArrival(SimEvent ev) {
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

        System.out.println(getName() + " processing event tuple: " + eventTuple.getEventType());
    }

    @Override
    protected void sendDown(Tuple tuple, int childId) {
        super.sendDown(tuple, childId);
    }

    /*@Override
    protected void updateTimingsOnSending(Tuple resTuple) {
        String srcModule = resTuple.getSrcModuleName();
        String destModule = resTuple.getDestModuleName();
        for (AppLoop loop : getApplicationMap().get(resTuple.getAppId()).getLoops()) {
            if (loop.hasEdge(srcModule, destModule) && loop.isStartModule(srcModule)) {
                int tupleId = TimeKeeper.getInstance().getUniqueId();
                resTuple.setActualTupleId(tupleId);
                if (!TimeKeeper.getInstance().getLoopIdToTupleIds().containsKey(loop.getLoopId())) {
                    TimeKeeper.getInstance().getLoopIdToTupleIds().put(loop.getLoopId(), new ArrayList<Integer>());

                    System.out.println("Loop " + loop.getLoopId() + " added!");
                }
                TimeKeeper.getInstance().getLoopIdToTupleIds().get(loop.getLoopId()).add(tupleId);
                TimeKeeper.getInstance().getEmitTimes().put(tupleId, CloudSim.clock());

                //Logger.debug(getName(), "\tSENDING\t"+resTuple.getActualTupleId()+"\tSrc:"+srcModule+"\tDest:"+destModule);
            } else {
                //System.out.println(srcModule + " ---> " + destModule);
            }
        }
    }

    @Override
    protected void checkCloudletCompletion() {
        boolean cloudletCompleted = false;
        List<? extends Host> list = getVmAllocationPolicy().getHostList();
        for (int i = 0; i < list.size(); i++) {
            Host host = list.get(i);
            for (Vm vm : host.getVmList()) {
                while (vm.getCloudletScheduler().isFinishedCloudlets()) {
                    Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
                    if (cl != null) {

                        cloudletCompleted = true;
                        Tuple tuple = (Tuple) cl;
                        TimeKeeper.getInstance().tupleEndedExecution(tuple);
                        Application application = getApplicationMap().get(tuple.getAppId());
                        Logger.debug(getName(), "Completed execution of tuple " + tuple.getCloudletId() + "on " + tuple.getDestModuleName());
                        List<Tuple> resultantTuples = application.getResultantTuples(tuple.getDestModuleName(), tuple, getId(), vm.getId());
                        for (Tuple resTuple : resultantTuples) {
                            resTuple.setModuleCopyMap(new HashMap<String, Integer>(tuple.getModuleCopyMap()));
                            resTuple.getModuleCopyMap().put(((AppModule) vm).getName(), vm.getId());
                            updateTimingsOnSending(resTuple);
                            sendToSelf(resTuple);
                        }
                        sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
                    }
                }
            }
        }
        if (cloudletCompleted)
            updateAllocatedMips(null);
    }

    @Override
    protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
        double currentTime = CloudSim.clock();
        double minTime = Double.MAX_VALUE;
        double timeDiff = currentTime - getLastProcessTime();
        double timeFrameDatacenterEnergy = 0.0;

        for (PowerHost host : this.<PowerHost>getHostList()) {
            Log.printLine();

            double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
            if (time < minTime) {
                minTime = time;
            }

            Log.formatLine(
                    "%.2f: [Host #%d] utilization is %.2f%%",
                    currentTime,
                    host.getId(),
                    host.getUtilizationOfCpu() * 100);
        }

        if (timeDiff > 0) {
            Log.formatLine(
                    "\nEnergy consumption for the last time frame from %.2f to %.2f:",
                    getLastProcessTime(),
                    currentTime);

            for (PowerHost host : this.<PowerHost>getHostList()) {
                double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
                double utilizationOfCpu = host.getUtilizationOfCpu();
                double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
                        previousUtilizationOfCpu,
                        utilizationOfCpu,
                        timeDiff);
                timeFrameDatacenterEnergy += timeFrameHostEnergy;

                Log.printLine();
                Log.formatLine(
                        "%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
                        currentTime,
                        host.getId(),
                        getLastProcessTime(),
                        previousUtilizationOfCpu * 100,
                        utilizationOfCpu * 100);
                Log.formatLine(
                        "%.2f: [Host #%d] energy is %.2f W*sec",
                        currentTime,
                        host.getId(),
                        timeFrameHostEnergy);
            }

            Log.formatLine(
                    "\n%.2f: Data center's energy is %.2f W*sec\n",
                    currentTime,
                    timeFrameDatacenterEnergy);
        }

        setPower(getPower() + timeFrameDatacenterEnergy);

        checkCloudletCompletion();

        Log.printLine();

        setLastProcessTime(currentTime);
        return minTime;
    }*/
}
