package sample.Graph.GraphActions;

import sample.Graph.GraphGroup;
import sample.Parser.Resolution;


public class ChangeResolution extends Action {
    private GraphGroup graphGroup;
    private Resolution oldResolution, newResolution;

    public ChangeResolution(GraphGroup graphGroup, Resolution oldResolution,
                            Resolution newResolution)
    {
        this.graphGroup = graphGroup;
        this.oldResolution = oldResolution;
        this.newResolution = newResolution;
    }

    @Override
    public void undo() {
        graphGroup.setResolution(oldResolution, false);
    }

    @Override
    public void redo() {
        graphGroup.setResolution(newResolution, false);
    }
}
