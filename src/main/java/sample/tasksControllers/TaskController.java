package sample.tasksControllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import sample.Graph.GraphGroup;
import sample.MatrixView.MatrixView;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

abstract public class TaskController {
    protected Set<Consumer<TaskController> > startListeners = new HashSet<>();
    private Set<Consumer<TaskController> > endListeners = new HashSet<>();
    private boolean isBusy = false;


    final public void addStartListener(Consumer<TaskController> consumer) {
        startListeners.add(consumer);
    }

    final public void addEndListener(Consumer<TaskController> consumer) {
        endListeners.add(consumer);
    }

    final public boolean isBusy() {
        return isBusy;
    }

    abstract public Parent getRoot();

    abstract public boolean validateGraph(GraphGroup graphGroup);

    @FXML private void onStart() {
        if (isBusy())
            return;
        for (Consumer<TaskController> startConsumer : startListeners)
            startConsumer.accept(this);
    }

    public void start(GraphGroup graphGroup, MatrixView matrixView) {
        isBusy = true;
    }

    final void end() {
        isBusy = false;
        for (Consumer<TaskController> endConsumer : endListeners)
            endConsumer.accept(this);
    }
}
