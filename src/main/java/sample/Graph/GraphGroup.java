package sample.Graph;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import sample.Graph.ContextMenus.EdgeContextMenu;
import sample.Graph.ContextMenus.EdgeEvent;
import sample.Graph.ContextMenus.VertexContextMenu;
import sample.Graph.ContextMenus.VertexEvent;
import sample.Graph.Elements.*;
import sample.Graph.EventsContainers.*;
import sample.Graph.GraphActions.*;
import sample.dialogs.InputDialogs;
import sample.Parser.SimpleData.EdgeData;
import sample.Parser.GraphData;
import sample.Parser.SimpleData.Resolution;
import sample.Parser.SimpleData.VertexData;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


public class GraphGroup extends Group {
    public enum Action {
        Empty,
        CreateVertex,
        CreateEdge,
        Delete,
        Move,
        SelectTwoVertices,
        SelectSpider,
        SelectBug
    }

    public static final double minWidth = 200, minHeight = 200, maxWidth = 5000, maxHeight = 5000;
    public static final double defaultWidth = 400, defaultHeight = 400;
    private Action currentAction = Action.Empty;
    private Rectangle backgroundRect = new Rectangle(),
                      clipRect = new Rectangle();
    private DoubleProperty width, height;
    private boolean needToSave = false;

    private VertexContextMenu vertexContextMenu = new VertexContextMenu(this::onVertexContextMenuAction);
    private EdgeContextMenu edgeContextMenu = new EdgeContextMenu(this::onEdgeContextMenuAction);

    private ObservableList<Vertex> vertices = FXCollections.observableArrayList();
    private ObservableSet<Edge> edges = FXCollections.observableSet();
    private GraphPath currentPath = null;
    // Spider and bugs
    private Vertex spiderVertex = null;
    private Spider spider = new Spider();
    private Map<Vertex, Bug> bugs = new HashMap<>();

    private GraphActionsController actionsController = new GraphActionsController();

    // constructor
    public GraphGroup() {
        super();

        initRects();
        getChildren().addAll(backgroundRect);
        setClip(clipRect);

        setOnMouseClicked(this::onMouseClick);
        setOnMouseDragged(this::onMouseDrag);
        setOnMouseReleased(this::onMouseRelease);

    }

    // init
    private void initRects() {
        backgroundRect.setWidth(defaultWidth);
        backgroundRect.setHeight(defaultHeight);
        backgroundRect.setFill(Color.WHITE);
        backgroundRect.setStrokeType(StrokeType.INSIDE);
        backgroundRect.setStroke(Color.BLACK);
        backgroundRect.setStrokeWidth(1);

        clipRect.setWidth(defaultWidth);
        clipRect.setHeight(defaultHeight);
        width = clipRect.widthProperty();
        height = clipRect.heightProperty();
    }

    //
    public void setCurrentAction(Action currentAction) {
        this.currentAction = currentAction;
        configureEventsFor(currentAction);
    }

    public Action getCurrentAction() {
        return currentAction;
    }

    public ObservableList<Vertex> getVertices() {
        return vertices;
    }

    public int getVerticesCount() {
         return vertices.size();
    }

    public ObservableSet<Edge> getEdges() {
        return edges;
    }

    public boolean isAllEdgesWeightsUnit() {
        for (Edge edge : edges)
            if (edge.getWeight() != 1) return false;
        return true;
    }

    public boolean isAllEdgesWeightsPositive() {
        for (Edge edge : edges)
            if (edge.getWeight() <= 0) return false;
        return true;
    }

    public boolean isGraphOriented() {
        for (Edge edge : edges)
            if (edge.getDirection() != Edge.Direction.Both) return true;
        return false;
    }

    public boolean isMultiGraph() {
        Set<VerticesPair> pairs = new HashSet<>();
        for (Edge edge : edges) {
            VerticesPair pair = new VerticesPair(edge.getFirstVertex(), edge.getSecondVertex());
            if (pairs.contains(pair)) return true;
            pairs.add(pair);
        }
        return false;
    }

    private ListOfActions getVerticesActionsForNewResolution(Resolution resolution) {
        ListOfActions listOfActions = new ListOfActions();
        for (Vertex vertex : vertices) {
            sample.Graph.GraphActions.Action vertexAction =
                    vertex.getActionForNewResolution(resolution);
            if (vertexAction != null)
                listOfActions.add(vertexAction);
        }
        return listOfActions;
    }

    private ListOfActions getChangeResolutionActionWithVerticesRedo(Resolution newResolution) {
        ListOfActions verticesActions =
                getVerticesActionsForNewResolution(newResolution);
        verticesActions.redo();

        ListOfActions listOfActions = new ListOfActions();
        listOfActions.add(verticesActions);
        listOfActions.add(new ChangeResolution(this, getResolution(),
                newResolution));

        return listOfActions;
    }

    public WritableImage getGraphImage() {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setDepthBuffer(true);

        return snapshot(parameters, null);
    }

    public boolean isNeedToSave() {
        return needToSave && vertices.size() > 0;
    }

    public boolean isEmpty() {
        return vertices.size() == 0;
    }

    public VertexContextMenu getVertexContextMenu() {
        return vertexContextMenu;
    }

    public EdgeContextMenu getEdgeContextMenu() {
        return edgeContextMenu;
    }

    public void setElementsPath(GraphPath path) {
        clearCurrentPath();
        currentPath = path;
        currentPath.setSelectedAsPath(true);
    }

    public void clearCurrentPath() {
        if (currentPath != null) {
            currentPath.setSelectedAsPath(false);
            currentPath = null;
        }
    }

    // Spider and bugs
    public List<Vertex> getVerticesByName(String name) {
        return vertices.stream().filter(vertex ->
                vertex.nameProperty().get().equals(name)).collect(Collectors.toList());
    }

    public void setSpider(Vertex vertex) {
        if (bugs.containsKey(vertex)) toggleBug(vertex);

        clearSpider();
        spiderVertex = vertex;
        spider.connect(spiderVertex);
        spiderVertex.getChildren().add(spider);
    }

    public Spider getSpider() {
        return spider;
    }

    public void clearSpider() {
        if (spiderVertex != null) {
            spider.disconnect();
            spiderVertex.getChildren().remove(spider);
            spiderVertex = null;
        }
    }

    public void toggleBug(Vertex vertex) {
        if (spiderVertex == vertex) clearSpider();

        Bug bug = bugs.get(vertex);
        if (bug == null) {
            bug = new Bug(vertex);
            bugs.put(vertex, bug);
            vertex.getChildren().add(bug);
        }
        else {
            bug.disconnect();
            bugs.remove(vertex);
            vertex.getChildren().remove(bug);
        }
    }

    public void clearBugs() {
        for (Map.Entry<Vertex, Bug> pair : bugs.entrySet()) {
            pair.getValue().disconnect();
            pair.getKey().getChildren().remove(pair.getValue());
        }
        bugs.clear();
    }

    public Optional<Vertex> getSpiderVertex() {
        return Optional.ofNullable(spiderVertex);
    }

    public Collection<Vertex> getBugs() {
        return bugs.keySet();
    }

    public int getBugsCount() {
        return bugs.size();
    }

    public boolean isSpider(Vertex vertex) {
        return vertex == spiderVertex;
    }

    public boolean isBug(Vertex vertex) {
        return bugs.get(vertex) != null;
    }

    // actions history
    public GraphActionsController getActionsController() {
        return actionsController;
    }

    public void undo() {
        actionsController.undo();
    }

    public void redo() {
        actionsController.redo();
    }

    // graph
    public void setGraph(GraphData data) {
        setResolution(data.getResolution(), false);


        // создание новых
        Map<String, Vertex> nameToVertex = new HashMap<>();
        for (VertexData vertexData : data.getVerticesData().get()) {
            Vertex vertex = vertexData.create(this);
            nameToVertex.put(vertex.getName(), vertex);
        }
        List<Edge> newEdges = new ArrayList<>();
        for (EdgeData edgeData : data.getEdgesData().getEdges())
            newEdges.add(edgeData.create(this, nameToVertex));

        // создание действия
//        if (createAction) {
//            ListOfActions actions = new ListOfActions();
//            for (Edge edge : edges)
//                actions.add(new DeleteEdge(edge, this));
//            for (Vertex vertex : vertices)
//                actions.add(new DeleteVertex(vertex, null, this));
//            for (Vertex vertex : nameToVertex.values())
//                actions.add(new CreateVertex(vertex, this));
//            for (Edge edge : newEdges)
//                actions.add(new CreateEdge(edge, this));
//            actions.add(getChangeResolutionActionWithVerticesRedo(data.getResolution()));
//            actionsController.addAction(actions);
//        }

        // исполнение
        clear();
        for (Vertex vertex : nameToVertex.values())
            addVertex(vertex, false);
        for (Edge edge : newEdges)
            addEdge(edge, false);

        needToSave = false;
    }

    public GraphData getGraph() {
        return GraphData.makeByGraph(vertices, edges, width.get(), height.get());
    }

    // resolution
    public void setResolution(Resolution resolution, boolean createAction) {
        needToSave = true;
        if (createAction) {
            actionsController.addAction(getChangeResolutionActionWithVerticesRedo(resolution));
        }

        setWidth(resolution.getWidth(), false);
        setHeight(resolution.getHeight(), false);
    }

    public Resolution getResolution() {
        return new Resolution(width.get(), height.get());
    }

    public void setWidth(double width, boolean createAction) {
        needToSave = true;
        if (createAction) {
            actionsController.addAction(
                    getChangeResolutionActionWithVerticesRedo(new Resolution(width, getHeight())));
        }
        backgroundRect.setWidth(width);
        clipRect.setWidth(width);
    }

    public void setHeight(double height, boolean createAction) {
        needToSave = true;
        if (createAction) {
            actionsController.addAction(
                    getChangeResolutionActionWithVerticesRedo(new Resolution(getWidth(), height)));
        }
        backgroundRect.setHeight(height);
        clipRect.setHeight(height);
    }

    public double getWidth() {
        return width.get();
    }

    public double getHeight() {
        return height.get();
    }

    public ReadOnlyDoubleProperty widthProperty() {
        return width;
    }

    public ReadOnlyDoubleProperty heightProperty() {
        return height;
    }

    //----------------|
    //   add/remove   |
    //----------------|
    public void clear() {
        for (Edge edge : edges)
            edge.disconnect();
        for (Vertex vertex : vertices)
            vertex.disconnect();
        edges.clear();
        vertices.clear();
        getChildren().remove(1, getChildren().size());
    }

    public Vertex addVertex(double x, double y, boolean createAction) {
        needToSave = true;

        Vertex vertex = new Vertex(this, x, y);
        addVertex(vertex, createAction);
        return vertex;
    }

    public void addVertex(Vertex vertex, boolean createAction) {
        needToSave = true;

        if (createAction)
            actionsController.addAction(new CreateVertex(vertex, this));
        vertex.connect();
        getChildren().add(vertex);
        vertices.add(vertex);
    }

    public void addEdge(Vertex firstVertex, Vertex secondVertex, boolean createAction) {
        needToSave = true;
        BinaryEdge edge = new BinaryEdge(this, firstVertex, secondVertex);
        addEdge(edge, createAction);
    }

    public void addEdge(Vertex v1, Vertex v2, Edge.Direction direction, boolean createAction) {
        needToSave = true;
        BinaryEdge edge = new BinaryEdge(this, v1, v2, direction);
        addEdge(edge, createAction);
    }

    public void addEdge(Vertex vertex, boolean createAction) {
        needToSave = true;

        UnaryEdge edge = new UnaryEdge(this, vertex);
        addEdge(edge, createAction);
    }

    public void addEdge(Edge edge, boolean createAction) {
        needToSave = true;

        if (createAction)
            actionsController.addAction(new CreateEdge(edge, this));
        edge.connect();
        getChildren().add(1, edge);
        edges.add(edge);
    }

    public void removeVertexWithEdges(Vertex vertex, boolean createAction) {
        needToSave = true;

        Set<Edge> incidentEdges = vertex.getEdgesCopy();

        if (createAction)
            actionsController.addAction(new DeleteVertex(vertex, incidentEdges, this));

        for (Edge edge : incidentEdges) {
            edge.disconnect();
            edges.remove(edge);
            getChildren().remove(edge);
        }

        vertex.disconnect();
        vertices.remove(vertex);
        getChildren().remove(vertex);
    }

    public void removeEdge(Edge edge, boolean createAction) {
        needToSave = true;

        if (createAction)
            actionsController.addAction(new DeleteEdge(edge, this));
        edge.disconnect();
        getChildren().remove(edge);
        edges.remove(edge);
    }

    //------------|
    //   events   |
    //------------|
    // mouse
    private MouseEvents emptyEvents = new MouseEvents(),
                        moveEvents = new MoveEvents(this),
                        createVertexEvents = new CreateVertexEvents(this),
                        createEdgeEvents = new CreateEdgeEvents(this),
                        deleteEvents = new DeleteEvents(this),
                        selectTwoVerticesEvents = new SelectTwoVerticesEvents(),
                        selectSpiderEvents = new SelectSpiderEvents(this),
                        selectBugsEvents = new SelectBugsEvents(this);
    private MouseEvents currentEvents = emptyEvents;

    private void configureEventsFor(Action action) {
        currentEvents.deactivate();
        switch (action) {
            case Empty:
                currentEvents = emptyEvents; break;
            case Move:
                currentEvents = moveEvents; break;
            case CreateVertex:
                currentEvents = createVertexEvents; break;
            case CreateEdge:
                currentEvents = createEdgeEvents; break;
            case Delete:
                currentEvents = deleteEvents; break;
            case SelectTwoVertices:
                currentEvents = selectTwoVerticesEvents; break;
            case SelectSpider:
                currentEvents = selectSpiderEvents; break;
            case SelectBug:
                currentEvents = selectBugsEvents; break;
        }
    }

    public void setOnTwoVerticesSelected(BiConsumer<Vertex, Vertex> consumer) {
        ((SelectTwoVerticesEvents) selectTwoVerticesEvents).setOnSelected(consumer);
    }

    private void onMouseClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            currentEvents.callOnClickEvent(event);
        }
    }

    public void onMouseClick_vertex(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            currentEvents.callOnLeftClickVertex(event);
        }
        else if (event.getButton() == MouseButton.SECONDARY) {
            currentEvents.callOnRightClickVertex(event);
        }
    }

    public void onMouseClick_edge(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            currentEvents.callOnLeftClickEdge(event);
        }
        else if (event.getButton() == MouseButton.SECONDARY) {
            currentEvents.callOnRightClickEdge(event);
        }
    }

    public void onMousePress_vertex(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            currentEvents.callOnPressVertex(event);
        }
    }

    public void onMousePress_edge(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            currentEvents.callOnPressEdge(event);
        }
    }

    private void onMouseDrag(MouseEvent event) {
        currentEvents.callOnDrag(event);
    }

    private void onMouseRelease(MouseEvent event) {
        currentEvents.callOnRelease(event);
    }
    // context menus
    private void onVertexContextMenuAction(VertexEvent event) {
        Vertex vertex = event.getVertex();
        switch (event.getAction()) {
            case Rename:
                String newName = InputDialogs.getVertexName(vertex.getName());
                if (newName != null)
                    vertex.setName(newName, true);
                break;
            case MakeLoop:
                addEdge(vertex, true);
                break;
            case Delete:
                removeVertexWithEdges(vertex, true);
                break;
        }
    }

    private void onEdgeContextMenuAction(EdgeEvent event) {
        Edge edge = event.getEdge();
        switch (event.getAction()) {
            case ChangeWeight:
                Double res = InputDialogs.getEdgeWeight(edge.getWeight());
                if (res != null)
                    edge.setWeight(res, true);
                break;
            case SelectBothDirection:
                edge.setDirection(Edge.Direction.Both, true);
                break;
            case SelectFirstDirection:
                edge.setDirection(Edge.Direction.FirstVertex, true);
                break;
            case SelectSecondDirection:
                edge.setDirection(Edge.Direction.SecondVertex, true);
                break;
            case Delete:
                removeEdge(edge, true);
                break;
        }
    }


}
