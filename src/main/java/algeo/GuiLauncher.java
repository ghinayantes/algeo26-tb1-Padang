package algeo;

import javafx.application.Application;

/** Entry point for the shaded JavaFX JAR. */
public final class GuiLauncher {

    private GuiLauncher() {
    }

    public static void main(String[] args) {
        Application.launch(GuiApp.class, args);
    }
}
