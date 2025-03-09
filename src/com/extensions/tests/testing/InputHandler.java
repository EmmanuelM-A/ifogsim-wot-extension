package com.extensions.tests.testing;

public class InputHandler {
    public final String NODE_RED_APPLICATION_JSON;
    public final String THINGS_REPO;
    public final String VDS_CONFIG_FILE;
    public final String THINGS_QUANTITIES;
    public InputHandler(String applicationLocation, String base, boolean usingThingQuantities) {
        this.NODE_RED_APPLICATION_JSON = base + "/" + applicationLocation;
        this.THINGS_REPO = base + "/things";
        this.VDS_CONFIG_FILE = base + "/configs/vd-configs.json";
        this.THINGS_QUANTITIES = usingThingQuantities ? base + "/configs/thing-quantities.json" : "";
    }
}
