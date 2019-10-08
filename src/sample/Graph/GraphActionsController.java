package sample.Graph;

import sample.Graph.GraphActions.Action;

import java.util.ArrayDeque;
import java.util.Deque;

// TODO not static
public class GraphActionsController {
    private Deque<Action> doneActions = new ArrayDeque<>(),
                          nextActions = new ArrayDeque<>();

    public void addAction(Action action) {
        nextActions.clear();
        doneActions.addLast(action);
    }

    public void undo() {
        if (!canUndo())
            return;
        Action action = doneActions.removeLast();
        action.undo();
        nextActions.addLast(action);
    }

    public void redo() {
        if (!canRedo())
            return;
        Action action = nextActions.removeLast();
        action.redo();
        doneActions.addLast(action);
    }

    private boolean canUndo() {
        return doneActions.size() > 0;
    }

    private boolean canRedo() {
        return nextActions.size() > 0;
    }



}
