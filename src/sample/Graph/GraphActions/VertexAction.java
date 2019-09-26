package sample.Graph.GraphActions;

import sample.Graph.Elements.Vertex;

abstract public class VertexAction extends Action {
    protected Vertex vertex;

    protected VertexAction(Vertex vertex) {
        this.vertex = vertex;
    }
}
