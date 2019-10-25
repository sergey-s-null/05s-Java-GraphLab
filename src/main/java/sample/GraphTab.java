package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import sample.Graph.GraphGroup;
import sample.MatrixView.MatrixView;
import sample.Parser.GraphData;

import java.io.IOException;

public class GraphTab extends Tab {
    private MenuItem close;
    private GraphGroup graphGroup;
    private MatrixView matrixView;

    public GraphTab(String tabText, GraphData data) {
        this();
        setText(tabText);
        graphGroup.setGraph(data, false);
    }

    public GraphTab() {
        super();
        setText("Новый таб " + tabsCounter++);
        initContextMenu();
        initContent();
    }

    private void initContextMenu() {
        MenuItem rename = new MenuItem("Переименовать");
        close = new MenuItem("Закрыть");
        rename.setOnAction(this::onRenameTab);

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(
                rename,
                new SeparatorMenuItem(),
                close
        );
        setContextMenu(contextMenu);
    }

    private void initContent() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/graph.fxml"));
        try {
            Parent root = loader.load();
            setContent(root);
            GraphController controller = loader.getController();
            graphGroup = controller.getGraphGroup();
            matrixView = controller.getMatrixView();
        }
        catch (IOException e) {
            GraphAlert.showAndWait("Error loading graph resource.");
            System.exit(-1);
        }
    }

    public GraphGroup getGraphGroup() {
        return graphGroup;
    }

    public MatrixView getMatrixView() {
        return matrixView;
    }

    private void onRenameTab(ActionEvent event) {
        String text = InputDialogs.getTabText(getText());
        if (text != null)
            setText(text);
    }

    public void setOnCloseAction(EventHandler<ActionEvent> handler) {
        close.setOnAction(handler);
    }

    //-----------------------|
    //   static components   |
    //-----------------------|
    public static boolean isValidTabText(String tabText) {
        return tabText.length() > 0 && tabText.length() <= 16;
    }

    public static String makeValidTabText(String tabText) {
        if (tabText.length() > 16)
            return tabText.substring(0, Math.min(16, tabText.length())) + "...";
        else
            return tabText;
    }

    private static int tabsCounter = 1;

}
