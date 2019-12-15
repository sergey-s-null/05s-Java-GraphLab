package sample.dialogs;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sample.GraphTab;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class SelectGraphDialog extends Stage {
    private SelectGraphController controller;
    private GraphTab result = null;

    public SelectGraphDialog() {
        try {
            initModality(Modality.APPLICATION_MODAL);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SelectGraph.fxml"));
            Parent root = loader.load();
            setScene(new Scene(root));
            setMinWidth(200);
            setMinHeight(156);
            setMaxHeight(156);

            controller = loader.getController();
            controller.init(this::onResult, this::onCanceled);
        }
        catch (IOException e) {
            System.out.println("Error while loading select graph dialog fxml file.");
            System.exit(-1);
        }
    }

    private void onResult(GraphTab tab) {
        result = tab;
        close();
    }

    private void onCanceled() {
        result = null;
        close();
    }

    public Optional<GraphTab> select(List<GraphTab> tabs) {
        result = null;
        controller.setGraphTabs(tabs);
        showAndWait();
        return Optional.ofNullable(result);
    }
}
