package sample.Graph.GraphActions;

import java.util.ArrayList;
import java.util.List;

public class ListOfActions extends Action {
    private List<Action> actions = new ArrayList<>();

    @Override
    public void undo() {
        for (int i = actions.size() - 1; i >= 0; --i)
            actions.get(i).undo();
    }

    @Override
    public void redo() {
        for (Action action : actions)
            action.redo();
    }
}
