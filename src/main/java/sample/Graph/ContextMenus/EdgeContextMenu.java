package sample.Graph.ContextMenus;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import sample.Graph.Elements.BinaryEdge;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.UnaryEdge;


public class EdgeContextMenu extends ContextMenu {
    public enum Action {
        ChangeWeight,
        SelectBothDirection,
        SelectFirstDirection,
        SelectSecondDirection,
        Delete
    }

    private Edge edge = null;
    private RadioMenuItem radioBothDirection, radioFirstDirection, radioSecondDirection;
    private EventHandler<EdgeEvent> handler = null;

    public EdgeContextMenu(EventHandler<EdgeEvent> handler) {
        this();
        this.handler = handler;
    }

    private EdgeContextMenu() {
        super();
        initMenuItems();
    }

    private void initMenuItems() {
        MenuItem changeWeight = new MenuItem("Изменить вес");
        radioBothDirection = new RadioMenuItem("В обе стороны");
        radioFirstDirection = new RadioMenuItem("В сторону v1");
        radioSecondDirection = new RadioMenuItem("В сторону v2");
        MenuItem delete = new MenuItem("Удалить");

        getItems().add(changeWeight);
        getItems().add(radioBothDirection);
        getItems().add(radioFirstDirection);
        getItems().add(radioSecondDirection);
        getItems().add(new SeparatorMenuItem());
        getItems().add(delete);

        changeWeight.setOnAction(this::onActionChangeWeight);
        radioBothDirection.setOnAction(this::onActionBothDirection);
        radioFirstDirection.setOnAction(this::onActionFirstDirection);
        radioSecondDirection.setOnAction(this::onActionSecondDirection);
        delete.setOnAction(this::onActionDelete);

        ToggleGroup tg = new ToggleGroup();
        radioBothDirection.setToggleGroup(tg);
        radioFirstDirection.setToggleGroup(tg);
        radioSecondDirection.setToggleGroup(tg);
    }

    public void configureFor(Edge edge) {
        this.edge = edge;

        if (this.edge.getClass() == UnaryEdge.class) {
            setDirectionItemsVisible(false);
        }
        else if (this.edge.getClass() == BinaryEdge.class) {
            setDirectionItemsVisible(true);

            switch (edge.getDirection()) {
                case Both:
                    radioBothDirection.setSelected(true);
                    break;
                case FirstVertex:
                    radioFirstDirection.setSelected(true);
                    break;
                case SecondVertex:
                    radioSecondDirection.setSelected(true);
                    break;
            }

            BinaryEdge binaryEdge = (BinaryEdge) this.edge;
            radioFirstDirection.setText("В сторону \"" + binaryEdge.getFirstVertex().getName() + "\"");
            radioSecondDirection.setText("В сторону \"" + binaryEdge.getSecondVertex().getName() + "\"");
        }
    }

    private void setDirectionItemsVisible(boolean flag) {
        radioBothDirection.setVisible(flag);
        radioFirstDirection.setVisible(flag);
        radioSecondDirection.setVisible(flag);
    }

    //------------|
    //   events   |
    //------------|
    private void onActionChangeWeight(ActionEvent ignored) {
        handleOrIgnore(Action.ChangeWeight);
    }

    private void onActionBothDirection(ActionEvent ignored) {
        handleOrIgnore(Action.SelectBothDirection);
    }

    private void onActionFirstDirection(ActionEvent ignored) {
        handleOrIgnore(Action.SelectFirstDirection);
    }

    private void onActionSecondDirection(ActionEvent ignored) {
        handleOrIgnore(Action.SelectSecondDirection);
    }

    private void onActionDelete(ActionEvent ignored) {
        handleOrIgnore(Action.Delete);
    }

    private void handleOrIgnore(Action action) {
        if (edge != null && handler != null)
            handler.handle(new EdgeEvent(edge, action));
    }

}
