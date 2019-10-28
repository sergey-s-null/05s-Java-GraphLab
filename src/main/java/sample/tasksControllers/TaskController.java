package sample.tasksControllers;

import javafx.scene.Parent;
import sample.Graph.GraphGroup;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

abstract public class TaskController {
    protected Set<Consumer<TaskController> > startListeners = new HashSet<>();
    private Set<Consumer<TaskController> > endListeners = new HashSet<>();
    private boolean isBusy = false;

    abstract public Parent getRoot();

    public void addStartListener(Consumer<TaskController> consumer) {
        startListeners.add(consumer);
    }

    public void addEndListener(Consumer<TaskController> consumer) {
        endListeners.add(consumer);
    }

    public boolean isBusy() {
        return isBusy;
    }

    abstract public boolean validateGraph(GraphGroup graphGroup);

    public void start(GraphGroup graphGroup) {
        isBusy = true;
    }

    protected void end() {
        for (Consumer<TaskController> endConsumer : endListeners)
            endConsumer.accept(this);
        isBusy = false;
    }
}
