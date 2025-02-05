package com.extensions.customfog;

import com.extensions.custommetrics.CustomMetricManager;
import com.extensions.simulation.SimulationResults;
import com.extensions.sysconstructor.eventdriver.EventDrivenApplication;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.placement.Controller;
import org.fog.placement.ModulePlacement;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;

import java.util.List;
import java.util.Map;

public class CustomController extends Controller {
    private final CustomMetricManager customMetricManager;
    private final SimulationResults simulationResults;
    private final List<FogDevice> fogDevices;
    public CustomController(String name, List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators) {
        super(name, fogDevices, sensors, actuators);

        this.customMetricManager = new CustomMetricManager();
        this.simulationResults = new SimulationResults(fogDevices);
        this.fogDevices = fogDevices;
    }

    @Override
    public void processEvent(SimEvent ev) {
        if (ev.getTag() == FogEvents.STOP_SIMULATION) {
            CloudSim.stopSimulation();
            simulationResults.printResults();
            System.exit(0);
        }
    }

    /* @Override
    public void processEvent(SimEvent ev) {
        switch(ev.getTag()){
            case FogEvents.APP_SUBMIT:
                processAppSubmit(ev);
                break;
            case FogEvents.TUPLE_FINISHED:
                processTupleFinished(ev);
                break;
            case FogEvents.CONTROLLER_RESOURCE_MANAGE:
                manageResources();
                break;
            case FogEvents.STOP_SIMULATION:
                CloudSim.stopSimulation();
                simulationResults.printResults();
                System.exit(0);
                break;

        }
    }

    private void processAppSubmit(SimEvent ev){
        Application app = (Application) ev.getData();
        processAppSubmit(app);
    }

    private void processAppSubmit(Application application){
        System.out.println(CloudSim.clock()+" Submitted application "+ application.getAppId());
        FogUtils.appIdToGeoCoverageMap.put(application.getAppId(), application.getGeoCoverage());
        getApplications().put(application.getAppId(), application);

        ModulePlacement modulePlacement = getAppModulePlacementPolicy().get(application.getAppId());
        for(FogDevice fogDevice : fogDevices){
            sendNow(fogDevice.getId(), FogEvents.ACTIVE_APP_UPDATE, application);
        }

        Map<Integer, List<AppModule>> deviceToModuleMap = modulePlacement.getDeviceToModuleMap();
        for(Integer deviceId : deviceToModuleMap.keySet()){
            for(AppModule module : deviceToModuleMap.get(deviceId)){
                sendNow(deviceId, FogEvents.APP_SUBMIT, application);
                sendNow(deviceId, FogEvents.LAUNCH_MODULE, module);
            }
        }
    }*/

    private void processTupleFinished(SimEvent ev) {
    }

    public CustomMetricManager getCustomMetricManager() {
        return customMetricManager;
    }

    public SimulationResults getSimulationResults() {
        return simulationResults;
    }
}
