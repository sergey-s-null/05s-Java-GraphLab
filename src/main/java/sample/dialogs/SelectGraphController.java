package sample.dialogs;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import sample.GraphTab;

import java.util.List;
import java.util.function.Consumer;

public class SelectGraphController {
    @FXML private ChoiceBox<GraphTab> graphChoiceBox;
    private Consumer<GraphTab> onSelected;
    private Runnable onCanceled;

    public void init(Consumer<GraphTab> onSelected, Runnable onCanceled) {
        this.onSelected = onSelected;
        this.onCanceled = onCanceled;
    }

    public void setGraphTabs(List<GraphTab> tabs) {
        graphChoiceBox.getItems().clear();
        graphChoiceBox.getItems().addAll(tabs);
    }

    @FXML private void onOk() {
        GraphTab tab = graphChoiceBox.getSelectionModel().getSelectedItem();
        if (tab != null) onSelected.accept(tab);
    }

    @FXML private void onCancel() {
        onCanceled.run();
    }
}
