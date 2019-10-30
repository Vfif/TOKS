import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Date;
import java.util.Random;

public class Main extends Application {
    private TextArea inputTextArea;
    private TextArea outputTextArea;
    private TextArea debugTextArea;
    private ComboBox<Integer> comboBox;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        stage.setTitle("CSMA/CD");
        stage.setWidth(400);
        stage.setHeight(600);

        Label inputLabel = new Label("Input");
        inputTextArea = new TextArea();
        Label outputLabel = new Label("Output");
        outputTextArea = new TextArea();
        outputTextArea.setEditable(false);
        Label debugAndControlLabel = new Label("Debug&Control");
        debugTextArea = new TextArea();
        debugTextArea.setEditable(false);
        ObservableList<Integer> collection = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100);
        comboBox = new ComboBox<>(collection);
        comboBox.setValue(10);
        Label collisionLabel = new Label("Max collision count");
        HBox hBox = new HBox(5, collisionLabel, comboBox);
        hBox.setAlignment(Pos.CENTER);


        inputTextArea.setOnKeyReleased(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                inputTextArea.setDisable(true);
                comboBox.setDisable(true);
                debugTextArea.clear();
                String text = inputTextArea.getText();
                text = text.substring(0, text.length() - 1);
                int maxCollisionCount = comboBox.getValue();//comboBox.getSelectionModel().getSelectedItem().intValue();
                long time;
                boolean collision;
                for (String symbol : text.split("")) {
                    for (int j = 0; j < maxCollisionCount; j++) {
                        time = new Date().getTime();//Carrier Sense
                        collision = time % 2 == 1 & new Random().nextBoolean();//Collision Detection
                        if (collision) {
                            debugTextArea.appendText("X");
                            try {
                                Thread.sleep(new Random().nextInt((int) Math.pow(2, j)));
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            if (j == 0) debugTextArea.appendText("-");
                            outputTextArea.appendText(symbol);
                            break;
                        }
                    }
                    debugTextArea.appendText("\n");
                }
                outputTextArea.appendText("\n");
                debugTextArea.appendText("\n");
                inputTextArea.clear();
                inputTextArea.setDisable(false);
                comboBox.setDisable(false);
            }
        });
        VBox vBox = new VBox(5, inputLabel, inputTextArea, outputLabel, outputTextArea,
                debugAndControlLabel, debugTextArea, hBox);
        vBox.setPadding(new Insets(5));
        vBox.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(vBox));
        stage.show();
    }
}