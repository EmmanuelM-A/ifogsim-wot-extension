package com.extensions.vdcreation.validation;

import com.extensions.vdcreation.core.VirtualDevice;

import java.util.List;

/**
 * Contains a report of all VD validators
 * - Original List of VDs
 * - List of VDs with VD that did not pass removed
 * - List of VDs that did not pass and the areas they did not pass
 * - Percentage of VDs that passed validation (define a threshold of acceptance)
 * - Log issues and successes
 * - Calculate acceptance
 */
public class ValidationReport {
    private List<VirtualDevice> originalVirtualDevices;
    private List<VirtualDevice> passedVirtualDevices;
    private List<Report> virtualDeviceReports;
    private double successRate;
    private double acceptance;

    public ValidationReport(List<VirtualDevice> originalVirtualDevices, List<VirtualDevice> passedVirtualDevices, List<Report> virtualDeviceReports) {

    }
}
