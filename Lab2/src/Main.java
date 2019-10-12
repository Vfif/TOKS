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
    private TextArea debugTextArea;
    private RadioButton buttonRTS;
    private RadioButton buttonDTR;
    private RadioButton buttonOFF;
    private ComboBox<String> comboBox;
    private SerialPortCheckThread thread = new SerialPortCheckThread();
    private Semaphore semaphore = new Semaphore(1);
    private ComboBox<Integer> destinationAddress;
    private ComboBox<Integer> sourceAddress;
    private CheckBox checkButtonFSC;

    public static void main(String[] args) {
        launch(args);
    }

    private void send(boolean isFlowControl, boolean isRTS) throws SerialPortException {
        try {
            String line = inputTextArea.getText();
            if (line.length() > 0) {
                byte dstAddress = destinationAddress.getSelectionModel().getSelectedItem().byteValue();
                byte srcAddress = sourceAddress.getSelectionModel().getSelectedItem().byteValue();
                if (dstAddress == srcAddress) return;
                boolean k = false;
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) > 127) {
                        k = true;
                        break;
                    }
                }
                if (k) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Russian language is not support");
                    alert.showAndWait();
                    return;
                }
                semaphore.acquire();
                String newline = Pack.pack(line, dstAddress, srcAddress, (byte) (checkButtonFSC.isSelected() ? 1 : 0), debugTextArea);
                for (int i = 0; i < newline.length(); i++) {
                    if (isFlowControl) {
                        if (isRTS) {
                            serialPort.setRTS(true);
                            int timeout = 1000;
                            do {
                                timeout--;
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException ex) {
                                    ex.getStackTrace();
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
                                        ex.getStackTrace();
                                    }
                                } while (serialPort.isCTS() && timeout != 0);

                                serialPort.writeByte((byte) newline.charAt(i));
                            } else {
                                if (i == 0) {
                                    serialPort.setRTS(false);
                                    semaphore.release();
                                    return;
                                }
                            }
                        } else {
                            serialPort.setDTR(true);
                            int timeout = 1000;
                            do {
                                timeout--;
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException ex) {
                                    ex.getStackTrace();
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
                                        ex.getStackTrace();
                                    }
                                } while (serialPort.isDSR() && timeout != 0);

                                serialPort.writeByte((byte) newline.charAt(i));
                            } else {
                                if (i == 0) {
                                    serialPort.setDTR(false);
                                    semaphore.release();
                                    return;
                                }
                            }
                        }
                    } else {
                        serialPort.writeByte((byte) newline.charAt(i));
                    }
                }
                inputTextArea.clear();
                semaphore.release();
            }
        } catch (InterruptedException ex) {
            ex.getStackTrace();
        }
    }

    private void receive(boolean isFlowControl, boolean isRTS) throws SerialPortException {
        buttonRTS.setDisable(true);
        buttonDTR.setDisable(true);
        buttonOFF.setDisable(true);
        comboBox.setDisable(true);
        inputTextArea.setDisable(true);
        StringBuilder line = new StringBuilder();
        Unpack st = new Unpack();
        if (isFlowControl) {
            if (isRTS) {
                do {
                    serialPort.setRTS(true);
                    int timeout = 1000;
                    do {
                        timeout--;
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ex) {
                            ex.getStackTrace();
                        }
                    } while (serialPort.isCTS() && timeout != 0);
                    serialPort.setRTS(false);
                } while (!st.unByteStaffing(line, serialPort.readBytes(1)[0]));
            } else {
                do {
                    serialPort.setDTR(true);
                    int timeout = 1000;
                    do {
                        timeout--;
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ex) {
                            ex.getStackTrace();
                        }
                    } while (serialPort.isDSR() && timeout != 0);
                    serialPort.setDTR(false);
                } while (!st.unByteStaffing(line, serialPort.readBytes(1)[0]));
            }
        } else {
            while (!st.unByteStaffing(line, serialPort.readBytes(1)[0]));
        }
        if (st.FCS == 0 && st.destAddress == sourceAddress.getSelectionModel().getSelectedItem().byteValue()
                && (isFlowControl || !isRTS))
            outputTextArea.appendText(new String(line));
        buttonRTS.setDisable(false);
        buttonDTR.setDisable(false);
        buttonOFF.setDisable(false);
        comboBox.setDisable(false);
        inputTextArea.setDisable(false);
    }

    public void start(Stage stage) {
        stage.setTitle("COM port");
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

        Label dstLabel = new Label("Destin Address:");
        ObservableList<Integer> collection = FXCollections.observableArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254, 255);
        destinationAddress = new ComboBox<>(collection);
        destinationAddress.setValue(0);
        Label srcLabel = new Label("Source Address:");
        sourceAddress = new ComboBox<>(collection);
        sourceAddress.setValue(0);
        Label labelFSC = new Label("FCS:");
        checkButtonFSC = new CheckBox();
        HBox boxFSC = new HBox(5, labelFSC, checkButtonFSC);
        VBox packVBox = new VBox(boxFSC, dstLabel, destinationAddress, srcLabel, sourceAddress);

        ToggleGroup group = new ToggleGroup();
        Label modeLabel = new Label("Mode:");
        buttonRTS = new RadioButton("RTS/CTS");
        buttonDTR = new RadioButton("DTR/DSR");
        buttonOFF = new RadioButton("No flow control");
        buttonRTS.setToggleGroup(group);
        buttonDTR.setToggleGroup(group);
        buttonOFF.setToggleGroup(group);
        VBox buttonVBox = new VBox(modeLabel, buttonRTS, buttonDTR, buttonOFF);
        buttonVBox.setAlignment(Pos.CENTER);
        buttonOFF.setSelected(true);

        ObservableList<String> items = FXCollections.observableArrayList(
                SerialPortList.getPortNames());
        items.add(NON_ACTIVE_FIELD);

        comboBox = new ComboBox<>(items);
        comboBox.setPromptText(NON_ACTIVE_FIELD);
        Label comPortLabel = new Label("COM port:");
        Label label = new Label(DISCONNECT);
        VBox comboVBox = new VBox(5, comPortLabel, comboBox, label);
        comboVBox.setAlignment(Pos.CENTER);
        HBox hBox = new HBox(30, comboVBox, buttonVBox, packVBox);
        hBox.setAlignment(Pos.CENTER);

        inputTextArea.setOnKeyReleased(e -> {
            if (e.getCode().equals(KeyCode.ENTER)) {
                try {
                    if (serialPort != null && !serialPort.getPortName().equals(NON_ACTIVE_FIELD) && serialPort.isOpened()) {
                        new Thread(() -> {
                            buttonRTS.setDisable(true);
                            buttonDTR.setDisable(true);
                            buttonOFF.setDisable(true);
                            comboBox.setDisable(true);
                        });
                        if (buttonOFF.isSelected()) {
                            send(false, false);
                        } else if (buttonRTS.isSelected()) {
                            send(true, true);
                        } else if (buttonDTR.isSelected()) {
                            send(true, false);
                        }
                        new Thread(() -> {
                            buttonRTS.setDisable(false);
                            buttonDTR.setDisable(false);
                            buttonOFF.setDisable(false);
                            comboBox.setDisable(false);
                        });
                    }
                } catch (SerialPortException ex) {
                    ex.getStackTrace();
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

        VBox vBox = new VBox(5, inputLabel, inputTextArea, outputLabel, outputTextArea,
                debugAndControlLabel, debugTextArea, hBox);
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
                    receive(false, false);
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            } else if (event.isRXCHAR() && event.getEventValue() > 0 && semaphore.availablePermits() == 1) {
                try {
                    receive(false, true);
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
                    if (serialPort.isOpened() && buttonRTS.isSelected() && serialPort.isCTS() && semaphore.availablePermits() == 1) {
                        receive(true, true);
                    } else if (serialPort.isOpened() && buttonDTR.isSelected() && serialPort.isDSR() && semaphore.availablePermits() == 1) {
                        receive(true, false);
                    }
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}