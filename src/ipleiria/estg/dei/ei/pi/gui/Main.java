package ipleiria.estg.dei.ei.pi.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mainFrame.fxml"));
        Parent root = fxmlLoader.load();

        MainFrameController mainFrameController = fxmlLoader.getController();

        primaryStage.setTitle("Picking");
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(788);
        primaryStage.setScene(new Scene(root, bounds.getWidth()*0.75, bounds.getHeight()*0.75));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
