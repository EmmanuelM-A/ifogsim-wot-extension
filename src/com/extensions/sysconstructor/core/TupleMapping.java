package com.extensions.sysconstructor.core;

import org.fog.application.selectivity.FractionalSelectivity;

import java.util.*;

public record TupleMapping(String module, String inputTupleType, String outputTupleType, FractionalSelectivity selectivity) {
    @Override
    public String toString() {
        return "{Module: " + module + " InputTupleType: " + inputTupleType + " OutputTupleType: " + outputTupleType + " Selectivity: " + selectivity.getSelectivity() + "}";
    }
}
