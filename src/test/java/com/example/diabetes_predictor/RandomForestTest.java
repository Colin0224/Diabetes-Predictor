package com.example.diabetes_predictor;

import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RandomForestTest {

    @InjectMocks
    private RandomForest randomForest;

    @Mock
    private DecisionTree decisionTreeMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testTrainFromCSVStringInvalidCSV_NoTarget() throws IOException, CsvException {
        String csvData = "age,glucose\n" +
                "45,120\n" +
                "50,80";

        boolean result = randomForest.trainFromCSVString(csvData, "label");
        assertFalse(result, "Training should fail when target column is missing");
    }


    @Test
    void testPredict() {
        // Create a sample GenericRecord
        GenericRecord record = new GenericRecord();
        record.setLabel(1.0);
        record.setFeature("age", 40.0);
        record.setFeature("glucose", 130.0);

        // Mock DecisionTree's predict method to return 1.0 for all trees
        when(decisionTreeMock.predict(any(GenericRecord.class))).thenReturn(1.0);

        // Inject the mock DecisionTree into the forest
        ReflectionTestUtils.setField(randomForest, "numTrees", 3);
        List<DecisionTree> mockedForest = Arrays.asList(decisionTreeMock, decisionTreeMock, decisionTreeMock);
        ReflectionTestUtils.setField(randomForest, "forest", mockedForest);

        double prediction = randomForest.predict(record);
        assertEquals(1.0, prediction, "Prediction should be the average of tree predictions");
    }

    @Test
    void testPredictWithMixedTreePredictions() {
        // Create a sample GenericRecord
        GenericRecord record = new GenericRecord();
        record.setLabel(1.0);
        record.setFeature("age", 40.0);
        record.setFeature("glucose", 130.0);

        // Mock DecisionTree's predict method to return mixed predictions
        DecisionTree tree1 = mock(DecisionTree.class);
        DecisionTree tree2 = mock(DecisionTree.class);
        DecisionTree tree3 = mock(DecisionTree.class);

        when(tree1.predict(any(GenericRecord.class))).thenReturn(1.0);
        when(tree2.predict(any(GenericRecord.class))).thenReturn(0.0);
        when(tree3.predict(any(GenericRecord.class))).thenReturn(1.0);

        // Inject the mocked trees into the forest
        List<DecisionTree> mockedForest = Arrays.asList(tree1, tree2, tree3);
        ReflectionTestUtils.setField(randomForest, "forest", mockedForest);

        double prediction = randomForest.predict(record);
        assertEquals((1.0 + 0.0 + 1.0) / 3, prediction, 1e-6, "Prediction should be the average of tree predictions");
    }

    @Test
    void testTrainFromCSVStringEmptyCSV() throws IOException, CsvException {
        String csvData = "";

        boolean result = randomForest.trainFromCSVString(csvData, "label");
        assertFalse(result, "Training should fail with empty CSV data");
    }

    @Test
    void testTrainFromCSVStringInsufficientColumns() throws IOException, CsvException {
        String csvData = "label\n" + // Only label column
                "1\n" +
                "0";

        boolean result = randomForest.trainFromCSVString(csvData, "label");
        assertFalse(result, "Training should fail when there are not enough columns");
    }

    @Test
    void testTestForestEmptyTestSet() {
        // Since testForest is private, we can't call it directly
        // Instead, we simulate training and check if accuracy is 0 when test set is empty

        // Mock the DecisionTree's predict method
        when(decisionTreeMock.predict(any(GenericRecord.class))).thenReturn(1.0);

        // Inject the mock DecisionTree into the forest
        ReflectionTestUtils.setField(randomForest, "numTrees", 1);
        List<DecisionTree> mockedForest = Arrays.asList(decisionTreeMock);
        ReflectionTestUtils.setField(randomForest, "forest", mockedForest);

        // Train with no data (empty CSV)
        String csvData = "label,age,glucose\n"; // Only header
        boolean result = randomForest.trainFromCSVString(csvData, "label");
        assertFalse(result, "Training should fail with only header and no data");

        // Since training failed, accuracy should remain 0.0
        double accuracy = randomForest.getLastAccuracy();
        assertEquals(0.0, accuracy, "Accuracy should be 0.0 when training fails or test set is empty");
    }

    // Additional helper methods can be added as needed
}
