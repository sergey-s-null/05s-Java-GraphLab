package sample.Graph.GraphActions;

import sample.Graph.Elements.Vertex;
import sample.Graph.GraphActionsController;


public class RenameVertex extends VertexAction {
    public static void create(Vertex vertex, String oldName, String newName) {
        GraphActionsController.addAction(new RenameVertex(vertex, oldName, newName));
    }

    private String oldName, newName;

    private RenameVertex(Vertex vertex, String oldName, String newName) {
        super(vertex);

        this.oldName = oldName;
        this.newName = newName;

    }

    @Override
    public void undo() {
        vertex.setName(oldName);
    }

    @Override
    public void redo() {
        vertex.setName(newName);
    }
}
