package sample.Graph.EventsContainers;

import sample.Graph.ContextMenus.EdgeContextMenu;
import sample.Graph.ContextMenus.VertexContextMenu;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;

public class ContextMenusEvents extends MouseEvents {
    protected Vertex selected, movingVertex;
    protected Edge movingEdge;

    protected ContextMenusEvents(GraphGroup graphGroup) {
        super();
        onRightClickVertex = (event) -> {
            if (selected != null || movingVertex != null || movingEdge != null)
                return;
            VertexContextMenu contextMenu = graphGroup.getVertexContextMenu();
            contextMenu.configureFor((Vertex) event.getSource());
            contextMenu.show((Vertex) event.getSource(), event.getScreenX(), event.getScreenY());
        };
        onRightClickEdge = (event) -> {
            if (selected != null || movingVertex != null || movingEdge != null)
                return;
            EdgeContextMenu contextMenu = graphGroup.getEdgeContextMenu();
            contextMenu.configureFor((Edge) event.getSource());
            contextMenu.show((Edge) event.getSource(), event.getScreenX(), event.getScreenY());
        };
    }

    @Override
    public void deactivate() {
        if (selected != null)
            selected.setSelected(false);
        selected = null;
        movingVertex = null;
        movingEdge = null;
    }
}
