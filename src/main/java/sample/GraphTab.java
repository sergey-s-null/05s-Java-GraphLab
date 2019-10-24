package sample;

import javafx.event.ActionEvent;
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
    private static int tabsCounter = 1;

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
        MenuItem close = new MenuItem("Закрыть");
        rename.setOnAction(this::onRenameTab);
        close.setOnAction(this::onCloseTab);

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
            controller.init();
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
        System.out.println("rename");//TODO
//        String text = inputDialog.getTabText(tab.getText());
//        if (text != null)
//            tab.setText(text);
    }

    private void onCloseTab(ActionEvent event) {
        System.out.println("close");//TODO
    }

}
