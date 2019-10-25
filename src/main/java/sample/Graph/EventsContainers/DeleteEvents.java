package sample.Graph.EventsContainers;

import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;

public class DeleteEvents extends ContextMenusEvents {
    public DeleteEvents(GraphGroup graphGroup) {
        super(graphGroup);
        onLeftClickVertex = (event) ->
                graphGroup.removeVertexWithEdges((Vertex) event.getSource(), true);
        onLeftClickEdge = (event) ->
                graphGroup.removeEdge((Edge) event.getSource(), true);
    }
}
