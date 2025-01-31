package com.extensions.sysconstructor.core;

import org.fog.application.AppModule;

import java.util.ArrayList;
import java.util.List;

public class MultiOutputNodeModule extends NodeModule {
    private final List<String> additionalOutputTuples;

    public MultiOutputNodeModule(AppModule module) {
        super(module);
        this.additionalOutputTuples = new ArrayList<>();
    }

    public void addOutputTuple(String tuple) {
        this.additionalOutputTuples.add(tuple);
    }

    public List<String> getAdditionalOutputTuples() {
        return additionalOutputTuples;
    }
}

