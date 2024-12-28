package com.extensions.vdcreation.validation;

import com.extensions.vdcreation.core.VirtualDevice;

import java.util.Map;

public class Report {
    /**
     * The id of virtual device which this report is addressed to.
     */
    private String id;

    /**
     * Stores which validators the virtual device passed and which ones it failed.
     */
    private Map<Validator, Boolean> validationResults;

    /**
     * Represents the number validators the virtual device passed against the total number of validators
     */
    private boolean result;

    //public
}

/*
 * VD Validation Report:
 * Map<Validator, Boolean> {
 *      CorrectMapping - Success,
 *      Functional - Success,
 *      Data - Fail,
 *      ... - ...,
 * }
 * sucessRate: 5 / No. validators
 *
 */
