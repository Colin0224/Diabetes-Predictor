package com.example.diabetes_predictor;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

class GenericRecordTest {

    @Test
    void testDefaultConstructor() {
        GenericRecord record = new GenericRecord();
        assertEquals(0.0, record.getLabel(), "Default label should be 0.0");
        assertNull(record.getFieldValue("anyFeature"), "Feature should be null by default");
    }

    @Test
    void testConvenienceConstructor() {
        Map<String, Double> features = new HashMap<>();
        features.put("age", 45.0);
        features.put("glucose", 120.0);
        GenericRecord record = new GenericRecord(1.0, features);

        assertEquals(1.0, record.getLabel(), "Label should be set to 1.0");
        assertEquals(45.0, record.getFieldValue("age"), "Feature 'age' should be 45.0");
        assertEquals(120.0, record.getFieldValue("glucose"), "Feature 'glucose' should be 120.0");
    }

    @Test
    void testSetAndGetLabel() {
        GenericRecord record = new GenericRecord();
        record.setLabel(0.0);
        assertEquals(0.0, record.getLabel(), "Label should be set to 0.0");

        record.setLabel(1.0);
        assertEquals(1.0, record.getLabel(), "Label should be updated to 1.0");
    }

    @Test
    void testSetAndGetFeatures() {
        GenericRecord record = new GenericRecord();
        record.setFeature("age", 50.0);
        record.setFeature("bloodPressure", 80.0);

        assertEquals(50.0, record.getFieldValue("age"), "Feature 'age' should be 50.0");
        assertEquals(80.0, record.getFieldValue("bloodPressure"), "Feature 'bloodPressure' should be 80.0");
        assertNull(record.getFieldValue("cholesterol"), "Feature 'cholesterol' should be null");
    }

    @Test
    void testOverwriteFeature() {
        GenericRecord record = new GenericRecord();
        record.setFeature("age", 30.0);
        assertEquals(30.0, record.getFieldValue("age"), "Feature 'age' should be 30.0");

        record.setFeature("age", 35.0);
        assertEquals(35.0, record.getFieldValue("age"), "Feature 'age' should be updated to 35.0");
    }
}
