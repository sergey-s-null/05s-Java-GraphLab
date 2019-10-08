package sample.Graph.GraphActions;

import sample.Graph.Elements.Vertex;


public class RenameVertex extends VertexAction {
    private String oldName, newName;

    public RenameVertex(Vertex vertex, String oldName, String newName) {
        super(vertex);

        this.oldName = oldName;
        this.newName = newName;

    }

    @Override
    public void undo() {
        vertex.setName(oldName, false);
    }

    @Override
    public void redo() {
        vertex.setName(newName, false);
    }
}
