package com.example.diabetes_predictor;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

public class DiabetesPredictorFX extends Application {

    private List<String> QUESTIONS = new ArrayList<>();
    private List<String> QUESTION_IMAGES = new ArrayList<>();
    private final String PLACEHOLDER_IMAGE_PATH = "/images/placeholder.png";

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
    private StackPane centerPane;
    private VBox resultBox;
    private Label resultLabel;
    private TextArea recommendationArea;
    private RandomForest randomForest;
    private boolean isCustomDataset = false;
    private ImageView questionImageView;

    // For custom dataset form
    private List<TextField> customTextFields = new ArrayList<>();
    private String[] featureNames;

    // A top-level reference to our stage
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Instantiate and train the random forest model before showing UI
        randomForest = new RandomForest();
        boolean trained = randomForest.practiceOutput();
        if (!trained) {
            System.err.println("Warning: Random Forest training failed. Predictions may be invalid.");
        }

        // Initial scene with two buttons
        BorderPane initialPane = new BorderPane();
        initialPane.setPadding(new Insets(20));

        Label initialTitle = new Label("Welcome to the Diabetes Predictor");
        initialTitle.setStyle("-fx-font-size: 24px; -fx-text-fill: #333333; -fx-font-weight: bold;");
        initialPane.setTop(new HBox(initialTitle));
        ((HBox) initialPane.getTop()).setAlignment(Pos.CENTER);
        ((HBox) initialPane.getTop()).setPadding(new Insets(10, 0, 20, 0));

        Button defaultButton = new Button("Use Default Questions");
        defaultButton.setOnAction(e -> {
            loadDefaultDataset();
            isCustomDataset = false;
            buildQuestionnaireScene();
        });

        Button loadCsvButton = new Button("Load Custom Questions CSV");
        loadCsvButton.setOnAction(e -> {
            loadCSVFromFileChooser();
        });

        HBox buttonBox = new HBox(20, defaultButton, loadCsvButton);
        buttonBox.setAlignment(Pos.CENTER);
        initialPane.setCenter(buttonBox);

        Scene initialScene = new Scene(initialPane, 600, 400);
        primaryStage.setTitle("Diabetes Predictor");
        primaryStage.setScene(initialScene);
        primaryStage.show();
    }

    /**
     * Show FileChooser to load custom CSV.
     */
    private void loadCSVFromFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File for Questions");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            boolean loaded = loadCustomQuestionsFromCSV(selectedFile);
            if (loaded) {
                isCustomDataset = true;
                buildFormScene(); // Build the form scene for custom dataset
            } else {
                showAlert("Error", "Failed to load CSV. Please ensure it is in the correct format.");
            }
        }
    }

    /**
     * For a custom dataset, use the CSV headers as questions.
     */
    private boolean loadCustomQuestionsFromCSV(File csvFile) {
        QUESTIONS.clear();
        QUESTION_IMAGES.clear();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                return false; // no header
            }

            String[] headers = headerLine.split(",");
            for (String h : headers) {
                String col = h.trim();
                if (!col.isEmpty()) {
                    QUESTIONS.add("Please provide your " + col + ":");
                    QUESTION_IMAGES.add(null);
                }
            }
            return !QUESTIONS.isEmpty();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load default set of questions and images.
     */
    private void loadDefaultDataset() {
        String[] defaultQuestions = {
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

        String[] defaultImages = {
                "/images/question1.png",
                "/images/question2.png",
                "/images/question3.png",
                "/images/question4.png",
                "/images/question5.png",
                "/images/question6.png",
                "/images/question7.png",
                "/images/question8.png",
                "/images/question9.png",
                "/images/question10.png",
                "/images/question11.png",
                "/images/question12.png",
                "/images/question13.png",
                "/images/question14.png",
                "/images/question15.png",
                "/images/question16.png",
                "/images/question17.png",
                "/images/question18.png",
                "/images/question19.png",
                "/images/question20.png",
                "/images/question21.png"
        };

        QUESTIONS.clear();
        QUESTION_IMAGES.clear();
        for (String q : defaultQuestions) {
            QUESTIONS.add(q);
        }
        for (String img : defaultImages) {
            QUESTION_IMAGES.add(img);
        }
    }

    /**
     * Build the questionnaire scene for the default dataset.
     */
    private void buildQuestionnaireScene() {
        root = new BorderPane();
        root.setPadding(new Insets(20));

        // Title Label
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

        // Question label
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
        nextButton.setOnAction(e -> handleNextQuestion());

        questionImageView = new ImageView();
        questionImageView.setPreserveRatio(true);
        questionImageView.setFitWidth(200);
        questionImageView.setSmooth(true);
        questionImageView.setCache(true);

        VBox questionContainer = new VBox(15, questionLabel, choices, progressBox, nextButton);
        questionContainer.setAlignment(Pos.CENTER);
        questionContainer.setPadding(new Insets(20));
        questionContainer.setStyle("-fx-background-color: #f3f3f3; -fx-background-radius: 10;");

        HBox questionLayout = new HBox(20, questionImageView, questionContainer);
        questionLayout.setAlignment(Pos.CENTER);
        questionLayout.setPadding(new Insets(20));

        centerPane = new StackPane(questionLayout);
        centerPane.setAlignment(Pos.CENTER);
        root.setCenter(centerPane);

        // Result box
        buildResultBox();

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        currentQuestionIndex = 0;
        answers.clear();
        showQuestion(0, true);
    }

    /**
     * Build the form scene for the custom dataset.
     * Here we show all questions at once as text fields.
     */
    private void buildFormScene() {
        root = new BorderPane();
        root.setPadding(new Insets(20));

        Label title = new Label("Diabetes Predictor - Custom Dataset");
        title.setStyle("-fx-font-size: 32px; -fx-text-fill: #333333; -fx-font-weight: bold;");
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(10, 0, 20, 0));
        root.setTop(titleBox);

        // We'll show a placeholder image on the side and the form on the other side
        questionImageView = new ImageView();
        questionImageView.setPreserveRatio(true);
        questionImageView.setFitWidth(200);
        questionImageView.setSmooth(true);
        questionImageView.setCache(true);
        // Load placeholder image
        InputStream placeholderStream = getClass().getResourceAsStream(PLACEHOLDER_IMAGE_PATH);
        if (placeholderStream != null) {
            questionImageView.setImage(new Image(placeholderStream));
        }

        // Create a form with text fields for each question
        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(20));
        customTextFields.clear();

        // We'll also determine featureNames from this scenario
        // For simplicity, assume featureNames are the same as the QUESTIONS but stripped of prefix text.
        // If randomForest.getFeatureNames() is what we need, let's just call that:
        featureNames = randomForest.getFeatureNames();

        for (int i = 0; i < QUESTIONS.size(); i++) {
            // QUESTION format: "Please provide your <col>:"
            // We'll just show the question label and a text field.
            HBox row = new HBox(10);
            Label qLabel = new Label((i + 1) + ") " + QUESTIONS.get(i));
            TextField field = new TextField();
            customTextFields.add(field);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(qLabel, field);
            form.getChildren().add(row);
        }

        Button submitButton = new Button("Submit");
        submitButton.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        submitButton.setOnAction(e -> handleFormSubmit());
        form.getChildren().add(submitButton);

        HBox mainContent = new HBox(20, questionImageView, form);
        mainContent.setAlignment(Pos.CENTER);
        mainContent.setPadding(new Insets(20));

        centerPane = new StackPane(mainContent);
        centerPane.setAlignment(Pos.CENTER);
        root.setCenter(centerPane);

        buildResultBox();

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void buildResultBox() {
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

        resultBox.getChildren().clear();
        resultBox.getChildren().addAll(resultLabel, recommendationArea);
    }

    private void showQuestion(int index, boolean fadeIn) {
        if (index < QUESTIONS.size()) {
            questionLabel.setText(QUESTIONS.get(index));
            answerGroup.selectToggle(null);
            double progress = (double) index / QUESTIONS.size();
            questionProgress.setProgress(progress);
            nextButton.setText((index == QUESTIONS.size() - 1) ? "Finish" : "Next");

            // Load image for default dataset
            Image img = null;
            if (!isCustomDataset && index < QUESTION_IMAGES.size()) {
                String imagePath = QUESTION_IMAGES.get(index);
                if (imagePath != null && !imagePath.isEmpty()) {
                    InputStream imageStream = getClass().getResourceAsStream(imagePath);
                    if (imageStream == null) {
                        System.err.println("Image not found: " + imagePath + ", using placeholder");
                        InputStream placeholderStream = getClass().getResourceAsStream(PLACEHOLDER_IMAGE_PATH);
                        if (placeholderStream != null) {
                            img = new Image(placeholderStream);
                        }
                    } else {
                        img = new Image(imageStream);
                    }
                } else {
                    // If no image for this question, use placeholder
                    InputStream placeholderStream = getClass().getResourceAsStream(PLACEHOLDER_IMAGE_PATH);
                    if (placeholderStream != null) {
                        img = new Image(placeholderStream);
                    }
                }
            }
            questionImageView.setImage(img);

            if (fadeIn) {
                applyFadeTransition(centerPane, 0, 1, 500).play();
            }
        }
    }

    private void handleNextQuestion() {
        RadioButton selected = (RadioButton) answerGroup.getSelectedToggle();
        if (selected == null) {
            showAlert("No Selection", "Please select an answer before proceeding.");
            return;
        }
        answers.add(selected.getText());
        if (currentQuestionIndex < QUESTIONS.size() - 1) {
            FadeTransition ft = applyFadeTransition(centerPane, 1, 0, 500);
            ft.setOnFinished(event -> {
                currentQuestionIndex++;
                showQuestion(currentQuestionIndex, true);
            });
            ft.play();
        } else {
            // All answers collected
            nextButton.setDisable(true);
            showLoadingAndPredictForQuestionnaire();
        }
    }

    private void showLoadingAndPredictForQuestionnaire() {
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(100, 100);
        Label loadingLabel = new Label("Analyzing your responses...");
        loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555555;");
        VBox loadingBox = new VBox(loadingIndicator, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setSpacing(20);
        centerPane.getChildren().clear();
        centerPane.getChildren().add(loadingBox);
        Task<Void> predictionTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Simulate computation time
                Thread.sleep(2000);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                displayResultsForQuestionnaire();
            }
        };
        new Thread(predictionTask).start();
    }

    private void displayResultsForQuestionnaire() {
        GenericRecord record = buildGenericRecordFromAnswers(answers);
        double prediction = randomForest.predict(record);
        double predClass = (prediction >= 0.5) ? 1.0 : 0.0;
        String predMessage = (predClass == 1.0)
                ? "According to the model, you are likely diabetic."
                : "According to the model, you are likely not diabetic.";
        resultLabel.setText(predMessage);

        String response = generateAResponseFromOpenAIForQuestionnaire(answers, predClass);
        recommendationArea.setText(response);

        centerPane.getChildren().clear();
        centerPane.getChildren().add(resultBox);

        applyFadeTransition(resultBox, 0, 1, 1000).play();
    }

    /**
     * Handle submit for custom dataset form
     */
    private void handleFormSubmit() {
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(100, 100);
        Label loadingLabel = new Label("Analyzing your inputs...");
        loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #555555;");
        VBox loadingBox = new VBox(loadingIndicator, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setSpacing(20);
        centerPane.getChildren().clear();
        centerPane.getChildren().add(loadingBox);

        Task<Void> predictionTask = new Task<>() {
            List<Double> values = new ArrayList<>();
            @Override
            protected Void call() throws Exception {
                // Convert text field inputs to values
                for (int i = 0; i < customTextFields.size(); i++) {
                    String text = customTextFields.get(i).getText().trim();
                    double val = 0.5; // default
                    if (!text.isEmpty()) {
                        try {
                            val = Double.parseDouble(text);
                        } catch (NumberFormatException e) {
                            // If invalid, just use default or handle differently
                            val = 0.5;
                        }
                    }
                    values.add(val);
                }
                // Simulate computation time
                Thread.sleep(2000);
                // On succeeded, we will display results
                this.updateValue(null);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                displayResultsForCustomDataset(values);
            }
        };
        new Thread(predictionTask).start();
    }

    private void displayResultsForCustomDataset(List<Double> values) {
        // Similar logic to questionnaire, but using numeric values
        GenericRecord record = buildGenericRecordFromValues(values);
        double prediction = randomForest.predict(record);
        double predClass = (prediction >= 0.5) ? 1.0 : 0.0;
        String predMessage = (predClass == 1.0)
                ? "According to the model, you are likely diabetic."
                : "According to the model, you are likely not diabetic.";
        resultLabel.setText(predMessage);

        String response = generateAResponseFromOpenAIForCustomDataset(values, predClass);
        recommendationArea.setText(response);
        centerPane.getChildren().clear();
        centerPane.getChildren().add(resultBox);

        applyFadeTransition(resultBox, 0, 1, 1000).play();
    }

    /**
     * Convert the user's answers (from yes/no/don't know) into a GenericRecord.
     */
    private GenericRecord buildGenericRecordFromAnswers(List<String> answers) {
        GenericRecord record = new GenericRecord();
        String[] fn = randomForest.getFeatureNames();

        for (int i = 0; i < fn.length; i++) {
            double value = 0.5;
            if (i < answers.size()) {
                value = getValueFromChoice(answers.get(i));
            }
            record.setFeature(fn[i], value);
        }
        record.setLabel(0.0);
        return record;
    }

    /**
     * Build GenericRecord from numeric values for custom dataset
     */
    private GenericRecord buildGenericRecordFromValues(List<Double> values) {
        GenericRecord record = new GenericRecord();
        String[] fn = randomForest.getFeatureNames();

        for (int i = 0; i < fn.length; i++) {
            double val = 0.5;
            if (i < values.size()) {
                val = values.get(i);
            }
            record.setFeature(fn[i], val);
        }
        record.setLabel(0.0);
        return record;
    }

    private double getValueFromChoice(String choice) {
        switch (choice) {
            case "Yes":
                return 1.0;
            case "No":
                return 0.0;
            case "I Don't Know":
                return 0.5;
            default:
                return 0.5;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private String generateAResponseFromOpenAIForQuestionnaire(List<String> userAnswers, double predClass) {
        StringBuilder prompt = new StringBuilder();
        if (predClass == 1.0) {
            prompt.append("The user’s inputs suggest they may be at risk for diabetes. ");
        } else {
            prompt.append("The user’s inputs suggest they may not be at high risk for diabetes. ");
        }
        prompt.append("They answered a series of yes/no/I don't know questions.\n");
        prompt.append("User Answers:\n");
        for (int i = 0; i < QUESTIONS.size(); i++) {
            String ans = (i < userAnswers.size()) ? userAnswers.get(i) : "No Response";
            prompt.append((i + 1)).append(". ").append(QUESTIONS.get(i)).append(" Answer: ").append(ans).append("\n");
        }
        prompt.append("\nPlease provide a detailed, empathetic health and lifestyle plan considering their inputs. ")
                .append("Include advice on diet, exercise, and follow-up with healthcare professionals. ")
                .append("If 'I Don't Know' was answered, encourage seeking professional guidance.");
        return callOpenAIAPI(prompt.toString());
    }

    private String generateAResponseFromOpenAIForCustomDataset(List<Double> values, double predClass) {
        StringBuilder prompt = new StringBuilder();
        if (predClass == 1.0) {
            prompt.append("The user’s inputs suggest they may be at risk for diabetes. ");
        } else {
            prompt.append("The user’s inputs suggest they may not be at high risk for diabetes. ");
        }
        prompt.append("They provided numeric values for the following features:\n");
        for (int i = 0; i < featureNames.length; i++) {
            prompt.append(featureNames[i]).append(": ").append(values.get(i)).append("\n");
        }

        prompt.append("\nPlease provide a detailed, empathetic health and lifestyle plan considering their inputs. ")
                .append("Include advice on diet, exercise, and follow-up with healthcare professionals. ")
                .append("If uncertain about any input, encourage seeking professional guidance.");
        return callOpenAIAPI(prompt.toString());
    }

    private String callOpenAIAPI(String prompt) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null) {
            return "OPENAI_API_KEY not set. Please set the environment variable.";
        }
        OpenAiService service = new OpenAiService(apiKey);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful medical assistant."));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .maxTokens(500)
                .temperature(0.7)
                .build();
        try {
            var result = service.createChatCompletion(request).getChoices();
            if (!result.isEmpty()) {
                return result.get(0).getMessage().getContent().trim();
            } else {
                return "No response from OpenAI.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling OpenAI API: " + e.getMessage();
        }
    }

    private FadeTransition applyFadeTransition(javafx.scene.Node node, double fromValue, double toValue, int durationMillis) {
        FadeTransition ft = new FadeTransition(Duration.millis(durationMillis), node);
        ft.setFromValue(fromValue);
        ft.setToValue(toValue);
        return ft;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
