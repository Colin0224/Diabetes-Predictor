package com.example.diabetes_predictor;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@Service
public class RandomForest {
    private static final String DEFAULT_FILE_PATH = "Data/s.csv";
    private List<GenericRecord> dataset = new ArrayList<>();
    private List<DecisionTree> forest = new ArrayList<>();
    private int numTrees = 10; // number of trees in the forest
    private String labelName; // Not strictly used here, but we store for clarity
    private String[] featureNames; // Dynamic features from the CSV

    /**
     * Trains the model using the default dataset.
     * For general-purpose use, call loadAndTrain with your own file path.
     */
    public boolean practiceOutput() {
        return loadAndTrain(DEFAULT_FILE_PATH);
    }

    /**
     * Load a dataset from a given CSV file path, train a RandomForest model,
     * and evaluate it. This method is general purpose.
     * It expects:
     *   - The first row is a header row.
     *   - The first column in the header is the label column name.
     *   - The remaining columns are features.
     * Data that can't be parsed to doubles is skipped.
     */
    public boolean loadAndTrain(String filePath) {
        try (Reader fileReader = new InputStreamReader(new ClassPathResource(filePath).getInputStream());
             CSVReader csvReader = new CSVReader(fileReader)) {

            List<String[]> data = csvReader.readAll();
            if (data.isEmpty()) {
                System.err.println("CSV file is empty.");
                return false;
            }

            // The first row is the header
            String[] header = data.get(0);
            if (header.length < 2) {
                System.err.println("Not enough columns in the dataset. Need at least one label and one feature.");
                return false;
            }

            labelName = header[0];
            featureNames = Arrays.copyOfRange(header, 1, header.length);

            dataset.clear();
            // Parse each subsequent row
            for (int i = 1; i < data.size(); i++) {
                String[] row = data.get(i);
                if (row.length != header.length) {
                    // Skip rows with incorrect length
                    continue;
                }

                // First column: label
                double label;
                try {
                    label = Double.parseDouble(row[0]);
                } catch (NumberFormatException e) {
                    // Skip if label not numeric
                    continue;
                }

                // The rest are features
                GenericRecord record = new GenericRecord();
                record.setLabel(label);

                boolean validRow = true;
                for (int j = 1; j < row.length; j++) {
                    double val;
                    try {
                        val = Double.parseDouble(row[j]);
                    } catch (NumberFormatException ex) {
                        // Non-numeric feature: skip this row
                        validRow = false;
                        break;
                    }
                    record.setFeature(header[j], val);
                }

                if (validRow) {
                    dataset.add(record);
                }
            }

            if (dataset.isEmpty()) {
                System.err.println("No valid rows found in the dataset.");
                return false;
            }

            // Shuffle and split data
            Collections.shuffle(dataset, new Random(42));
            int trainSize = (int) (dataset.size() * 0.8);
            List<GenericRecord> trainSet = dataset.subList(0, trainSize);
            List<GenericRecord> testSet = dataset.subList(trainSize, dataset.size());

            // Build the random forest
            buildForest(trainSet);

            // Test the forest
            double accuracy = testForest(testSet);
            System.out.println("Random Forest Accuracy: " + accuracy);

            return true;

        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return false;
        }
    }
    public String[] getFeatureNames() {
        return featureNames;
    }
    private void buildForest(List<GenericRecord> trainSet) {
        // In a general scenario, we already have featureNames from the header.
        // We'll use them directly.
        for (int i = 0; i < numTrees; i++) {
            List<GenericRecord> bootstrapSample = bootstrapSample(trainSet);
            DecisionTree tree = new DecisionTree();
            tree.train(bootstrapSample, featureNames);
            forest.add(tree);
        }
    }

    private List<GenericRecord> bootstrapSample(List<GenericRecord> original) {
        List<GenericRecord> sample = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < original.size(); i++) {
            sample.add(original.get(rand.nextInt(original.size())));
        }
        return sample;
    }

    private double testForest(List<GenericRecord> testSet) {
        int correct = 0;
        for (GenericRecord r : testSet) {
            double prediction = predict(r);
            double actual = r.getLabel();
            // Round prediction for a binary outcome (0 or 1)
            double predClass = (prediction >= 0.5) ? 1.0 : 0.0;
            if (predClass == actual) {
                correct++;
            }
        }
        return testSet.isEmpty() ? 0.0 : (double) correct / testSet.size();
    }

    public double predict(GenericRecord record) {
        double sum = 0.0;
        for (DecisionTree tree : forest) {
            sum += tree.predict(record);
        }
        return sum / forest.size();
    }
}
