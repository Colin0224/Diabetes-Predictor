package com.example.diabetes_predictor;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.util.List;


@Service
public class Practice {
    private static final String FILE_PATH = "Data/Practice.csv"; // Adjusted file path

    public boolean practiceOutput() {
        try (Reader fileReader = new InputStreamReader(new ClassPathResource(FILE_PATH).getInputStream());
             CSVReader csvReader = new CSVReader(fileReader)) {

            List<String[]> data = csvReader.readAll();

            // Process the data as needed
            for (String[] row : data) {
                System.out.println(String.join(", ", row)); // Prints each row
            }
            return true; // Indicate success
        } catch (IOException | CsvException e) { // Catch both IOException and CsvException here
            e.printStackTrace();
            return false; // Indicate failure
        }
    }
}
