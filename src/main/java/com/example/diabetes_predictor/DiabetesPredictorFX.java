package com.example.diabetes_predictor;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;


// Import statements for OpenAI client
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;

public class DiabetesPredictorFX extends Application {

    private static final String[] QUESTIONS = {
            "Do you have High Blood Pressure?",
            "Do you have High Cholesterol?",
            "Have you had a Cholesterol Check recently?",
            "Is your BMI high?",
            "Do you smoke?",
            "Have you ever had a stroke?",
            "Have you had heart disease or a heart attack?",
            "Do you engage in regular physical activity?",
            "Do you eat fruits regularly?",
            "Do you eat vegetables regularly?",
            "Do you have heavy alcohol consumption?",
            "Do you have any healthcare coverage?",
            "Have you avoided a doctor due to cost?",
            "How is your general health? (Yes=Good, No=Bad, Don't Know=Average)",
            "Is your mental health good?",
            "Is your physical health good?",
            "Do you have difficulty walking?",
            "Are you male? (Yes=Male, No=Female, Don't Know=Prefer not to say)",
            "Are you older? (Yes=Older, No=Younger, Don't Know=Average)",
            "Do you have higher education?",
            "Do you have a higher income?"
    };

    private int currentQuestionIndex = 0;
    private List<String> answers = new ArrayList<>();

    private Label questionLabel;
    private ToggleGroup answerGroup;
    private RadioButton yesButton;
    private RadioButton noButton;
    private RadioButton dontKnowButton;
    private Button nextButton;
    private ProgressBar questionProgress;
    private BorderPane root;
    private VBox centerBox;

    private VBox resultBox;
    private Label resultLabel;
    private TextArea recommendationArea;

    private RandomForest randomForest;

    @Override
    public void start(Stage primaryStage) {
        // Instantiate and train the random forest model before showing UI
        randomForest = new RandomForest();
        boolean trained = randomForest.practiceOutput();
        if (!trained) {
            System.err.println("Warning: Random Forest training failed. Predictions may be invalid.");
        }

        root = new BorderPane();
        root.setPadding(new Insets(20));

        // Title
        Label title = new Label("Diabetes Predictor");
        title.setStyle("-fx-font-size: 32px; -fx-text-fill: #333333; -fx-font-weight: bold;");

        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(10, 0, 20, 0));
        root.setTop(titleBox);

        // Progress Bar
        questionProgress = new ProgressBar(0);
        questionProgress.setPrefWidth(300);
        HBox progressBox = new HBox(questionProgress);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(10,0,10,0));

        // Question area
        questionLabel = new Label();
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #444444;");

        answerGroup = new ToggleGroup();
        yesButton = new RadioButton("Yes");
        noButton = new RadioButton("No");
        dontKnowButton = new RadioButton("I Don't Know");

        yesButton.setToggleGroup(answerGroup);
        noButton.setToggleGroup(answerGroup);
        dontKnowButton.setToggleGroup(answerGroup);

        HBox choices = new HBox(20, yesButton, noButton, dontKnowButton);
        choices.setAlignment(Pos.CENTER);

        nextButton = new Button("Next");
        nextButton.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5;");
        nextButton.setOnAction(e -> handleNext());

        VBox questionContainer = new VBox(15, questionLabel, choices, progressBox, nextButton);
        questionContainer.setAlignment(Pos.CENTER);
        questionContainer.setPadding(new Insets(20));
        questionContainer.setStyle("-fx-background-color: #f3f3f3; -fx-background-radius: 10;");

        centerBox = new VBox(questionContainer);
        centerBox.setAlignment(Pos.CENTER);
        root.setCenter(centerBox);

        // Result box
        resultBox = new VBox(10);
        resultBox.setPadding(new Insets(20));
        resultBox.setAlignment(Pos.CENTER);

        resultLabel = new Label();
        resultLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        recommendationArea = new TextArea();
        recommendationArea.setWrapText(true);
        recommendationArea.setEditable(false);
        recommendationArea.setPrefSize(400, 200);
        recommendationArea.setStyle("-fx-control-inner-background: #f5f5f5; -fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 14px;");

        resultBox.getChildren().addAll(resultLabel, recommendationArea);

        Scene scene = new Scene(root, 800, 500);

        primaryStage.setTitle("Diabetes Predictor");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Show the first question after training is done
        showQuestion(0);
    }

    private void showQuestion(int index) {
        if (index < QUESTIONS.length) {
            questionLabel.setText(QUESTIONS[index]);
            answerGroup.selectToggle(null);
            double progress = (double)index / QUESTIONS.length;
            questionProgress.setProgress(progress);
            nextButton.setText((index == QUESTIONS.length - 1) ? "Finish" : "Next");
        }
    }

    private void handleNext() {
        RadioButton selected = (RadioButton) answerGroup.getSelectedToggle();
        if (selected == null) {
            showAlert("No Selection", "Please select an answer before proceeding.");
            return;
        }

        answers.add(selected.getText());
        currentQuestionIndex++;

        if (currentQuestionIndex < QUESTIONS.length) {
            showQuestion(currentQuestionIndex);
        } else {
            // All answers collected
            nextButton.setDisable(true);
            showLoadingAndPredict();
        }
    }

    private void showLoadingAndPredict() {
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(100, 100);

        Label loadingLabel = new Label("Analyzing your responses...");
        loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555555;");

        VBox loadingBox = new VBox(loadingIndicator, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setSpacing(20);

        centerBox.getChildren().clear();
        centerBox.getChildren().add(loadingBox);

        Task<Void> predictionTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Simulate computation time (e.g., calling predict on model)
                Thread.sleep(2000);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                displayResults();
            }
        };

        new Thread(predictionTask).start();
    }

    private void displayResults() {
        DiabetesRecord record = buildRecordFromAnswers(answers);

        double prediction = randomForest.predict(record);
        double predClass = (prediction >= 0.5) ? 1.0 : 0.0;

        String predMessage = (predClass == 1.0)
                ? "According to the model, you are likely diabetic."
                : "According to the model, you are likely not diabetic.";

        resultLabel.setText(predMessage);

        // Call OpenAI API for a more robust response
        String response = generateAResponseFromOpenAI(answers, predClass);
        recommendationArea.setText(response);

        centerBox.getChildren().clear();
        centerBox.getChildren().add(resultBox);
    }

    private DiabetesRecord buildRecordFromAnswers(List<String> answers) {
        double[] vals = new double[QUESTIONS.length];
        for (int i = 0; i < answers.size(); i++) {
            vals[i] = getValueFromChoice(answers.get(i));
        }

        DiabetesRecord r = new DiabetesRecord();
        r.setDiabetesBinary(0);
        r.setHighBP(vals[0]);
        r.setHighChol(vals[1]);
        r.setCholCheck(vals[2]);
        r.setBmi(vals[3]);
        r.setSmoker(vals[4]);
        r.setStroke(vals[5]);
        r.setHeartDiseaseOrAttack(vals[6]);
        r.setPhysActivity(vals[7]);
        r.setFruits(vals[8]);
        r.setVeggies(vals[9]);
        r.setHvyAlcoholConsump(vals[10]);
        r.setAnyHealthcare(vals[11]);
        r.setNoDocbcCost(vals[12]);
        r.setGenHlth(vals[13]);
        r.setMentHlth(vals[14]);
        r.setPhysHlth(vals[15]);
        r.setDiffWalk(vals[16]);
        r.setSex(vals[17]);
        r.setAge(vals[18]);
        r.setEducation(vals[19]);
        r.setIncome(vals[20]);

        return r;
    }

    private double getValueFromChoice(String choice) {
        switch (choice) {
            case "Yes": return 1.0;
            case "No": return 0.0;
            case "I Don't Know": return 0.5;
        }
        return 0.5;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    /**
     * Generates a prompt using user answers and the prediction result, then calls OpenAI API.
     */
    private String generateAResponseFromOpenAI(List<String> userAnswers, double predClass) {
        StringBuilder prompt = new StringBuilder();

        if (predClass == 1.0) {
            prompt.append("The user’s inputs suggest they may be at risk for diabetes. ")
                    .append("They answered a series of yes/no/I don't know questions related to health, lifestyle, and demographics.\n");
        } else {
            prompt.append("The user’s inputs suggest they may not be at high risk for diabetes. ")
                    .append("They answered a series of yes/no/I don't know questions related to health, lifestyle, and demographics.\n");
        }

        prompt.append("User Answers:\n");
        for (int i = 0; i < QUESTIONS.length; i++) {
            String ans = (i < userAnswers.size()) ? userAnswers.get(i) : "No Response";
            prompt.append((i + 1)).append(". ").append(QUESTIONS[i]).append(" Answer: ").append(ans).append("\n");
        }

        prompt.append("\nPlease provide a detailed, empathetic health and lifestyle plan considering their inputs. ")
                .append("Include advice on diet, exercise, and follow-up with healthcare professionals. ")
                .append("If 'I Don't Know' was answered, encourage seeking professional guidance.");

        return callOpenAIAPI(prompt.toString());
    }

    /**
     * Calls the OpenAI API with the given prompt and returns the completion text.
     */private String callOpenAIAPI(String prompt) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) {
            return "OPENAI_API_KEY not set. Please set the environment variable.";
        }

        OpenAiService service = new OpenAiService(apiKey);

        // Build the chat messages. Typically you might have a system message setting the role or instructions.
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful medical assistant."));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o") // Chat-based model
                .messages(messages)
                .maxTokens(500)
                .temperature(0.7)
                .build();

        try {
            var result = service.createChatCompletion(request).getChoices();
            if (!result.isEmpty()) {
                // Extract the response from the first choice
                return result.get(0).getMessage().getContent().trim();
            } else {
                return "No response from OpenAI.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling OpenAI API: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
