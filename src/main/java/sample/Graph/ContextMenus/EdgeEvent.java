package sample.Graph.ContextMenus;

import javafx.event.Event;
import javafx.event.EventType;
import sample.Graph.Elements.Edge;

public class EdgeEvent extends Event {
    private Edge edge;
    private EdgeContextMenu.Action action;

    EdgeEvent(Edge edge, EdgeContextMenu.Action action) {
        super(EventType.ROOT);
        this.edge = edge;
        this.action = action;
    }

    public Edge getEdge() {
        return edge;
    }

    public EdgeContextMenu.Action getAction() {
        return action;
    }
}
