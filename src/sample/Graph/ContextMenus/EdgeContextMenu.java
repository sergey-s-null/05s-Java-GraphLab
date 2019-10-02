package sample.Graph.ContextMenus;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import sample.Graph.Elements.BinaryEdge;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.UnaryEdge;
import sample.Graph.GraphGroup;



public class EdgeContextMenu extends ContextMenu {
    public enum Action {
        ChangeWeight,
        SelectBothDirection,
        SelectFirstDirection,
        SelectSecondDirection,
        Delete
    }

    private GraphGroup graphGroup;
    private Edge edge = null;
    private RadioMenuItem radioBothDirection, radioFirstDirection, radioSecondDirection;

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

    public EdgeContextMenu(GraphGroup owner) {
        super();
        graphGroup = owner;
        initMenuItems();
    }

    private void setDirectionItemsVisible(boolean flag) {
        radioBothDirection.setVisible(flag);
        radioFirstDirection.setVisible(flag);
        radioSecondDirection.setVisible(flag);
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

    //------------|
    //   events   |
    //------------|
    private void onActionChangeWeight(ActionEvent ignored) {
        if (edge != null)
            graphGroup.onEdgeContextMenuAction(edge, Action.ChangeWeight);
    }

    private void onActionBothDirection(ActionEvent ignored) {
        if (edge != null)
            graphGroup.onEdgeContextMenuAction(edge, Action.SelectBothDirection);
    }

    private void onActionFirstDirection(ActionEvent ignored) {
        if (edge != null)
            graphGroup.onEdgeContextMenuAction(edge, Action.SelectFirstDirection);
    }

    private void onActionSecondDirection(ActionEvent ignored) {
        if (edge != null)
            graphGroup.onEdgeContextMenuAction(edge, Action.SelectSecondDirection);
    }

    private void onActionDelete(ActionEvent ignored) {
        if (edge != null)
            graphGroup.onEdgeContextMenuAction(edge, Action.Delete);
    }

}
