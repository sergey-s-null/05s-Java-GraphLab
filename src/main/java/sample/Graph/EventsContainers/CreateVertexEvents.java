package sample.Graph.EventsContainers;

import sample.Graph.GraphGroup;

public class CreateVertexEvents extends ContextMenusEvents {
    public CreateVertexEvents(GraphGroup graphGroup) {
        super(graphGroup);
        onGraphGroupClick = (event) ->
                graphGroup.addVertex(event.getX(), event.getY(), true);
    }
}
