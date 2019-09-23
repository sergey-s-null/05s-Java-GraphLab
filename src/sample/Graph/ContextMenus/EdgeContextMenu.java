package sample.Graph.ContextMenus;

import javafx.event.ActionEvent;
import javafx.scene.control.*;
import sample.Graph.Elements.BinaryEdge;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.UnaryEdge;
import sample.Graph.GraphGroup;



public class EdgeContextMenu extends ContextMenu {
    public enum Action {
        SelectBothDirection,
        SelectFirstDirection,
        SelectSecondDirection,
        Delete
    }

    private GraphGroup graphGroup;
    private Edge edge = null;

    private void initMenuItems() {
        RadioMenuItem radioBothDirection = new RadioMenuItem("В обе стороны"),
                      radioFirstDirection = new RadioMenuItem("В сторону v1"),
                      radioSecondDirection = new RadioMenuItem("В сторону v2");
        MenuItem delete = new MenuItem("Удалить");

        getItems().add(radioBothDirection);
        getItems().add(radioFirstDirection);
        getItems().add(radioSecondDirection);
        getItems().add(new SeparatorMenuItem());
        getItems().add(delete);

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
        for (int i = 0; i < 3; ++i)
            getItems().get(i).setVisible(flag);
    }

    public void configureFor(Edge edge) {
        this.edge = edge;
        if (this.edge.getClass() == UnaryEdge.class) {
            setDirectionItemsVisible(false);
        }
        else if (this.edge.getClass() == BinaryEdge.class) {
            setDirectionItemsVisible(true);
        }
    }

    private void onActionBothDirection(ActionEvent event) {
        if (edge != null)
            graphGroup.onEdgeContextMenuAction(edge, Action.SelectBothDirection);
    }

    private void onActionFirstDirection(ActionEvent event) {
        if (edge != null)
            graphGroup.onEdgeContextMenuAction(edge, Action.SelectFirstDirection);
    }

    private void onActionSecondDirection(ActionEvent event) {
        if (edge != null)
            graphGroup.onEdgeContextMenuAction(edge, Action.SelectSecondDirection);
    }

    private void onActionDelete(ActionEvent event) {
        if (edge != null)
            graphGroup.onEdgeContextMenuAction(edge, Action.Delete);
    }

}
