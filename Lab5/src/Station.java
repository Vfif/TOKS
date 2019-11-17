import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Station {
    final private byte sourceAddress;
    private Stage stage;
    private TextArea inputTextArea;
    private TextArea outputTextArea;
    private TextArea debugTextArea;
    private ComboBox<Integer> comboBox;
    private List<byte[]> list = new ArrayList<>();
    private String line;
    private long time;

    Station(Integer tittle, Button button) {
        sourceAddress = tittle.byteValue();
        stage = new Stage();
        stage.setX(-200 + sourceAddress*400);
        stage.setY(50);
        stage.setTitle("Station" + tittle.toString());
        stage.setWidth(300);
        stage.setHeight(400);
        Label inputLabel = new Label("Input");
        inputTextArea = new TextArea();
        Label outputLabel = new Label("Output");
        outputTextArea = new TextArea();
        outputTextArea.setEditable(false);
        Label debugAndControlLabel = new Label("Debug&Control");
        debugTextArea = new TextArea();
        debugTextArea.setEditable(false);
        ObservableList<Integer> collection = FXCollections.observableArrayList(1, 2, 3, 4);
        comboBox = new ComboBox<>(collection);
        comboBox.setValue(tittle);
        Label sourceLabel = new Label("Source address: " + tittle);
        Label destinationLabel = new Label("Destin address:");
        HBox destinationHBox = new HBox(5, destinationLabel, comboBox);
        HBox hBox = new HBox(40, sourceLabel, destinationHBox);
        VBox vBox = new VBox(5, inputLabel, inputTextArea, outputLabel, outputTextArea,
                debugAndControlLabel, debugTextArea, hBox);
        vBox.setPadding(new Insets(5));
        vBox.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(vBox));
        stage.show();
        inputTextArea.setOnKeyReleased(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                if (button.isDisable()) {
                    String text = inputTextArea.getText();
                    if (sourceAddress == comboBox.getValue().byteValue()) {
                        line = text;
                    } else {
                        try {
                        for (byte symbol : text.getBytes("windows-1251")) {
                            byte[] array = new byte[6];
                            array[0] = "F".getBytes()[0];
                            array[1] = tittle.byteValue();
                            array[2] = comboBox.getValue().byteValue();
                            array[3] = " ".getBytes()[0];
                            array[4] = " ".getBytes()[0];
                            array[5] = symbol;
                            list.add(array);
                        }
                        }catch (UnsupportedEncodingException ex){}
                    }
                    inputTextArea.clear();
                }
            }
        });
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public byte getSourceAddress() {
        return sourceAddress;
    }

    public Stage getStage() {
        return stage;
    }

    public ComboBox<Integer> getComboBox() {
        return comboBox;
    }

    public TextArea getInputTextArea() {
        return inputTextArea;
    }

    public List<byte[]> getList() {
        return list;
    }

    public TextArea getOutputTextArea() {
        return outputTextArea;
    }

    public TextArea getDebugTextArea() {
        return debugTextArea;
    }
}
