// src/test/java/com/example/diabetes_predictor/DiabetesPredictorFXLogicTest.java
package com.example.diabetes_predictor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class DiabetesPredictorFXLogicTest {

    private DiabetesPredictorFX predictor;
    private RandomForest mockForest;
    @BeforeEach
    void setUp() {
        predictor = new DiabetesPredictorFX();
        mockForest = Mockito.mock(RandomForest.class);
        predictor.setRandomForest(mockForest);

        when(mockForest.getFeatureNames()).thenReturn(new String[]{"feat1", "feat2", "feat3"});
    }

    @Test
    void testBuildGenericRecordFromAnswers() {
        List<String> answers = Arrays.asList("Yes", "No");
        // Now directly call buildGenericRecordFromAnswers without reflection
        GenericRecord record = predictor.buildGenericRecordFromAnswers(answers);
        assertEquals(1.0, record.getFieldValue("feat1"));
        assertEquals(0.0, record.getFieldValue("feat2"));
        assertEquals(0.5, record.getFieldValue("feat3"));
    }


    @Test
    void testGaussian() {
        double val = callGaussian(0.5, 0.5, 0.15);
        // Gaussian( mean=0.5, x=0.5) should be at max density
        assertTrue(val > 2.0, "Peak of Gaussian with small std dev should be high");
    }


    @Test
    void testBuildGenericRecordFromValues() {
        List<Double> values = Arrays.asList(0.2, 0.8);
        GenericRecord record = callBuildGenericRecordFromValues(values);

        assertEquals(0.2, record.getFieldValue("feat1"));
        assertEquals(0.8, record.getFieldValue("feat2"));
        assertEquals(0.5, record.getFieldValue("feat3"), "Missing value defaults to 0.5");
    }


    // Helper methods to invoke private logic of DiabetesPredictorFX
    // If the tested methods are private, consider refactoring DiabetesPredictorFX to make them package-private
    // or protected so the test can access them, or use reflection.

    private double callGetValueFromChoice(String choice) {
        return invokePrivateDoubleMethod("getValueFromChoice", new Class[]{String.class}, new Object[]{choice});
    }

    private double callGaussian(double x, double m, double sd) {
        return invokePrivateDoubleMethod("gaussian", new Class[]{double.class, double.class, double.class}, new Object[]{x, m, sd});
    }

    private GenericRecord callBuildGenericRecordFromAnswers(List<String> answers) {
        return invokePrivateRecordMethod("buildGenericRecordFromAnswers", new Class[]{List.class}, new Object[]{answers});
    }

    private GenericRecord callBuildGenericRecordFromValues(List<Double> values) {
        return invokePrivateRecordMethod("buildGenericRecordFromValues", new Class[]{List.class}, new Object[]{values});
    }

    private String callGenerateOpenAIPrompt(List<String> userAnswers, double prediction) {
        return invokePrivateStringMethod("generateOpenAIPrompt", new Class[]{List.class, double.class}, new Object[]{userAnswers, prediction});
    }

    // Reflection utilities:
    private double invokePrivateDoubleMethod(String methodName, Class<?>[] paramTypes, Object[] params) {
        try {
            var method = DiabetesPredictorFX.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return (double) method.invoke(predictor, params);
        } catch (Exception e) {
            fail("Failed to invoke " + methodName + ": " + e.getMessage());
            return Double.NaN;
        }
    }

    private GenericRecord invokePrivateRecordMethod(String methodName, Class<?>[] paramTypes, Object[] params) {
        try {
            var method = DiabetesPredictorFX.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return (GenericRecord) method.invoke(predictor, params);
        } catch (Exception e) {
            fail("Failed to invoke " + methodName + ": " + e.getMessage());
            return null;
        }
    }

    private String invokePrivateStringMethod(String methodName, Class<?>[] paramTypes, Object[] params) {
        try {
            var method = DiabetesPredictorFX.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return (String) method.invoke(predictor, params);
        } catch (Exception e) {
            fail("Failed to invoke " + methodName + ": " + e.getMessage());
            return null;
        }
    }
}
