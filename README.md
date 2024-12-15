# Diabetes Predictor

## Project Overview
The **Diabetes Predictor** project aims to predict the likelihood of diabetes in individuals using a Random Forest algorithm. The application takes user-provided health data as input, processes it through a trained machine learning model, and outputs a risk assessment. This project is built using JavaFX for the graphical user interface (GUI) and requires Java 23 and JavaFX SDK 23.0.1.

## Download Jar File: https://drive.google.com/file/d/1HhbbieXcNxUj5FJO4xwYx9uyw04fqEZF/view?usp=sharing

---

## Prerequisites
To run the application, ensure you have the following:

1. **Java Development Kit (JDK)**:
    - Download from: [OpenJDK 23](https://jdk.java.net/23/)
    - Install and note the path to the `java.exe` file (usually located in the `bin` directory of the JDK installation folder).

2. **JavaFX SDK**:
    - Download from: [JavaFX SDK 23](https://jdk.java.net/javafx23/)
    - Extract the SDK and note the path to the `lib` folder.

3. **Diabetes Predictor JAR File**:
    - The executable JAR file is available in the `D:\diabetes predictor\diabetes-predictor-0.0.1-SNAPSHOT.jar` folder or can be downloaded from this repository.

---

## Running the Application
Follow these steps to run the application on your machine:

### Windows Instructions
1. Open a command prompt.
2. Navigate to the directory containing the `diabetes-predictor-0.0.1-SNAPSHOT.jar` file:
   ```
   cd D:\diabetes predictor
   ```
3. Run the following command (replace paths with the appropriate locations on your machine):
   ```
   "path\to\jdk-23.0.1\bin\java" --module-path "path\to\javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.fxml -jar diabetes-predictor-0.0.1-SNAPSHOT.jar
   ```

   Example:
   ```
   "D:\jdk-23.0.1\bin\java" --module-path "D:\javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.fxml -jar "D:\diabetes predictor\diabetes-predictor-0.0.1-SNAPSHOT.jar"
   ```

### Mac/Linux Instructions
Replace the paths with appropriate ones for your operating system. Use forward slashes (`/`) instead of backslashes (`\`).

---

## Features
- **User-Friendly Interface**: Built using JavaFX, the application is interactive and easy to use.
- **Health Data Input**: Users can enter data such as BMI, age, and other health-related metrics.
- **Machine Learning Model**: Predicts diabetes risk using a trained Random Forest algorithm.
- **Output Analysis**: Provides a clear risk score with recommendations for further action.

---

## Troubleshooting
### Common Issues
1. **Module Not Found Error**:
    - Ensure the `--module-path` is correctly pointing to the `lib` folder in your JavaFX SDK installation.

2. **JAR File Not Found**:
    - Double-check the path to the `diabetes-predictor-0.0.1-SNAPSHOT.jar` file and ensure it is specified correctly.

3. **Driver or Compatibility Errors**:
    - Ensure you are running the correct versions of Java (23.0.1) and JavaFX SDK (23.0.1).
    - Update your system environment variables to include the Java executable if necessary.

### Logs and Debugging
Run the command with the `--verbose` flag to view detailed logs for debugging:
```sh
"path\to\java.exe" --module-path "path\to\javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.fxml -jar diabetes-predictor-0.0.1-SNAPSHOT.jar --verbose
```

---

## Repository
The full source code, JAR file, and additional documentation can be found on GitHub:
[Diabetes Predictor GitHub Repository](https://github.com/Colin0224/Diabetes-Predictor)

---

## Contributions
Contributions are welcome! Feel free to open issues or submit pull requests on the GitHub repository.

---

## Contact
For questions or support, contact Colin at [your-email@example.com].

