import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static java.lang.Math.floor;

public class Main2 extends Application {
    private static final int ROWS = 5;
    private static final int COLUMNS = 5;
    private TextField textField;
    private VBox historyEntries;
    private StringBuilder first;
    private StringBuilder second;
    private StringBuilder current;
    private Pair<String, BiFunction<Double, Double, Double>> operator;
    private History history;
    private List<Pair<String, EventHandler<MouseEvent>>> buttons = new ArrayList<>() {{
        add(new Pair<>("log", event -> setOperator(new Pair<>("log", (i, j) -> logaritm(j, i)))));
        add(new Pair<>("n!", event -> setOperator(new Pair<>("!", (i, j) -> {
            double result = gamma(i, i + 1.0 / (12 * i - 1.0 / (10 * i)));
            return i == Math.floor(i) ? Math.round(result) : result;
        }))));
        add(new Pair<>("DEL", event -> delete())); //new del
        add(new Pair<>("C", event -> reset())); //new C
        add(new Pair<>("/", event -> setOperator(new Pair<>("/", (i, j) -> i / j))));
        add(new Pair<>("π", event -> pi()));
        add(new Pair<>("7", event -> append("7")));
        add(new Pair<>("8", event -> append("8")));
        add(new Pair<>("9", event -> append("9"))); //"/"
        add(new Pair<>("*", event -> setOperator(new Pair<>("*", (i, j) -> i * j)))); //del
        add(new Pair<>("x√y", event -> setOperator(new Pair<>("√", (i, j) -> juur(i, j)))));
        add(new Pair<>("4", event -> append("4")));
        add(new Pair<>("5", event -> append("5")));
        add(new Pair<>("6", event -> append("6"))); //"*"
        add(new Pair<>("-", event -> setOperator(new Pair<>("-", (i, j) -> i - j)))); //"C"
        add(new Pair<>("x^y", event -> setOperator(new Pair<>("^", (i, j) -> astenda(i, j)))));
        add(new Pair<>("1", event -> append("1")));
        add(new Pair<>("2", event -> append("2")));
        add(new Pair<>("3", event -> append("3"))); //"-"
        add(new Pair<>("+", event -> setOperator(new Pair<>("+", (i, j) -> i + j)))); //"+"
        add(null);
        add(new Pair<>("-", event -> minus()));
        add(new Pair<>("0", event -> append("0")));
        add(new Pair<>(".", event -> append("."))); //"."
        add(new Pair<>("=", event -> call())); //"="
    }};

    public Main2() {
        first = new StringBuilder();
        second = new StringBuilder();
        history = new History();

        current = first;
    }

    @Override
    public void start(Stage stage) {
        HBox root = new HBox();
        VBox vBox = new VBox();

        textField = new TextField();
        textField.setPrefHeight(100);
        textField.setEditable(false);

        GridPane gridPane = new GridPane();
        gridPane.maxWidth(Double.MAX_VALUE);
        gridPane.maxHeight(Double.MAX_VALUE);

        for (int i = 0; i < ROWS; ++i) {
            for (int j = 0; j < COLUMNS; ++j) {
                Pair<String, EventHandler<MouseEvent>> buttonData = buttons.get(i + j * ROWS);

                if (buttonData == null) {
                    continue;
                }

                Button button = new Button(buttonData.getKey());
                button.setOnMouseClicked(buttonData.getValue());
                button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                GridPane.setVgrow(button, Priority.ALWAYS);
                GridPane.setHgrow(button, Priority.ALWAYS);

                gridPane.add(button, i, j);
            }
        }

        VBox sideMenu = new VBox();
        Button clearButton = new Button("Kustuta ajalugu");
        clearButton.setMaxWidth(Double.MAX_VALUE);

        clearButton.setOnMouseClicked(this::clearHistory);

        historyEntries = new VBox();

        ScrollPane scrollPane = new ScrollPane(historyEntries);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setMinWidth(200);

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        for (String item : history.get()) {
            pushHistory(item, false);
        }

        sideMenu.getChildren().addAll(clearButton, scrollPane);

        root.getChildren().addAll(vBox, sideMenu);
        HBox.setHgrow(vBox, Priority.ALWAYS);

        vBox.getChildren().addAll(textField, gridPane);
        VBox.setVgrow(gridPane, Priority.ALWAYS);

        stage.setScene(new Scene(root, 700, 500));
        stage.setOnCloseRequest(event -> history.save());
        stage.show();
    }

    private void pushHistory(String text, boolean append) {
        Label label = new Label(text);
        label.setPadding(new Insets(10, 10, 10, 10));

        label.setOnMouseClicked(event -> {
            current.setLength(0);
            current.append(label.getText());
            update();
        });

        historyEntries.getChildren().add(0, label);

        if (append) {
            history.push(text);
            reset();
            textField.setText(text);
        }
    }

    private void append(String number) {
        current.append(number);

        try {
            Double.parseDouble(current.toString());
        } catch (NumberFormatException exception) {
            current.setLength(current.length() - 1);
        }

        update();
    }

    private void pi() {
        current.setLength(0);
        current.append(Double.toString(Math.PI));

        update();
    }

    private void minus() {
        if (current.length() == 0) {
            return;
        }

        double value = Double.parseDouble(current.toString());

        if (value > 0) {
            current.insert(0, "-");
        } else if (value < 0) {
            current.replace(0, 1, "");
        }

        update();
    }

    private void call() {
        if (first.length() > 0 && (second.length() > 0 || operator != null && operator.getKey().equals("!"))) {
            double i = Double.parseDouble(first.toString());
            double j = second.length() > 0 ? Double.parseDouble(second.toString()) : 0;

            pushHistory(Double.toString(operator.getValue().apply(i, j)), true);
        } else if (first.length() > 0 && operator == null) {
            pushHistory(Double.toString(Double.parseDouble(first.toString())), true);
        }
    }

    private void reset() {
        first.setLength(0);
        second.setLength(0);
        current = first;
        operator = null;

        update();
    }

    private void clearHistory(MouseEvent event) {
        historyEntries.getChildren().clear();
        history.clear();
    }

    private void update() {
        textField.setText(first.toString() + (operator == null ? "" : operator.getKey()) + second.toString());
    }

    private void delete() {
        if (current.length() > 0) {
            current.setLength(current.length() - 1);
        } else if (current == second) {
            operator = null;
            current = first;
        }

        update();
    }

    private void setOperator(Pair<String, BiFunction<Double, Double, Double>> operator) {
        if (first.length() != 0 && second.length() == 0) {
            this.operator = operator;
            this.current = this.second;

            update();
        }

        call();
    }

    private double astenda(double num1, double num2) {
        return Math.pow(num1, num2);
    }

    public double juur(double a1, double a2) {
        return Math.pow(a2, 1.0 / a1);
    }

    public double logaritm(double a1, double a2) {
        return Math.log(a1) / Math.log(a2);
    }

    public static double gamma(double z, double tmp2) {
        double tmp1 = Math.sqrt((2 * Math.PI) * z);
        double tmp3 = (1 + (1 / 12 * z) + (1 / (288 * z * z)) - (139 / (51840 * z * z * z)) - (571 / (2488320 * z * z * z * z)));
        //tmp2 = z + 1.0/(12 * z - 1.0/(10*z));
        //tmp2 = Math.pow(z/Math.E, z); //whoops
        tmp2 = Math.pow(tmp2 / Math.E, z);
        return floor((tmp1 * tmp2 * tmp3) * 1000 + 0.5) / 1000;
    }

    public static void main(String[] args) {
        launch(Main2.class, args);
    }
}