package sample.Graph.ContextMenus;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

    private Vertex vertex = null;
    private EventHandler<VertexEvent> handler = null;

    public VertexContextMenu(EventHandler<VertexEvent> handler) {
        this();
        this.handler = handler;
    }

    private VertexContextMenu() {
        super();
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
    public void setOnItemAction(EventHandler<VertexEvent> handler) {
        this.handler = handler;
    }

    private void onActionRename(ActionEvent ignored) {
        handleOrIgnore(Action.Rename);
    }

    private void onActionMakeLoop(ActionEvent ignored) {
        handleOrIgnore(Action.MakeLoop);
    }

    private void onActionDelete(ActionEvent ignored) {
        handleOrIgnore(Action.Delete);
    }

    private void handleOrIgnore(Action action) {
        if (vertex != null && handler != null)
            handler.handle(new VertexEvent(vertex, action));
    }
}
