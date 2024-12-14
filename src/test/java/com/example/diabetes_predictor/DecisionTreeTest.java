// src/test/java/com/example/diabetes_predictor/DecisionTreeTest.java
package com.example.diabetes_predictor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DecisionTreeTest {

	private DecisionTree decisionTree;
	private List<GenericRecord> sampleRecords;
	private String[] attributes;

	@BeforeEach
	void setUp() {
		decisionTree = new DecisionTree();
		attributes = new String[]{"age", "bmi", "glucose"};

		// Initialize sample records using the new constructor
		sampleRecords = Arrays.asList(
				new GenericRecord(1.0, createFields(25.0, 22.0, 85.0)),
				new GenericRecord(0.0, createFields(30.0, 28.0, 90.0)),
				new GenericRecord(1.0, createFields(35.0, 24.0, 95.0)),
				new GenericRecord(1.0, createFields(40.0, 30.0, 100.0)),
				new GenericRecord(0.0, createFields(45.0, 35.0, 105.0)),
				new GenericRecord(1.0, createFields(50.0, 40.0, 110.0))
		);
	}

	/**
	 * Helper method to create a fields map.
	 */
	private Map<String, Double> createFields(Double age, Double bmi, Double glucose) {
		Map<String, Double> fields = new HashMap<>();
		if (age != null) fields.put("age", age);
		if (bmi != null) fields.put("bmi", bmi);
		if (glucose != null) fields.put("glucose", glucose);
		return fields;
	}

	/**
	 * Test training with a standard dataset.
	 */
	@Test
	void testTrainWithValidData() {
		decisionTree.train(sampleRecords, attributes);
		// After training, predictions should be possible
		GenericRecord testRecord = new GenericRecord(0.0, createFields(28.0, 25.0, 88.0));
		double prediction = decisionTree.predict(testRecord);
		assertTrue(Double.isFinite(prediction), "Prediction should be a finite number.");
	}

	/**
	 * Test training with an empty dataset.
	 */
	@Test
	void testTrainWithEmptyRecords() {
		decisionTree.train(Collections.emptyList(), attributes);
		// Predicting after training with empty records should return 0.0 as per predict method
		GenericRecord testRecord = new GenericRecord(0.0, createFields(30.0, 25.0, 90.0));
		double prediction = decisionTree.predict(testRecord);
		assertEquals(0.0, prediction, 0.0001, "Prediction should be 0.0 when trained with empty records.");
	}

	/**
	 * Test training with all records having the same label (pure dataset).
	 */
	@Test
	void testTrainWithPureRecords() {
		List<GenericRecord> pureRecords = Arrays.asList(
				new GenericRecord(1.0, createFields(25.0, 22.0, 85.0)),
				new GenericRecord(1.0, createFields(30.0, 28.0, 90.0)),
				new GenericRecord(1.0, createFields(35.0, 24.0, 95.0))
		);

		decisionTree.train(pureRecords, attributes);
		GenericRecord testRecord = new GenericRecord(1.0, createFields(40.0, 30.0, 100.0));
		double prediction = decisionTree.predict(testRecord);
		assertEquals(1.0, prediction, 0.0001, "Prediction should be the average label of pure records.");
	}

	/**
	 * Test prediction when some attribute values are missing.
	 */
	@Test
	void testPredictWithMissingAttributes() {
		decisionTree.train(sampleRecords, attributes);
		// Create a record with missing 'bmi' attribute
		GenericRecord testRecord = new GenericRecord(0.0, createFields(28.0, null, 88.0));
		double prediction = decisionTree.predict(testRecord);
		assertTrue(Double.isFinite(prediction), "Prediction should be a finite number even with missing attributes.");
	}

	/**
	 * Test prediction on an untrained DecisionTree.
	 */
	@Test
	void testPredictWithoutTraining() {
		// Without training, the root is null, so predict should return 0.0
		GenericRecord testRecord = new GenericRecord(0.0, createFields(28.0, 25.0, 88.0));
		double prediction = decisionTree.predict(testRecord);
		assertEquals(0.0, prediction, 0.0001, "Prediction should be 0.0 when the tree is not trained.");
	}

	/**
	 * Test training with insufficient records to prevent splitting.
	 */
	@Test
	void testTrainWithInsufficientRecords() {
		List<GenericRecord> insufficientRecords = Arrays.asList(
				new GenericRecord(1.0, createFields(25.0, 22.0, 85.0)),
				new GenericRecord(0.0, createFields(30.0, 28.0, 90.0)),
				new GenericRecord(1.0, createFields(35.0, 24.0, 95.0)),
				new GenericRecord(1.0, createFields(40.0, 30.0, 100.0))
		); // Less than 5 records

		decisionTree.train(insufficientRecords, attributes);
		GenericRecord testRecord = new GenericRecord(0.0, createFields(28.0, 25.0, 88.0));
		double prediction = decisionTree.predict(testRecord);
		// The prediction should be the average label: (1 + 0 + 1 + 1)/4 = 0.75
		assertEquals(0.75, prediction, 0.0001, "Prediction should be the average label of insufficient records.");
	}

	/**
	 * Test that the tree handles records with null attribute values gracefully.
	 */
	@Test
	void testTrainWithNullAttributeValues() {
		List<GenericRecord> recordsWithNulls = Arrays.asList(
				new GenericRecord(1.0, createFields(null, 22.0, 85.0)),
				new GenericRecord(0.0, createFields(30.0, null, 90.0)),
				new GenericRecord(1.0, createFields(35.0, 24.0, null)),
				new GenericRecord(1.0, createFields(40.0, 30.0, 100.0)),
				new GenericRecord(0.0, createFields(45.0, 35.0, 105.0))
		);

		decisionTree.train(recordsWithNulls, attributes);
		GenericRecord testRecord = new GenericRecord(0.0, createFields(28.0, 25.0, 88.0));
		double prediction = decisionTree.predict(testRecord);
		assertTrue(Double.isFinite(prediction), "Prediction should be a finite number even with null attribute values.");
	}

	/**
	 * Test that multiple trainings reset the tree appropriately.
	 */
	@Test
	void testMultipleTrainings() {
		// First training
		decisionTree.train(sampleRecords, attributes);
		GenericRecord testRecord1 = new GenericRecord(0.0, createFields(28.0, 25.0, 88.0));
		double prediction1 = decisionTree.predict(testRecord1);
		assertTrue(Double.isFinite(prediction1), "First prediction should be a finite number.");

		// Second training with different data
		List<GenericRecord> newRecords = Arrays.asList(
				new GenericRecord(0.0, createFields(50.0, 40.0, 110.0)),
				new GenericRecord(0.0, createFields(55.0, 42.0, 115.0)),
				new GenericRecord(0.0, createFields(60.0, 45.0, 120.0)),
				new GenericRecord(0.0, createFields(65.0, 48.0, 125.0)),
				new GenericRecord(0.0, createFields(70.0, 50.0, 130.0))
		);
		decisionTree.train(newRecords, attributes);
		GenericRecord testRecord2 = new GenericRecord(0.0, createFields(58.0, 46.0, 118.0));
		double prediction2 = decisionTree.predict(testRecord2);
		assertTrue(Double.isFinite(prediction2), "Second prediction should be a finite number.");

		// The prediction should reflect the new training data
		// Average label is 0.0, so prediction should be 0.0
		assertEquals(0.0, prediction2, 0.0001, "Prediction should reflect the new training data.");
	}
}
