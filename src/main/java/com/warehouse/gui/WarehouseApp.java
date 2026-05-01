package com.warehouse.gui;

import com.warehouse.gui.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class WarehouseApp extends Application {
    public static void launchApp(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(WarehouseApp.class.getResource("/com/warehouse/gui/main.fxml"));
        Parent root = loader.load();

                    Scene scene = new Scene(root, 1820, 920);
        URL stylesheet = WarehouseApp.class.getResource("/com/warehouse/gui/style.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }

        stage.setTitle("Warehouse Management System");
                    stage.setMinWidth(1720);
        stage.setMinHeight(850);
        stage.setScene(scene);
        MainController controller = loader.getController();
            stage.setOnCloseRequest(event -> {
              if (event != null) {
                controller.shutdown();
              }
            });
        stage.show();
    }
}
