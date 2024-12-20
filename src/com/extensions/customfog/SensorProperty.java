package com.extensions.customfog;

import com.extensions.utils.presets.SensorPreset;
import com.extensions.vdcreation.models.Property;
import org.fog.entities.Sensor;

public class SensorProperty extends Sensor {
    private Property property;
    private SensorPreset preset;
    private String units;
    private double minValue;
    private double maxValue;
    private double samplingRate;

    public SensorProperty(String name, int userId, String appId, Property property, SensorPreset preset) {
        super(name, property.getType(), userId, appId, preset.DISTRIBUTION);
        this.property = property;
        this.preset = preset;
    }
}
