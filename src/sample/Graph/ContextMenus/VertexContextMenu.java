package sample.Graph.ContextMenus;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;

public class VertexContextMenu extends ContextMenu {
    public enum Action {
        MakeLoop,
        Delete
    }

    private GraphGroup graphGroup;
    private Vertex vertex = null;

    private void initMenuItems() {
        getItems().add(new MenuItem("Петля"));
        getItems().add(new SeparatorMenuItem());
        getItems().add(new MenuItem("Удалить"));

        getItems().get(0).setOnAction(this::onActionMakeLoop);
        getItems().get(2).setOnAction(this::onActionDelete);
    }

    public VertexContextMenu(GraphGroup owner) {
        super();
        graphGroup = owner;
        initMenuItems();
    }

    public void configureFor(Vertex vertex) {
        this.vertex = vertex;
    }

    private void onActionMakeLoop(ActionEvent event) {
        if (vertex != null)
            graphGroup.onVertexContextMenuAction(vertex, Action.MakeLoop);
    }

    private void onActionDelete(ActionEvent event) {
        if (vertex != null)
            graphGroup.onVertexContextMenuAction(vertex, Action.Delete);
    }
}
