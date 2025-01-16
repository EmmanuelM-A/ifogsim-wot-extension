package com.extensions.vdcreation.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UriVariable extends BaseEntity {
    @JsonProperty("enum")
    private List<String> uriVariableEnum;

    public List<String> getUriVariableEnum() {
        return uriVariableEnum;
    }

    public void setUriVariableEnum(List<String> uriVariableEnum) {
        this.uriVariableEnum = uriVariableEnum;
    }
}
