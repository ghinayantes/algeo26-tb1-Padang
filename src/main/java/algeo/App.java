package algeo;

import algeo.modules.ModuleContoh;

public class App {
    public static void main(String[] args) {
        ModuleContoh module = new ModuleContoh();
        module.jalankan();
    }
}

// Contoh entry point JavaFX
// import javafx.application.Application;
// import javafx.scene.Scene;
// import javafx.scene.control.Label;
// import javafx.stage.Stage;
//
// public class App extends Application {
//     @Override
//     public void start(Stage stage) {
//         Scene scene = new Scene(new Label("Tugas Besar 1 Algeo"), 400, 240);
//         stage.setTitle("Matrix Calculator");
//         stage.setScene(scene);
//         stage.show();
//     }
//
//     public static void main(String[] args) {
//         launch(args);
//     }
// }