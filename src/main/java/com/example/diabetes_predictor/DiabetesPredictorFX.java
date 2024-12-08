import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import com.example.diabetes_predictor.DiabetesRecord;
import com.example.diabetes_predictor.RandomForest;

public class DiabetesPredictorFX extends Application {

    private static final String[] INPUT_FEATURES = {
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

    private TextField[] inputFields;
    private Label resultLabel;
    private TextArea recommendationArea;
    private RandomForest randomForest;

    @Override
    public void start(Stage primaryStage) {
        randomForest = new RandomForest();

        // Main container
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Top: Title
        Label title = new Label("Diabetes Predictor");
        title.setFont(Font.font("SansSerif", 24));
        title.setTextFill(Color.web("#333333"));
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(10, 0, 20, 0));

        // Left: Input fields panel
        GridPane inputGrid = new GridPane();
        inputGrid.setVgap(10);
        inputGrid.setHgap(10);
        inputGrid.setPadding(new Insets(10));
        inputGrid.setStyle("-fx-background-color: #f9f9f9; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;");

        inputFields = new TextField[INPUT_FEATURES.length];

        for (int i = 0; i < INPUT_FEATURES.length; i++) {
            Label lbl = new Label(INPUT_FEATURES[i] + ":");
            lbl.setFont(Font.font("SansSerif", 14));
            TextField tf = new TextField();
            tf.setPromptText("Enter " + INPUT_FEATURES[i]);
            inputFields[i] = tf;

            inputGrid.add(lbl, 0, i);
            inputGrid.add(tf, 1, i);
        }

        // Center: Predict button
        Button predictButton = new Button("Predict & Get Plan");
        predictButton.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 5;");
        predictButton.setOnAction(e -> predictAndRecommend());

        VBox centerBox = new VBox(predictButton);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(20));

        // Bottom: Results
        VBox resultBox = new VBox(10);
        resultBox.setPadding(new Insets(10));
        resultBox.setStyle("-fx-background-color: #ffffff; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;");

        resultLabel = new Label("Prediction result will appear here...");
        resultLabel.setFont(Font.font("SansSerif", 16));
        resultLabel.setTextFill(Color.web("#333333"));

        recommendationArea = new TextArea();
        recommendationArea.setWrapText(true);
        recommendationArea.setPrefRowCount(8);
        recommendationArea.setFont(Font.font("SansSerif", 14));
        recommendationArea.setStyle("-fx-control-inner-background: #f5f5f5; -fx-background-radius: 8; -fx-border-radius: 8;");
        recommendationArea.setEditable(false);

        Label recLabel = new Label("Recommendations:");
        recLabel.setFont(Font.font("SansSerif", 16));
        recLabel.setTextFill(Color.web("#333333"));

        resultBox.getChildren().addAll(resultLabel, recLabel, recommendationArea);

        root.setTop(titleBox);
        root.setLeft(inputGrid);
        root.setCenter(centerBox);
        root.setBottom(resultBox);

        Scene scene = new Scene(root, 1000, 600, Color.WHITE);
        // Example custom CSS (optional): scene.getStylesheets().add("styles.css");
        primaryStage.setTitle("Diabetes Predictor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void predictAndRecommend() {
        DiabetesRecord record = new DiabetesRecord();
        try {
            record.setDiabetesBinary(0);
            record.setHighBP(Double.parseDouble(getInputValue("highBP")));
            record.setHighChol(Double.parseDouble(getInputValue("highChol")));
            record.setCholCheck(Double.parseDouble(getInputValue("cholCheck")));
            record.setBmi(Double.parseDouble(getInputValue("bmi")));
            record.setSmoker(Double.parseDouble(getInputValue("smoker")));
            record.setStroke(Double.parseDouble(getInputValue("stroke")));
            record.setHeartDiseaseOrAttack(Double.parseDouble(getInputValue("heartDiseaseOrAttack")));
            record.setPhysActivity(Double.parseDouble(getInputValue("physActivity")));
            record.setFruits(Double.parseDouble(getInputValue("fruits")));
            record.setVeggies(Double.parseDouble(getInputValue("veggies")));
            record.setHvyAlcoholConsump(Double.parseDouble(getInputValue("hvyAlcoholConsump")));
            record.setAnyHealthcare(Double.parseDouble(getInputValue("anyHealthcare")));
            record.setNoDocbcCost(Double.parseDouble(getInputValue("noDocbcCost")));
            record.setGenHlth(Double.parseDouble(getInputValue("genHlth")));
            record.setMentHlth(Double.parseDouble(getInputValue("mentHlth")));
            record.setPhysHlth(Double.parseDouble(getInputValue("physHlth")));
            record.setDiffWalk(Double.parseDouble(getInputValue("diffWalk")));
            record.setSex(Double.parseDouble(getInputValue("sex")));
            record.setAge(Double.parseDouble(getInputValue("age")));
            record.setEducation(Double.parseDouble(getInputValue("education")));
            record.setIncome(Double.parseDouble(getInputValue("income")));
        } catch (NumberFormatException ex) {
            showAlert("Input Error", "Please ensure all fields are entered as numbers.");
            return;
        }

        double prediction = randomForest.predict(record);
        double predClass = (prediction >= 0.5) ? 1.0 : 0.0;

        String predMessage = (predClass == 1.0) ?
                "You are likely diabetic (according to the model)." :
                "You are likely not diabetic (according to the model).";
        resultLabel.setText(predMessage);

        String userDataSummary = buildUserDataSummary(record);

        // In a real application, you would call the OpenAI API here.
        // For this example, let's just put a placeholder recommendation.
        String openAIResponse = "Based on your metrics, consider a balanced diet rich in fiber and lean proteins, engage in moderate physical activity at least 3-5 times a week, and consult with a healthcare provider for personalized advice.";

        recommendationArea.setText(openAIResponse);
    }

    private String getInputValue(String featureName) {
        for (int i = 0; i < INPUT_FEATURES.length; i++) {
            if (INPUT_FEATURES[i].equals(featureName)) {
                return inputFields[i].getText().trim();
            }
        }
        return "";
    }

    private String buildUserDataSummary(DiabetesRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append("User Data:\n");
        sb.append("HighBP: ").append(r.getHighBP()).append("\n");
        sb.append("HighChol: ").append(r.getHighChol()).append("\n");
        sb.append("CholCheck: ").append(r.getCholCheck()).append("\n");
        sb.append("BMI: ").append(r.getBmi()).append("\n");
        sb.append("Smoker: ").append(r.getSmoker()).append("\n");
        sb.append("Stroke: ").append(r.getStroke()).append("\n");
        sb.append("HeartDiseaseOrAttack: ").append(r.getHeartDiseaseOrAttack()).append("\n");
        sb.append("PhysActivity: ").append(r.getPhysActivity()).append("\n");
        sb.append("Fruits: ").append(r.getFruits()).append("\n");
        sb.append("Veggies: ").append(r.getVeggies()).append("\n");
        sb.append("HvyAlcoholConsump: ").append(r.getHvyAlcoholConsump()).append("\n");
        sb.append("AnyHealthcare: ").append(r.getAnyHealthcare()).append("\n");
        sb.append("NoDocbcCost: ").append(r.getNoDocbcCost()).append("\n");
        sb.append("GenHlth: ").append(r.getGenHlth()).append("\n");
        sb.append("MentHlth: ").append(r.getMentHlth()).append("\n");
        sb.append("PhysHlth: ").append(r.getPhysHlth()).append("\n");
        sb.append("DiffWalk: ").append(r.getDiffWalk()).append("\n");
        sb.append("Sex: ").append(r.getSex()).append("\n");
        sb.append("Age: ").append(r.getAge()).append("\n");
        sb.append("Education: ").append(r.getEducation()).append("\n");
        sb.append("Income: ").append(r.getIncome()).append("\n");
        return sb.toString();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
