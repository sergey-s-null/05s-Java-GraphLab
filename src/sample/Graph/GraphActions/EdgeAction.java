package sample.Graph.GraphActions;

import sample.Graph.Elements.Edge;

abstract public class EdgeAction extends Action {
    protected Edge edge;

    protected EdgeAction(Edge edge) {
        this.edge = edge;
    }

}
