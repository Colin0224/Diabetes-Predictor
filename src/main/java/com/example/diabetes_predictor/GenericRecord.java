package com.example.diabetes_predictor;

import java.util.HashMap;
import java.util.Map;

public class GenericRecord {
    private double label;
    private Map<String, Double> features = new HashMap<>();

    // Default constructor
    public GenericRecord() {}

    // Convenience constructor
    public GenericRecord(double label, Map<String, Double> features) {
        this.label = label;
        if (features != null) {
            this.features.putAll(features);
        }
    }

    public double getLabel() {
        return label;
    }

    public void setLabel(double label) {
        this.label = label;
    }

    public void setFeature(String name, double value) {
        features.put(name, value);
    }

    public Double getFieldValue(String fieldName) {
        return features.get(fieldName);
    }
}
