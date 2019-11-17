import javafx.application.Application;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        Monitor monitor = new Monitor(stage);
        Station station1 = new Station(1, monitor.getStartButton());
        Station station2 = new Station(2, monitor.getStartButton());
        Station station3 = new Station(3, monitor.getStartButton());
        stage.setOnCloseRequest(e -> closeAll(e, stage, station1.getStage(), station2.getStage(), station3.getStage()));
        station1.getStage().setOnCloseRequest(e -> closeAll(e, stage, station1.getStage(), station2.getStage(), station3.getStage()));
        station2.getStage().setOnCloseRequest(e -> closeAll(e, stage, station1.getStage(), station2.getStage(), station3.getStage()));
        station3.getStage().setOnCloseRequest(e -> closeAll(e, stage, station1.getStage(), station2.getStage(), station3.getStage()));

        final Timer timer = new Timer(true);

        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (monitor.getStartButton().isDisable()) {

                    Thread thread = new Thread(() -> {
                        byte[] channelMonitorStation1;
                        byte[] channelStation1Station2;
                        byte[] channelStation2Station3;
                        byte[] channelStation3Monitor = new byte[6];

                        while (true) {
                            channelMonitorStation1 = handleMonitor(channelStation3Monitor, monitor.getDebugTextArea());

                            channelStation1Station2 = handleStation(channelMonitorStation1, station1, monitor.getComboBox());

                            channelStation2Station3 = handleStation(channelStation1Station2, station2, monitor.getComboBox());

                            channelStation3Monitor = handleStation(channelStation2Station3, station3, monitor.getComboBox());
                        }
                    });
                    thread.setDaemon(true);
                    thread.start();
                    timer.cancel();
                    timer.purge();
                }
            }
        };

        timer.schedule(task, 0, 100);
    }

    private byte[] handleMonitor(byte[] out, TextArea textArea) {
        textArea.setText("*");
        if(out[0] == "F".getBytes()[0] && out[4] == "M".getBytes()[0]){
            textArea.appendText("\nDelete");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        textArea.clear();

        byte[] in = new byte[6];
        if (out[0] == "T".getBytes()[0]) {           //if Token
            in = out;
        } else if (out[0] == "F".getBytes()[0]) {    //if Frame
            if (out[4] == "M".getBytes()[0]) {       // if Frame and monitoring(against looping)
                in[0] = "T".getBytes()[0];
            } else {
                in = out;
                in[4] = "M".getBytes()[0];           //mark packet monitoring
            }
        } else if (out[0] == 0) {                     //if first time
            in[0] = "T".getBytes()[0];
        }
        return in;
    }

    private byte[] handleStation(byte[] out, Station station, ComboBox<Integer> holdTime) {

        byte[] in = new byte[6];
        if (out[0] == "T".getBytes()[0]) {                // if Token
            if (station.getLine() != null) {           //source address = destination address
                station.getOutputTextArea().appendText(station.getLine());
                station.setLine(null);
            }
            if (!station.getList().isEmpty()) {            //if station want to send
                in = station.getList().get(0);
                station.getList().remove(0);
                station.setTime(new Date().getTime());
            } else {                                      //if station doesn't want anything
                in = out;
            }
        } else {                                             //if Frame
            if (out[2] == station.getSourceAddress()) {      // if destination = source
                in = out;
                in[3] = "C".getBytes()[0];
                try {
                    station.getOutputTextArea().appendText(new String(new byte[]{out[5]}, "windows-1251"));
                } catch (UnsupportedEncodingException ignored) {
                }
            } else if (out[1] == station.getSourceAddress()) {   //if packet went round
                if (out[3] == "C".getBytes()[0]) {                //check packet was received
                    if (station.getLine() != null) {           //source address = destination address
                        station.getOutputTextArea().appendText(station.getLine());
                        station.setLine(null);
                    }
                    if (!station.getList().isEmpty()) {                                         //if station want to send and can
                        if (new Date().getTime() - station.getTime() < holdTime.getValue() * 1000) {
                            in = station.getList().get(0);
                            station.getList().remove(0);
                        } else {
                            station.setTime(0);
                            in[0] = "T".getBytes()[0];
                        }
                    } else {
                        station.setTime(0);
                        in[0] = "T".getBytes()[0];                //set Token
                    }
                } else {
                    //System.out.println("Problem with Control");  //значит что то криво
                    in = out;
                }
            } else {
                in = out;
            }
        }

        station.getDebugTextArea().setText("*");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        station.getDebugTextArea().clear();

        return in;
    }

    private void closeAll(WindowEvent event, Stage stage, Stage stage1, Stage stage2, Stage stage3) {
        event.consume();
        stage.close();
        stage1.close();
        stage2.close();
        stage3.close();
    }
}