import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jssc.*;

import java.util.concurrent.Semaphore;

public class Main extends Application {
    private static final String NON_ACTIVE_FIELD = "Non-active";
    private static final String DISCONNECT = "Disconnected";
    private static final String CONNECT = "Connected";
    private SerialPort serialPort;
    private TextArea inputTextArea;
    private TextArea outputTextArea;
    private RadioButton buttonRTS;
    private RadioButton buttonDTR;
    private RadioButton buttonOFF;
    private ComboBox<String> comboBox;
    private SerialPortCheckThread thread = new SerialPortCheckThread();
    private Semaphore semaphore = new Semaphore(1);


    public static void main(String[] args) {
        launch(args);
    }

    private void sendRTS() throws SerialPortException {
        try {
            String line = inputTextArea.getText() + "\r\n";
            byte[] array = line.getBytes();
            if (array.length > 2) {
                semaphore.acquire();
                for (int i = 0; i < array.length; i++) {
                    serialPort.setRTS(true);
                    int timeout = 1000;
                    do {
                        timeout--;
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ex) {
                        }
                    } while (!serialPort.isCTS() && timeout != 0);

                    if (serialPort.isCTS() && buttonRTS.isSelected()) {
                        serialPort.setRTS(false);
                        timeout = 1000;
                        do {
                            timeout--;
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException ex) {
                            }
                        } while (serialPort.isCTS() && timeout != 0);

                        serialPort.writeByte(array[i]);
                    } else {
                        if (i == 0) {
                            serialPort.setRTS(false);
                            semaphore.release();
                            return;
                        }
                    }
                }
                inputTextArea.clear();
                semaphore.release();
            }
        } catch (InterruptedException ex) {
            ex.getStackTrace();
        }
    }

    private void sendDRS() throws SerialPortException {
        try {
            String line = inputTextArea.getText() + "\r\n";
            byte[] array = line.getBytes();
            if (array.length > 2) {
                semaphore.acquire();
                for (int i = 0; i < array.length; i++) {
                    serialPort.setDTR(true);
                    int timeout = 1000;
                    do {
                        timeout--;
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ex) {
                        }
                    } while (!serialPort.isDSR() && timeout != 0);

                    if (serialPort.isDSR() && buttonDTR.isSelected()) {
                        serialPort.setDTR(false);
                        timeout = 1000;
                        do {
                            timeout--;
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException ex) {
                            }
                        } while (serialPort.isDSR() && timeout != 0);

                        serialPort.writeByte(array[i]);
                    } else {
                        if (i == 0) {
                            serialPort.setDTR(false);
                            semaphore.release();
                            return;
                        }
                    }
                }
                inputTextArea.clear();
                semaphore.release();
            }
        } catch (InterruptedException ex) {
            ex.getStackTrace();
        }
    }

    public void start(Stage stage) {
        stage.setTitle("COM port");
        stage.setWidth(400);
        stage.setHeight(500);

        Label inputLabel = new Label("Input");
        inputTextArea = new TextArea();
        Label outputLabel = new Label("Output");
        outputTextArea = new TextArea();
        outputTextArea.setEditable(false);
        Label debugAndControlLabel = new Label("Debug&Control");

        ToggleGroup group = new ToggleGroup();
        Label modeLabel = new Label("Mode:");
        buttonRTS = new RadioButton("RTS/CTS");
        buttonDTR = new RadioButton("DTR/DSR");
        buttonOFF = new RadioButton("No flow control");
        buttonRTS.setToggleGroup(group);
        buttonDTR.setToggleGroup(group);
        buttonOFF.setToggleGroup(group);
        VBox buttonVBox = new VBox(modeLabel, buttonRTS, buttonDTR, buttonOFF);
        buttonOFF.setSelected(true);

        ObservableList<String> items = FXCollections.observableArrayList(
                SerialPortList.getPortNames());
        items.add(NON_ACTIVE_FIELD);

        comboBox = new ComboBox<String>(items);
        comboBox.setPromptText(NON_ACTIVE_FIELD);
        Label comPortLabel = new Label("COM port:");
        Label label = new Label(DISCONNECT);
        VBox comboVBox = new VBox(5, comPortLabel, comboBox, label);
        comboVBox.setAlignment(Pos.CENTER);
        HBox hBox = new HBox(30, comboVBox, buttonVBox);
        hBox.setAlignment(Pos.CENTER);


        inputTextArea.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                try {
                    if (serialPort != null && !serialPort.getPortName().equals(NON_ACTIVE_FIELD) && serialPort.isOpened()) {
                        new Thread(() -> {
                            buttonDTR.setDisable(true);
                            buttonOFF.setDisable(true);
                            comboBox.setDisable(true);
                        });
                        if (buttonOFF.isSelected()) {
                            serialPort.writeString(inputTextArea.getText());
                            inputTextArea.clear();
                        } else if (buttonRTS.isSelected()) {
                            sendRTS();
                        } else if (buttonDTR.isSelected()) {
                            sendDRS();
                        }
                        new Thread(() -> {
                            buttonDTR.setDisable(false);
                            buttonOFF.setDisable(false);
                            comboBox.setDisable(false);
                        });
                    }
                } catch (SerialPortException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("");
                    alert.setContentText("Not all information send");
                    alert.showAndWait();
                }
            }
        });
        buttonRTS.setOnAction(e -> {
            if (!thread.isFlag()) {
                thread = new SerialPortCheckThread();
                thread.setFlag(true);
                if (serialPort != null && serialPort.isOpened()) thread.start();
            }
        });

        buttonDTR.setOnAction(e -> {
            if (!thread.isFlag()) {
                thread = new SerialPortCheckThread();
                thread.setFlag(true);
                if (serialPort != null && serialPort.isOpened()) thread.start();
            }
        });

        buttonOFF.setOnAction(e -> {
            if (thread.isFlag()) {
                thread.setFlag(false);
            }
        });

        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (serialPort != null && serialPort.isOpened()) {
                    thread.setFlag(false);
                    serialPort.closePort();
                    label.setText(DISCONNECT);
                }
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
            serialPort = new SerialPort(newValue);
            if (!newValue.equals(NON_ACTIVE_FIELD)) {
                try {
                    serialPort.openPort();
                    serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE, false, false);
                    serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
                    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                            SerialPort.FLOWCONTROL_RTSCTS_OUT);
                    serialPort.addEventListener(new SerialPortReader());
                    label.setText(CONNECT);
                    if (buttonRTS.isSelected() || buttonDTR.isSelected()) {
                        thread = new SerialPortCheckThread();
                        thread.setFlag(true);
                        thread.start();
                    }

                } catch (SerialPortException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Incorrect COM port name");
                    alert.setContentText("Please, choose the correct one");
                    alert.showAndWait();
                    label.setText(DISCONNECT);
                }
            }
        });

        stage.setOnCloseRequest(e -> {
            if (serialPort != null && serialPort.isOpened()) {
                try {
                    serialPort.closePort();
                    thread.setFlag(false);
                } catch (SerialPortException ex) {
                    ex.printStackTrace();
                }
            }
            stage.close();
        });

        VBox vBox = new VBox(5, inputLabel, inputTextArea, outputLabel, outputTextArea, debugAndControlLabel, hBox);
        vBox.setPadding(new Insets(5));
        vBox.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(vBox));
        stage.show();


    }

    class SerialPortReader implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0 && buttonOFF.isSelected()) {
                try {
                    String line = serialPort.readString(event.getEventValue());
                    outputTextArea.appendText(line + "\r\n");
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            } else if(event.isRXCHAR() && event.getEventValue() > 0 && semaphore.availablePermits() == 1){
                try {
                    String line = serialPort.readString(event.getEventValue());
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    class SerialPortCheckThread extends Thread {
        private boolean flag;

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        public boolean isFlag() {
            return flag;
        }

        @Override
        public void run() {
            while (flag) {
                try {
                    String symbol;
                    if (serialPort.isOpened() && buttonRTS.isSelected() && serialPort.isCTS() && semaphore.availablePermits() == 1) {
                        buttonDTR.setDisable(true);
                        buttonOFF.setDisable(true);
                        comboBox.setDisable(true);
                        inputTextArea.setDisable(true);
                        StringBuilder line = new StringBuilder();
                        do {
                            serialPort.setRTS(true);
                            int timeout = 1000;
                            do {
                                timeout--;
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException ex) {
                                }
                            } while (serialPort.isCTS() && timeout != 0);
                            serialPort.setRTS(false);
                            symbol = new String(serialPort.readBytes(1));
                            line.append(symbol);
                        } while (!symbol.equals("\n"));
                        outputTextArea.appendText(new String(line));
                        comboBox.setDisable(false);
                        buttonDTR.setDisable(false);
                        buttonOFF.setDisable(false);
                        inputTextArea.setDisable(false);
                    } else if (serialPort.isOpened() && buttonDTR.isSelected() && serialPort.isDSR() && semaphore.availablePermits() == 1) {
                        buttonDTR.setDisable(true);
                        buttonOFF.setDisable(true);
                        comboBox.setDisable(true);
                        inputTextArea.setDisable(true);
                        StringBuilder line = new StringBuilder();
                        do {
                            serialPort.setDTR(true);
                            int timeout = 1000;
                            do {
                                timeout--;
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException ex) {
                                }
                            } while (serialPort.isDSR() && timeout != 0);
                            serialPort.setDTR(false);
                            symbol = new String(serialPort.readBytes(1));
                            line.append(symbol);
                        } while (!symbol.equals("\n"));
                        outputTextArea.appendText(new String(line));
                        comboBox.setDisable(false);
                        buttonDTR.setDisable(false);
                        buttonOFF.setDisable(false);
                        inputTextArea.setDisable(false);
                    }
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}