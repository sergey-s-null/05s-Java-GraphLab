package sample.tasksControllers;

import javafx.scene.Parent;
import javafx.stage.FileChooser;
import sample.Graph.GraphGroup;
import sample.GraphTab;
import sample.MatrixView.MatrixView;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

abstract public class TaskController {
    static Function<TaskController, Boolean> startIfCan;
    static Runnable end;
    static Supplier<Optional<GraphTab> > currentGraphTab;
    static Supplier<Optional<GraphGroup> > currentGraph;
    static Supplier<Optional<MatrixView> > currentMatrixView;
    static Supplier<List<GraphTab> > allGraphTabs;
    static Supplier<Optional<GraphTab> > selectGraph;
    static Supplier<GraphTab> createNewGraph;

    public static void initOnStart(Function<TaskController, Boolean> startIfCan) {
        TaskController.startIfCan = startIfCan;
    }

    public static void initOnEnd(Runnable end) {
        TaskController.end = end;
    }

    public static void initGraphTabReceiver(Supplier<Optional<GraphTab> > currentGraphTab) {
        TaskController.currentGraphTab = currentGraphTab;
    }

    public static void initGraphReceiver(Supplier<Optional<GraphGroup> > currentGraph) {
        TaskController.currentGraph = currentGraph;
    }

    public static void initMatrixViewReceiver(Supplier<Optional<MatrixView> > currentMatrixView) {
        TaskController.currentMatrixView = currentMatrixView;
    }

    public static void initAllGraphTabsReceiver(Supplier<List<GraphTab> > allGraphTabs) {
        TaskController.allGraphTabs = allGraphTabs;
    }

    public static void initSelectGraph(Supplier<Optional<GraphTab> > selectGraph) {
        TaskController.selectGraph = selectGraph;
    }

    public static void initNewGraphCreator(Supplier<GraphTab> createNewGraph) {
        TaskController.createNewGraph = createNewGraph;
    }


    protected FileChooser fileChooser = new FileChooser();

    protected TaskController() {
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Текстовый файл", "*.txt"));
    }

    abstract public Parent getRoot();

}
