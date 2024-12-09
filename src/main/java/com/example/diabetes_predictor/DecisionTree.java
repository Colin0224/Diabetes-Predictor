package com.example.diabetes_predictor;

import java.util.List;
import java.util.Random;

public class DecisionTree {
    private Node root;
    private Random rand = new Random();

    // This is a highly simplified decision tree constructor
    public void train(List<DiabetesRecord> records, String[] attributes) {
        // For simplicity, we just pick a random attribute and a random threshold
        // In a real tree, you'd find the attribute/threshold that yields the best split
        if (records.isEmpty()) {
            return;
        }
        root = buildTree(records, attributes);
    }

    private Node buildTree(List<DiabetesRecord> records, String[] attributes) {
        // Stopping condition: if too small or pure, return a leaf
        if (records.size() < 5 || isPure(records)) {
            Node leaf = new Node();
            leaf.prediction = averageLabel(records);
            return leaf;
        }

        // Otherwise, pick a random attribute and threshold (placeholder logic)
        String attribute = attributes[rand.nextInt(attributes.length)];
        double threshold = findThreshold(records, attribute);

        // Split records
        List<DiabetesRecord> leftSet = records.stream().filter(r -> (double) r.getFieldValue(attribute) <= threshold).toList();
        List<DiabetesRecord> rightSet = records.stream().filter(r -> (double) r.getFieldValue(attribute) > threshold).toList();

        // If we end up with empty splits, just return a leaf
        if (leftSet.isEmpty() || rightSet.isEmpty()) {
            Node leaf = new Node();
            leaf.prediction = averageLabel(records);
            return leaf;
        }

        Node node = new Node();
        node.attribute = attribute;
        node.threshold = threshold;
        node.left = buildTree(leftSet, attributes);
        node.right = buildTree(rightSet, attributes);

        return node;
    }

    private boolean isPure(List<DiabetesRecord> records) {
        double first = records.get(0).getDiabetesBinary();
        for (DiabetesRecord r : records) {
            if (r.getDiabetesBinary() != first) return false;
        }
        return true;
    }

    private double averageLabel(List<DiabetesRecord> records) {
        return records.stream().mapToDouble(DiabetesRecord::getDiabetesBinary).average().orElse(0.0);
    }

    private double findThreshold(List<DiabetesRecord> records, String attribute) {
        // Random threshold based on min-max of this attribute
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (DiabetesRecord r : records) {
            double val = (double) r.getFieldValue(attribute);
            if (val < min) min = val;
            if (val > max) max = val;
        }
        return min + (max - min) * rand.nextDouble();
    }

    public double predict(DiabetesRecord record) {
        Node current = root;
        while (!current.isLeaf()) {
            double val = (double) record.getFieldValue(current.attribute);
            current = (val <= current.threshold) ? current.left : current.right;
        }
        return current.prediction;
    }
}
