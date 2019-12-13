package sample.Graph.EventsContainers;

import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;

public class SelectSpiderEvents extends MouseEvents {
    public SelectSpiderEvents(GraphGroup graphGroup) {
        onLeftClickVertex = event ->
            graphGroup.setSpider((Vertex) event.getSource());
    }
}
