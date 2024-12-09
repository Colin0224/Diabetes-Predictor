package com.example.diabetes_predictor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DiabetesPredictorApplication {
	@Autowired
	private RandomForest practice;

	public static void main(String[] args) {
		SpringApplication.run(DiabetesPredictorApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner() {
		return args -> {
			System.out.println("Reading CSV file and building Random Forest:");
			boolean success = practice.practiceOutput();
			if (!success) {
				System.out.println("Failed to read CSV file.");
			}
		};
	}
}
