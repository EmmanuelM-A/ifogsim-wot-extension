package com.extensions.utils.presets;

public enum ApplicationPreset {
    DEFAULT(
           10,
           1000,
           1000,
           1000,
           500,
           17,
           10,
           6
    );

    /**
     * The RAM allocation for application modules in MB.
     */
    public final int APP_MODULE_RAM;

    /**
     * The processing power for application modules in MIPS.
     */
    public final int APP_MODULE_MIPS;

    /**
     * The storage allocation for application modules in MB.
     */
    public final int APP_MODULE_SIZE;

    /**
     * The CPU processing time for tuples in CPU instructions.
     */
    public final int APP_EDGE_TUPLE_CPU_LENGTH;

    /**
     * The network transmission size for tuples in bits.
     */
    public final int APP_EDGE_TUPLE_NW_LENGTH;

    /**
     * The uplink latency from edge nodes to the cloud in milliseconds.
     */
    public final int UPLINK_LATENCY_EDGE_TO_CLOUD;

    /**
     * The uplink latency from virtual devices to edge nodes in milliseconds.
     */
    public final int UPLINK_LATENCY_VD_TO_EDGE;

    /**
     * The maximum number of virtual devices allowed on a single edge node.
     */
    public final int MAX_VDS_FOR_ONE_EDE_NODE;

    /**
     * Constructs an ApplicationPreset with the specified parameters.
     *
     * @param appModuleRam            The RAM allocation for application modules.
     * @param appModuleMips           The processing power for application modules.
     * @param appModuleSize           The storage allocation for application modules.
     * @param appEdgeTupleCpuLength   The CPU processing time for tuples.
     * @param appEdgeTupleNwLength    The network transmission size for tuples.
     * @param upLinkEdgeToCloud       The uplink latency from edge nodes to the cloud.
     * @param uplinkVdToEdge          The uplink latency from virtual devices to edge nodes.
     * @param maxNoVDsForOneEdgeNode The maximum number of virtual devices allowed on a single edge node.
     */
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
