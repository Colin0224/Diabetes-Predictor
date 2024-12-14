package com.example.diabetes_predictor;

import java.util.List;
import java.util.Random;

public class DecisionTree {
    private Node root;
    private Random rand = new Random();

    public void train(List<GenericRecord> records, String[] attributes) {
        if (records.isEmpty()) {
            return;
        }
        root = buildTree(records, attributes);
    }

    private Node buildTree(List<GenericRecord> records, String[] attributes) {
        // Stopping condition: if too small or pure, return a leaf
        if (records.size() < 5 || isPure(records)) {
            Node leaf = new Node();
            leaf.prediction = averageLabel(records);
            return leaf;
        }

        // Otherwise, pick a random attribute and threshold (placeholder logic)
        String attribute = attributes[rand.nextInt(attributes.length)];
        double threshold = findThreshold(records, attribute);

        List<GenericRecord> leftSet = records.stream()
                .filter(r -> {
                    Double val = r.getFieldValue(attribute);
                    return val != null && val <= threshold;
                }).toList();

        List<GenericRecord> rightSet = records.stream()
                .filter(r -> {
                    Double val = r.getFieldValue(attribute);
                    return val != null && val > threshold;
                }).toList();

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

    private boolean isPure(List<GenericRecord> records) {
        double first = records.get(0).getLabel();
        for (GenericRecord r : records) {
            if (r.getLabel() != first) return false;
        }
        return true;
    }

    private double averageLabel(List<GenericRecord> records) {
        return records.stream().mapToDouble(GenericRecord::getLabel).average().orElse(0.0);
    }

    private double findThreshold(List<GenericRecord> records, String attribute) {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (GenericRecord r : records) {
            Double val = r.getFieldValue(attribute);
            if (val != null) {
                if (val < min) min = val;
                if (val > max) max = val;
            }
        }
        if (min == Double.MAX_VALUE || max == -Double.MAX_VALUE) {
            // If we cannot find a valid min/max, default to 0.5
            return 0.5;
        }
        return min + (max - min) * rand.nextDouble();
    }

    public double predict(GenericRecord record) {
        Node current = root;
        while (current != null && !current.isLeaf()) {
            Double val = record.getFieldValue(current.attribute);
            if (val == null) {
                current = current.left;
            } else {
                current = (val <= current.threshold) ? current.left : current.right;
            }
        }
        return (current != null) ? current.prediction : 0.0;
    }

    private static class Node {
        String attribute;
        double threshold;
        Node left;
        Node right;
        double prediction = Double.NaN;

        boolean isLeaf() {
            return Double.isFinite(prediction);
        }
    }
}
