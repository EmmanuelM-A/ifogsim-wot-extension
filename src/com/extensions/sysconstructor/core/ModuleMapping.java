package com.extensions.sysconstructor.core;

public class ModuleMapping {
    private String inputTupleType;

    private String outputTupleType;

    public ModuleMapping() {
        this.inputTupleType = null;
        this.outputTupleType = null;
    }

    public String getInputTupleType() {
        return inputTupleType;
    }

    public void setInputTupleType(String inputTupleType) {
        this.inputTupleType = inputTupleType;
    }

    public String getOutputTupleType() {
        return outputTupleType;
    }

    public void setOutputTupleType(String outputTupleType) {
        this.outputTupleType = outputTupleType;
    }
}
