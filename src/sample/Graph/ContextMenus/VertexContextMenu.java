package sample.Graph.ContextMenus;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;

public class VertexContextMenu extends ContextMenu {
    public enum Action {
        Rename,
        MakeLoop,
        Delete
    }

    private GraphGroup graphGroup;
    private Vertex vertex = null;

    public VertexContextMenu(GraphGroup owner) {
        super();
        graphGroup = owner;
        initMenuItems();
    }

    private void initMenuItems() {
        MenuItem rename = new MenuItem("Переименовать"),
                loop = new MenuItem("Петля"),
                delete = new MenuItem("Удалить");

        getItems().add(rename);
        getItems().add(loop);
        getItems().add(new SeparatorMenuItem());
        getItems().add(delete);

        loop.setOnAction(this::onActionMakeLoop);
        rename.setOnAction(this::onActionRename);
        delete.setOnAction(this::onActionDelete);
    }

    public void configureFor(Vertex vertex) {
        this.vertex = vertex;
    }

    //------------|
    //   events   |
    //------------|
    private void onActionRename(ActionEvent ignored) {
        if (vertex != null)
            graphGroup.onVertexContextMenuAction(vertex, Action.Rename);
    }

    private void onActionMakeLoop(ActionEvent ignored) {
        if (vertex != null)
            graphGroup.onVertexContextMenuAction(vertex, Action.MakeLoop);
    }

    private void onActionDelete(ActionEvent ignored) {
        if (vertex != null)
            graphGroup.onVertexContextMenuAction(vertex, Action.Delete);
    }
}
