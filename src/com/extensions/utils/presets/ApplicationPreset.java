package com.extensions.utils.presets;

public enum ApplicationPreset {
    DEFAULT(
           10,
           0,
           0,
           10000,
           100,
           100,
           10,
           6
    );

    public final int APP_MODULE_RAM;
    public final int APP_MODULE_MIPS;
    public final int APP_MODULE_SIZE;
    public final int APP_EDGE_TUPLE_CPU_LENGTH;
    public final int APP_EDGE_TUPLE_NW_LENGTH;
    public final int UPLINK_LATENCY_EDGE_TO_CLOUD;
    public final int UPLINK_LATENCY_VD_TO_EDGE;
    public final int MAX_VDS_FOR_ONE_EDE_NODE;

    ApplicationPreset(
            int appModuleRam,
            int appModuleMips,
            int appModuleSize,
            int appEdgeTupleCpuLength,
            int appEdgeTupleNwLength,
            int upLinkEdgeToCloud,
            int uplinkVdToEdge,
            int maxNoVDsForOneEdgeNode
    ) {
        this.APP_MODULE_RAM = appModuleRam;
        this.APP_MODULE_MIPS = appModuleMips;
        this.APP_MODULE_SIZE = appModuleSize;
        this.APP_EDGE_TUPLE_CPU_LENGTH = appEdgeTupleCpuLength;
        this.APP_EDGE_TUPLE_NW_LENGTH = appEdgeTupleNwLength;
        this.UPLINK_LATENCY_EDGE_TO_CLOUD = upLinkEdgeToCloud;
        this.UPLINK_LATENCY_VD_TO_EDGE = uplinkVdToEdge;
        this.MAX_VDS_FOR_ONE_EDE_NODE = maxNoVDsForOneEdgeNode;
    }
}
