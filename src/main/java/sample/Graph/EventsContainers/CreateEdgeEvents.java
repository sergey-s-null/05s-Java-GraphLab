package sample.Graph.EventsContainers;

import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;

public class CreateEdgeEvents extends ContextMenusEvents {
    public CreateEdgeEvents(GraphGroup graphGroup) {
        super(graphGroup);
        onLeftClickVertex = (event) -> {
            if (selected == null) {
                selected = (Vertex) event.getSource();
                selected.setSelected(true);
            }
            else if (selected == event.getSource()) {
                //addNewEdge(selected);
                selected.setSelected(false);
                selected = null;
            }
            else {
                graphGroup.addEdge(selected, (Vertex) event.getSource(), true);
                selected.setSelected(false);
                selected = null;
            }
        };
    }
}
