package com.extensions.vdcreation.validation;

import com.extensions.vdcreation.core.VirtualDevice;

import java.util.List;
import java.util.Map;

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

    /**
     * The id of virtual device which this report is addressed to.
     */
    private String id;

    /**
     * Stores which validators the virtual device passed and which ones it failed.
     */
    private Map<Validator, Boolean> validationResults;

    /**
     * Represents the number validators the virtual device passed.
     */
    private boolean successCount;

    public ValidationReport(List<Validator> validators) {

    }
}
