package com.example.diabetes_predictor;

import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DiabetesPredictorFX extends Application {

    // Fields for the original Q&A mode
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
    private List<TextField> customTextFields = new ArrayList<>();
    private String[] featureNames;
    private Stage primaryStage;

    // Fields for the general RF interface mode
    private TextArea dataInputArea;
    private TextField targetField;
    private Button trainButton;
    private Button loadCsvButton;
    private Label statusLabel;
    private VBox predictionBox;
    private TextField predictInputField;
    private Label predictionResult;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showMainMenu();
    }

    private void showMainMenu() {
        BorderPane initialPane = new BorderPane();
        initialPane.setPadding(new Insets(20));

        Label initialTitle = new Label("Welcome to the Diabetes Predictor");
        initialTitle.setStyle("-fx-font-size: 24px; -fx-text-fill: #333333; -fx-font-weight: bold;");
        initialPane.setTop(new HBox(initialTitle));
        ((HBox) initialPane.getTop()).setAlignment(Pos.CENTER);
        ((HBox) initialPane.getTop()).setPadding(new Insets(10, 0, 20, 0));

        // Button 1: Original Diabetes Predictor
        Button originalModeButton = new Button("Use Original Diabetes Predictor");
        originalModeButton.setOnAction(e -> {
            instantiateAndTrainRandomForest();
            showOriginalModeMenu();
        });

        // Button 2: General Random Forest Interface
        Button generalModeButton = new Button("Use General Random Forest Interface");
        generalModeButton.setOnAction(e -> showGeneralRFInterface());

        HBox buttonBox = new HBox(20, originalModeButton, generalModeButton);
        buttonBox.setAlignment(Pos.CENTER);
        initialPane.setCenter(buttonBox);

        Scene initialScene = new Scene(initialPane, 600, 400);
        primaryStage.setTitle("Diabetes Predictor");
        primaryStage.setScene(initialScene);
        primaryStage.show();
    }

    private void instantiateAndTrainRandomForest() {
        randomForest = new RandomForest();
        boolean trained = randomForest.practiceOutput();
        if (!trained) {
            System.err.println("Warning: Random Forest training failed. Predictions may be invalid.");
        }
    }

    // -------------------- ORIGINAL Q&A MODE --------------------

    private void showOriginalModeMenu() {
        BorderPane initialPane = new BorderPane();
        initialPane.setPadding(new Insets(20));

        Label initialTitle = new Label("Diabetes Predictor (Original Mode)");
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
            loadCSVFromFileChooserForQuestions();
        });

        HBox buttonBox = new HBox(20, defaultButton, loadCsvButton);
        buttonBox.setAlignment(Pos.CENTER);
        initialPane.setCenter(buttonBox);

        Scene initialScene = new Scene(initialPane, 600, 400);
        primaryStage.setScene(initialScene);
        primaryStage.show();
    }

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
        Collections.addAll(QUESTIONS, defaultQuestions);
        Collections.addAll(QUESTION_IMAGES, defaultImages);
    }

    private void loadCSVFromFileChooserForQuestions() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File for Questions");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            boolean loaded = loadCustomQuestionsFromCSV(selectedFile);
            if (loaded) {
                isCustomDataset = false;
                buildQuestionnaireScene();
            } else {
                showAlert("Error", "Failed to load CSV. Please ensure it is in the correct format.");
            }
        }
    }

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

        buildResultBox();

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        currentQuestionIndex = 0;
        answers.clear();
        showQuestion(0, true);
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

    private String callOpenAIAPI(String prompt) {
        // Retrieve OpenAI API key from the environment variable
        String apiKey = "APIKEY";
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Environment variable OPENAI_API_KEY is not set or is empty.");
        }

        // Initialize the OpenAI service with the API key
        OpenAiService service = new OpenAiService(apiKey);

        // Create the list of chat messages
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "You are a helpful medical assistant."));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));

        // Create the chat completion request
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .maxTokens(1024)
                .temperature(0.7)
                .build();

        try {
            // Call the OpenAI API
            ChatCompletionResult result = service.createChatCompletion(request);

            // Extract and return the response content
            if (result != null && !result.getChoices().isEmpty()) {
                ChatMessage responseMessage = result.getChoices().get(0).getMessage();
                if (responseMessage != null && responseMessage.getContent() != null) {
                    return responseMessage.getContent().trim();
                }
            }
            return "No meaningful response from OpenAI.";
        } catch (Exception e) {
            // Handle API errors gracefully
            e.printStackTrace();
            return "Error communicating with OpenAI: " + e.getMessage();
        }
    }


    private String generateOpenAIPrompt(List<String> userAnswers, double prediction) {
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("You are a medical assistant specializing in Diabetes. ")
                .append("The user answered a series of questions related to health and lifestyle. ")
                .append("Based on their answers, a model predicted their likelihood of having diabetes.\n\n");

        if (prediction >= 0.5) {
            promptBuilder.append("The Random Forest model suggests the user is LIKELY diabetic.\n\n");
        } else {
            promptBuilder.append("The Random Forest model suggests the user is NOT likely diabetic.\n\n");
        }

        String highBloodPressure = (userAnswers.size() > 0) ? userAnswers.get(0) : "I Don't Know";
        String physicalActivity = (userAnswers.size() > 7) ? userAnswers.get(7) : "I Don't Know";
        String alcohol = (userAnswers.size() > 10) ? userAnswers.get(10) : "I Don't Know";
        String generalHealth = (userAnswers.size() > 13) ? userAnswers.get(13) : "I Don't Know";
        String gender = (userAnswers.size() > 17) ? userAnswers.get(17) : "I Don't Know";

        promptBuilder.append("User Answers Snapshot:\n")
                .append("High Blood Pressure: ").append(highBloodPressure).append("\n")
                .append("Regular Physical Activity: ").append(physicalActivity).append("\n")
                .append("Heavy Alcohol Consumption: ").append(alcohol).append("\n")
                .append("General Health: ").append(generalHealth).append("\n")
                .append("Gender: ").append(gender).append("\n\n");

        promptBuilder.append("Given these answers, provide a very detailed set of recommendations to either prevent or manage diabetes. ")
                .append("Tailor advice based on their specific answers.\n\n");

        // Example conditions based on some answers
        switch (highBloodPressure) {
            case "Yes":
                promptBuilder.append("Note: The user has high blood pressure, advise on managing it.\n");
                break;
            case "No":
                promptBuilder.append("Note: No high blood pressure reported, but still maintain healthy BP.\n");
                break;
            default:
                promptBuilder.append("Note: Uncertain about blood pressure, advise check-ups.\n");
                break;
        }

        switch (physicalActivity) {
            case "Yes":
                promptBuilder.append("They exercise regularly: encourage continuing.\n");
                break;
            case "No":
                promptBuilder.append("They don't exercise: encourage starting a routine.\n");
                break;
            default:
                promptBuilder.append("Uncertain about exercise: recommend at least 30 min/day.\n");
                break;
        }

        switch (alcohol) {
            case "Yes":
                promptBuilder.append("Heavy alcohol: recommend reduction.\n");
                break;
            case "No":
                promptBuilder.append("No heavy alcohol: maintain low intake.\n");
                break;
            default:
                promptBuilder.append("Uncertain alcohol use: advise moderation.\n");
                break;
        }

        switch (generalHealth) {
            case "Yes":
                promptBuilder.append("Good general health: maintain healthy habits.\n");
                break;
            case "No":
                promptBuilder.append("Poor general health: urgent lifestyle changes.\n");
                break;
            default:
                promptBuilder.append("Uncertain health: comprehensive check-ups.\n");
                break;
        }

        promptBuilder.append("\nNow provide detailed, practical recommendations (nutrition, exercise, medical check-ups, mental health support, etc.) tailored to these conditions.\n");

        return promptBuilder.toString();
    }
    private void displayResultsForQuestionnaire() {
        GenericRecord record = buildGenericRecordFromAnswers(answers);
        double prediction = randomForest.predict(record);
        double predClass = (prediction >= 0.5) ? 1.0 : 0.0;

        String predMessage = (predClass == 1.0)
                ? "According to the model, you are likely diabetic."
                : "According to the model, you are likely not diabetic.";
        resultLabel.setText(predMessage);

        String prompt = generateOpenAIPrompt(answers, prediction);
        String openAIResponse = callOpenAIAPI(prompt);
        recommendationArea.setText(openAIResponse);

        // Parameters for the bell curve
        double mean = 0.5;
        double stdDev = 0.15;
        int numPoints = 200; // The resolution of the curve

        // Create the axes for the bell curve chart
        NumberAxis xAxis = new NumberAxis(0, 1, 0.1);
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Risk");
        yAxis.setLabel("Density");

        // Create the chart
        LineChart<Number, Number> bellChart = new LineChart<>(xAxis, yAxis);
        bellChart.setTitle("Risk Distribution");
        bellChart.setCreateSymbols(false);

        // Generate the bell curve data
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Bell Curve");
        for (int i = 0; i <= numPoints; i++) {
            double xVal = i / (double) numPoints;
            double yVal = gaussian(xVal, mean, stdDev);
            series.getData().add(new XYChart.Data<>(xVal, yVal));
        }
        bellChart.getData().add(series);

        // Add a point for the user's prediction on the curve
        XYChart.Series<Number, Number> userPointSeries = new XYChart.Series<>();
        userPointSeries.setName("Your Risk");
        double userYVal = gaussian(prediction, mean, stdDev);
        userPointSeries.getData().add(new XYChart.Data<>(prediction, userYVal));
        bellChart.getData().add(userPointSeries);

        // Add a vertical line marker at the user's prediction
        Line userLine = new Line();
        userLine.setStroke(Color.RED);
        userLine.setStrokeWidth(2);

        // Update user line position whenever chart is laid out or resized
        bellChart.widthProperty().addListener((obs, oldW, newW) -> updateUserLinePosition(bellChart, userLine, prediction));
        bellChart.heightProperty().addListener((obs, oldH, newH) -> updateUserLinePosition(bellChart, userLine, prediction));
        bellChart.layout();
        updateUserLinePosition(bellChart, userLine, prediction);

        // Create a StackPane to overlay the line on the chart
        StackPane chartPane = new StackPane(bellChart, userLine);
        chartPane.setPrefHeight(300);
        chartPane.setPrefWidth(500);

        // Create an HBox to display recommendations and the chart side-by-side
        HBox visualizationBox = new HBox(20, recommendationArea, chartPane);
        visualizationBox.setAlignment(Pos.CENTER);

        // Categorize risk based on prediction
        String riskCategory;
        if (prediction < 0.2) {
            riskCategory = "Very Low Risk";
        } else if (prediction < 0.4) {
            riskCategory = "Low Risk";
        } else if (prediction < 0.6) {
            riskCategory = "Moderate Risk";
        } else if (prediction < 0.8) {
            riskCategory = "High Risk";
        } else {
            riskCategory = "Very High Risk";
        }

        Label riskCategoryLabel = new Label("Your position on the curve: " + riskCategory);
        riskCategoryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #444444;");

        // Clear the resultBox and add the result label, visualization, and risk category
        resultBox.getChildren().clear();
        resultBox.getChildren().addAll(resultLabel, visualizationBox, riskCategoryLabel);

        centerPane.getChildren().clear();
        centerPane.getChildren().add(resultBox);

        applyFadeTransition(resultBox, 0, 1, 1000).play();
    }

    private double gaussian(double x, double m, double sd) {
        return (1.0 / (sd * Math.sqrt(2 * Math.PI))) *
                Math.exp(-0.5 * Math.pow((x - m) / sd, 2));
    }

    private void updateUserLinePosition(LineChart<Number, Number> chart, Line line, double prediction) {
        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();

        double xPos = xAxis.getDisplayPosition(prediction);

        double top = yAxis.getDisplayPosition(yAxis.getUpperBound());
        double bottom = yAxis.getDisplayPosition(yAxis.getLowerBound());

        line.setStartX(xPos);
        line.setEndX(xPos);
        line.setStartY(top);
        line.setEndY(bottom);
    }
    public void setRandomForest(RandomForest randomForest) {
        this.randomForest = randomForest;
    }
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
                for (int i = 0; i < customTextFields.size(); i++) {
                    String text = customTextFields.get(i).getText().trim();
                    double val = 0.5;
                    if (!text.isEmpty()) {
                        try {
                            val = Double.parseDouble(text);
                        } catch (NumberFormatException e) {
                            val = 0.5;
                        }
                    }
                    values.add(val);
                }
                Thread.sleep(2000);
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
        GenericRecord record = buildGenericRecordFromValues(values);
        double prediction = randomForest.predict(record);
        double predClass = (prediction >= 0.5) ? 1.0 : 0.0;
        String predMessage = (predClass == 1.0)
                ? "According to the model, you are likely diabetic."
                : "According to the model, you are likely not diabetic.";
        resultLabel.setText(predMessage);

        List<String> mockAnswers = IntStream.range(0, QUESTIONS.size())
                .mapToObj(i -> "I Don't Know")
                .collect(Collectors.toList());

        String prompt = generateOpenAIPrompt(mockAnswers, prediction);
        String openAIResponse = callOpenAIAPI(prompt);

        recommendationArea.setText(openAIResponse);

        centerPane.getChildren().clear();
        centerPane.getChildren().add(resultBox);

        applyFadeTransition(resultBox, 0, 1, 1000).play();
    }

    protected GenericRecord buildGenericRecordFromAnswers(List<String> answers) {
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

    private FadeTransition applyFadeTransition(javafx.scene.Node node, double fromValue, double toValue, int durationMillis) {
        FadeTransition ft = new FadeTransition(Duration.millis(durationMillis), node);
        ft.setFromValue(fromValue);
        ft.setToValue(toValue);
        return ft;
    }

    // -------------------- GENERAL RF INTERFACE MODE --------------------
    private void showGeneralRFInterface() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        Label title = new Label("General Random Forest Interface");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        HBox titleBox = new HBox(title);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(10, 0, 20, 0));
        root.setTop(titleBox);

        Label instructions = new Label(
                "Instructions:\n" +
                        "1. Paste your CSV data in the text area below or load a CSV file using the 'Load CSV' button.\n" +
                        "2. Specify the target variable (the column name you want to predict).\n" +
                        "3. Click 'Train Model' to train a Random Forest on your data.\n\n" +
                        "Data Format:\n" +
                        "- The first line: header with column names.\n" +
                        "- The target column must be numeric (e.g., 0/1 for classification).\n" +
                        "- Feature columns must be numeric.\n\n" +
                        "After training, enter a single new data row (without the target) to predict.\n" +
                        "Values must be comma-separated and match the feature columns order."
        );
        instructions.setWrapText(true);

        dataInputArea = new TextArea();
        dataInputArea.setPrefRowCount(15);
        dataInputArea.setWrapText(true);

        targetField = new TextField();
        targetField.setPromptText("Target Variable Name");
        trainButton = new Button("Train Model");
        trainButton.setOnAction(e -> trainGeneralModel());

        loadCsvButton = new Button("Load CSV");
        loadCsvButton.setOnAction(e -> loadCSVFromFileChooserGeneral());

        HBox trainingControls = new HBox(10, new Label("Target:"), targetField, trainButton, loadCsvButton);
        trainingControls.setAlignment(Pos.CENTER_LEFT);

        VBox centerBox = new VBox(10, instructions, dataInputArea, trainingControls);
        centerBox.setAlignment(Pos.TOP_CENTER);
        centerBox.setPadding(new Insets(10, 0, 20, 0));
        root.setCenter(centerBox);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #555;");

        predictionBox = new VBox(10);
        predictionBox.setAlignment(Pos.CENTER_LEFT);
        predictionBox.setPadding(new Insets(10, 0, 0, 0));
        predictionBox.setVisible(false);

        Label predInstructions = new Label(
                "Enter a new data row (comma-separated values) without the target.\n" +
                        "Values must be in the same order as the feature columns."
        );
        predInstructions.setWrapText(true);

        predictInputField = new TextField();
        predictInputField.setPrefWidth(400);
        predictInputField.setPromptText("e.g. 5.1,3.5,1.4,0.2");

        Button predictButton = new Button("Predict");
        predictButton.setOnAction(e -> handleGeneralPrediction());

        predictionResult = new Label();
        predictionResult.setStyle("-fx-font-weight: bold;");

        predictionBox.getChildren().addAll(
                new Label("Prediction:"),
                predInstructions,
                predictInputField,
                predictButton,
                predictionResult
        );

        VBox bottomBox = new VBox(10, statusLabel, predictionBox);
        bottomBox.setAlignment(Pos.TOP_CENTER);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("General Random Forest Interface");
        primaryStage.show();
    }

    private void loadCSVFromFileChooserGeneral() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                dataInputArea.setText(sb.toString().trim());
                statusLabel.setText("CSV loaded. Now specify the target and train.");
                statusLabel.setStyle("-fx-text-fill: #555;");
            } catch (IOException e) {
                showErrorGeneral("Failed to load CSV: " + e.getMessage());
            }
        }
    }

    private void trainGeneralModel() {
        String csvData = dataInputArea.getText().trim();
        String targetName = targetField.getText().trim();

        if (csvData.isEmpty()) {
            showErrorGeneral("Please provide CSV data.");
            return;
        }
        if (targetName.isEmpty()) {
            showErrorGeneral("Please provide a target variable name.");
            return;
        }

        statusLabel.setText("Training model, please wait...");
        statusLabel.setStyle("-fx-text-fill: #555;");

        randomForest = new RandomForest();
        Task<Boolean> trainTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return randomForest.trainFromCSVString(csvData, targetName);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Boolean result = getValue();
                if (result) {
                    statusLabel.setText("Model trained successfully!");
                    statusLabel.setStyle("-fx-text-fill: green;");
                    featureNames = randomForest.getFeatureNames();
                    predictionBox.setVisible(true);
                } else {
                    showErrorGeneral("Training failed. Check console for details.");
                    predictionBox.setVisible(false);
                }
            }

            @Override
            protected void failed() {
                super.failed();
                showErrorGeneral("Training failed: " + getException().getMessage());
                predictionBox.setVisible(false);
            }
        };

        new Thread(trainTask).start();
    }

    private void handleGeneralPrediction() {
        if (randomForest == null) {
            showErrorGeneral("Model not trained yet!");
            return;
        }

        String inputLine = predictInputField.getText().trim();
        if (inputLine.isEmpty()) {
            showErrorGeneral("Please enter feature values.");
            return;
        }

        String[] parts = inputLine.split(",");
        if (parts.length != featureNames.length) {
            showErrorGeneral("Number of values does not match number of features. Expected: " + featureNames.length);
            return;
        }

        GenericRecord record = new GenericRecord();
        for (int i = 0; i < featureNames.length; i++) {
            double val;
            try {
                val = Double.parseDouble(parts[i].trim());
            } catch (NumberFormatException e) {
                showErrorGeneral("Invalid numeric value: " + parts[i]);
                return;
            }
            record.setFeature(featureNames[i], val);
        }

        double prediction = randomForest.predict(record);
        predictionResult.setText("Predicted value: " + prediction);
    }

    private void showErrorGeneral(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
