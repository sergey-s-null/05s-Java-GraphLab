package sample.Graph.EventsContainers;

import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;

public class SelectBugsEvents extends MouseEvents {
    public SelectBugsEvents(GraphGroup graphGroup) {
        onLeftClickVertex = event ->
            graphGroup.toggleBug((Vertex) event.getSource());
    }
}
