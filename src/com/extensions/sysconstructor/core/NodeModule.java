package com.extensions.sysconstructor.core;

import org.fog.application.AppModule;

public class NodeModule {
    private final AppModule module;

    private NodeModule nextModule;

    private String inputTupleType;

    private String outputTupleType;

    public NodeModule(AppModule module) {
        this.module = module;
        this.nextModule = null;
        this.inputTupleType = null;
        this.outputTupleType = null;
    }

    public AppModule getModule() {
        return module;
    }

    public NodeModule getNextModule() {
        return nextModule;
    }

    public void setNextModule(NodeModule nextModule) {
        this.nextModule = nextModule;
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
