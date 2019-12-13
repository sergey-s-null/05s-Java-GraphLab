package sample.tasksControllers;

import javafx.scene.Parent;
import javafx.stage.FileChooser;
import sample.Graph.GraphGroup;
import sample.MatrixView.MatrixView;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

abstract public class TaskController {
    protected static Function<TaskController, Boolean> startIfCan;
    protected static Runnable end;
    protected static Supplier<Optional<GraphGroup> > currentGraph;
    protected static Supplier<Optional<MatrixView> > currentMatrixView;

    public static void initOnStart(Function<TaskController, Boolean> startIfCan) {
        TaskController.startIfCan = startIfCan;
    }

    public static void initOnEnd(Runnable end) {
        TaskController.end = end;
    }

    public static void initGraphReceiver(Supplier<Optional<GraphGroup> > currentGraph) {
        TaskController.currentGraph = currentGraph;
    }

    public static void initMatrixViewReceiver(Supplier<Optional<MatrixView> > currentMatrixView) {
        TaskController.currentMatrixView = currentMatrixView;
    }


    protected FileChooser fileChooser = new FileChooser();

    protected TaskController() {
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Текстовый файл", "*.txt"));
    }

    abstract public Parent getRoot();

}
