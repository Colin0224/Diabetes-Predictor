package com.example.diabetes_predictor;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@Service
public class RandomForest {
    private static final String DEFAULT_FILE_PATH = "Data/Diabetes_Dataset.csv";
    private List<GenericRecord> dataset = new ArrayList<>();
    private List<DecisionTree> forest = new ArrayList<>();
    private int numTrees = 10; // number of trees in the forest
    private String labelName;
    private String[] featureNames;
    private double lastAccuracy = 0.0;

    /**
     * Original practice method. Not strictly needed if you rely on trainFromCSVString.
     * It trains using a default dataset from resources.
     */
    public boolean practiceOutput() {
        return loadAndTrain(DEFAULT_FILE_PATH);
    }

    /**
     * Loads and trains from a CSV file on the classpath. Assumes first column is label.
     * This is the original logic. You can keep or remove it if not needed.
     */
    public boolean loadAndTrain(String filePath) {
        try (Reader fileReader = new InputStreamReader(new ClassPathResource(filePath).getInputStream());
             CSVReader csvReader = new CSVReader(fileReader)) {

            List<String[]> data = csvReader.readAll();
            if (data.isEmpty()) {
                System.err.println("CSV file is empty.");
                return false;
            }

            String[] header = data.get(0);
            if (header.length < 2) {
                System.err.println("Not enough columns in the dataset. Need at least one label and one feature.");
                return false;
            }

            // By default, the first column is label
            labelName = header[0];
            featureNames = Arrays.copyOfRange(header, 1, header.length);

            dataset.clear();
            parseDataRows(data, header, labelName);

            if (dataset.isEmpty()) {
                System.err.println("No valid rows found in the dataset.");
                return false;
            }

            // Shuffle and split data
            Collections.shuffle(dataset, new Random(42));
            int trainSize = (int) (dataset.size() * 0.8);
            List<GenericRecord> trainSet = dataset.subList(0, trainSize);
            List<GenericRecord> testSet = dataset.subList(trainSize, dataset.size());

            buildForest(trainSet);
            double accuracy = testForest(testSet);
            System.out.println("Random Forest Accuracy: " + accuracy);

            return true;

        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return false;
        }
    }


    public double getLastAccuracy() {
        return lastAccuracy;
    }


    /**
     * Train the RandomForest from a given CSV string and target variable name.
     * The CSV should have a header row. The targetName must be one of the columns.
     * All other columns are treated as features.
     *
     * @param csvData    The entire CSV as a String.
     * @param targetName The name of the column to treat as the target.
     * @return true if training succeeded, false otherwise.
     */
    public boolean trainFromCSVString(String csvData, String targetName) {
        try (CSVReader csvReader = new CSVReader(new StringReader(csvData))) {
            List<String[]> data = csvReader.readAll();
            if (data.isEmpty()) {
                System.err.println("CSV data is empty.");
                return false;
            }

            String[] header = data.get(0);
            List<String> headerList = new ArrayList<>();
            for (String h : header) {
                headerList.add(h.trim());
            }

            if (!headerList.contains(targetName)) {
                System.err.println("Target variable not found in header.");
                return false;
            }

            // Identify feature columns (all except targetName)
            List<String> featureCols = new ArrayList<>();
            for (String h : headerList) {
                if (!h.equals(targetName)) {
                    featureCols.add(h);
                }
            }

            if (featureCols.isEmpty()) {
                System.err.println("No features found.");
                return false;
            }

            this.featureNames = featureCols.toArray(new String[0]);
            this.labelName = targetName;

            dataset.clear();
            parseDataRows(data, header, targetName);

            if (dataset.isEmpty()) {
                System.err.println("No valid rows found in the dataset.");
                return false;
            }

            Collections.shuffle(dataset, new Random(42));
            int trainSize = (int) (dataset.size() * 0.8);
            List<GenericRecord> trainSet = dataset.subList(0, trainSize);
            List<GenericRecord> testSet = dataset.subList(trainSize, dataset.size());

            buildForest(trainSet);
            double accuracy = testForest(testSet);
            this.lastAccuracy = accuracy;
            System.out.println("Random Forest Accuracy: " + accuracy);

            return true;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Parse data rows given a header and targetName.
     */
    private void parseDataRows(List<String[]> data, String[] header, String targetName) {
        // data[0] is header, start from data[1]
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length != header.length) {
                // Skip rows with incorrect length
                continue;
            }

            Double labelVal = null;
            Map<String, Double> featureMap = new HashMap<>();

            boolean validRow = true;
            for (int j = 0; j < header.length; j++) {
                String colName = header[j];
                String valStr = row[j].trim();
                double val;
                try {
                    val = Double.parseDouble(valStr);
                } catch (NumberFormatException e) {
                    // Non-numeric, skip this row entirely
                    validRow = false;
                    break;
                }

                if (colName.equals(targetName)) {
                    labelVal = val;
                } else {
                    featureMap.put(colName, val);
                }
            }

            if (validRow && labelVal != null) {
                GenericRecord record = new GenericRecord();
                record.setLabel(labelVal);
                for (Map.Entry<String, Double> entry : featureMap.entrySet()) {
                    record.setFeature(entry.getKey(), entry.getValue());
                }
                dataset.add(record);
            }
        }
    }

    public String[] getFeatureNames() {
        return featureNames;
    }

    private void buildForest(List<GenericRecord> trainSet) {
        forest.clear();
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
            // Assuming binary classification: round prediction
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
