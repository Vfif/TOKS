import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Monitor {

    private TextArea debugTextArea;
    private ComboBox<Integer> comboBox;
    private Button startButton;

    Monitor(Stage stage) {
        stage.setTitle("Monitor");
        stage.setX(600);
        stage.setY(470);
        stage.setWidth(300);
        stage.setHeight(200);
        Label debugAndControlLabel = new Label("Debug&Control");
        debugTextArea = new TextArea();
        debugTextArea.setEditable(false);
        ObservableList<Integer> collection = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100);
        comboBox = new ComboBox<>(collection);
        comboBox.setValue(5);
        Label collisionLabel = new Label("Hold time");
        startButton = new Button("Start");
        HBox hBox = new HBox(5, collisionLabel, comboBox);
        hBox.setAlignment(Pos.CENTER);
        VBox vBox = new VBox(5, debugAndControlLabel, debugTextArea, hBox, startButton);
        vBox.setPadding(new Insets(5));
        vBox.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(vBox));
        stage.show();
        startButton.setOnMouseClicked(e ->
                startButton.setDisable(true) );
    }

    public ComboBox<Integer> getComboBox() {
        return comboBox;
    }

    public Button getStartButton() {
        return startButton;
    }

    public TextArea getDebugTextArea() {
        return debugTextArea;
    }

    public void setDebugTextArea(TextArea debugTextArea) {
        this.debugTextArea = debugTextArea;
    }
}
