package com.example.diabetes_predictor;

public class Node {
    String attribute;
    double threshold;
    Node left;
    Node right;
    Double prediction; // if leaf node, store prediction

    public boolean isLeaf() {
        return (left == null && right == null);
    }
}
