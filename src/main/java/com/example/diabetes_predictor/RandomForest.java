package com.example.diabetes_predictor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;

@Service
public class RandomForest {
    private static final String FILE_PATH = "Data/Diabetes_Dataset.csv";
    private ArrayList<DiabetesRecord> dataset = new ArrayList<>();
    public static final String[] FIELD_NAMES = {
            "diabetesBinary",
            "highBP",
            "highChol",
            "cholCheck",
            "bmi",
            "smoker",
            "stroke",
            "heartDiseaseOrAttack",
            "physActivity",
            "fruits",
            "veggies",
            "hvyAlcoholConsump",
            "anyHealthcare",
            "noDocbcCost",
            "genHlth",
            "mentHlth",
            "physHlth",
            "diffWalk",
            "sex",
            "age",
            "education",
            "income"
    };

    private List<DecisionTree> forest = new ArrayList<>();
    private int numTrees = 10; // number of trees in the forest

    public boolean practiceOutput() {
        try (Reader fileReader = new InputStreamReader(new ClassPathResource(FILE_PATH).getInputStream());
             CSVReader csvReader = new CSVReader(fileReader)) {

            List<String[]> data = csvReader.readAll();
            // data.remove(0); // if there's a header
            for (String[] row : data) {
                if (row.length != 22) {
                    continue;
                }
                DiabetesRecord record = new DiabetesRecord();
                try {
                    record.setDiabetesBinary(Double.parseDouble(row[0]));
                    record.setHighBP(Double.parseDouble(row[1]));
                    record.setHighChol(Double.parseDouble(row[2]));
                    record.setCholCheck(Double.parseDouble(row[3]));
                    record.setBmi(Double.parseDouble(row[4]));
                    record.setSmoker(Double.parseDouble(row[5]));
                    record.setStroke(Double.parseDouble(row[6]));
                    record.setHeartDiseaseOrAttack(Double.parseDouble(row[7]));
                    record.setPhysActivity(Double.parseDouble(row[8]));
                    record.setFruits(Double.parseDouble(row[9]));
                    record.setVeggies(Double.parseDouble(row[10]));
                    record.setHvyAlcoholConsump(Double.parseDouble(row[11]));
                    record.setAnyHealthcare(Double.parseDouble(row[12]));
                    record.setNoDocbcCost(Double.parseDouble(row[13]));
                    record.setGenHlth(Double.parseDouble(row[14]));
                    record.setMentHlth(Double.parseDouble(row[15]));
                    record.setPhysHlth(Double.parseDouble(row[16]));
                    record.setDiffWalk(Double.parseDouble(row[17]));
                    record.setSex(Double.parseDouble(row[18]));
                    record.setAge(Double.parseDouble(row[19]));
                    record.setEducation(Double.parseDouble(row[20]));
                    record.setIncome(Double.parseDouble(row[21]));
                } catch (NumberFormatException e) {
                    // skip bad row
                    continue;
                }
                dataset.add(record);
            }

            // Now we have a dataset, let's split into training and testing
            Collections.shuffle(dataset, new Random(42));
            int trainSize = (int)(dataset.size() * 0.8);
            List<DiabetesRecord> trainSet = dataset.subList(0, trainSize);
            List<DiabetesRecord> testSet = dataset.subList(trainSize, dataset.size());

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

    private void buildForest(List<DiabetesRecord> trainSet) {
        // For simplicity, use all features except the target "diabetesBinary"
        String[] features = new String[FIELD_NAMES.length - 1];
        System.arraycopy(FIELD_NAMES, 1, features, 0, FIELD_NAMES.length - 1);

        for (int i = 0; i < numTrees; i++) {
            // Bootstrap sample for each tree (in a real implementation)
            List<DiabetesRecord> bootstrapSample = bootstrapSample(trainSet);
            DecisionTree tree = new DecisionTree();
            tree.train(bootstrapSample, features);
            forest.add(tree);
        }
    }

    private List<DiabetesRecord> bootstrapSample(List<DiabetesRecord> original) {
        // Simple bootstrap: same size, random sampling with replacement
        List<DiabetesRecord> sample = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < original.size(); i++) {
            sample.add(original.get(rand.nextInt(original.size())));
        }
        return sample;
    }

    private double testForest(List<DiabetesRecord> testSet) {
        int correct = 0;
        for (DiabetesRecord r : testSet) {
            double prediction = predict(r);
            double actual = r.getDiabetesBinary();
            // Rounding prediction for a binary outcome (0 or 1)
            double predClass = (prediction >= 0.5) ? 1.0 : 0.0;
            if (predClass == actual) {
                correct++;
            }
        }
        return (double) correct / testSet.size();
    }

    public double predict(DiabetesRecord record) {
        double sum = 0.0;
        for (DecisionTree tree : forest) {
            sum += tree.predict(record);
        }
        double avg = sum / forest.size();
        return avg;
    }
}
