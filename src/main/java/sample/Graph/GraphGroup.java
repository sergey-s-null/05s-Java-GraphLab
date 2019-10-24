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
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.ContextMenus.EdgeContextMenu;
import sample.Graph.ContextMenus.EdgeEvent;
import sample.Graph.ContextMenus.VertexContextMenu;
import sample.Graph.ContextMenus.VertexEvent;
import sample.Graph.Elements.BinaryEdge;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.UnaryEdge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphActions.*;
import sample.Parser.SimpleData.EdgeData;
import sample.Parser.GraphData;
import sample.Parser.SimpleData.Resolution;
import sample.Parser.SimpleData.VertexData;

import java.util.*;


public class GraphGroup extends Group {
    public enum Action {
        Empty,
        CreateVertex,
        CreateEdge,
        Delete,
        Move,
    }

    public static final double minWidth = 200, minHeight = 200, maxWidth = 5000, maxHeight = 5000;
    public static final double defaultWidth = 400, defaultHeight = 400;
    private Rectangle backgroundRect = new Rectangle(),
                      clipRect = new Rectangle();
    private DoubleProperty width, height;
    private boolean needToSave = false;

    private Action currentAction = Action.Empty;
    private Vertex selected = null; //dangerous
    private Edge movingEdge = null; //dangerous
    private Vertex movingVertex = null; //dangerous
    private Vector2D mousePressedPos = new Vector2D(0, 0),
                     vertexStartPos = new Vector2D(0, 0);

    private VertexContextMenu vertexContextMenu = new VertexContextMenu(this::onVertexContextMenuAction);
    private EdgeContextMenu edgeContextMenu = new EdgeContextMenu(this::onEdgeContextMenuAction);
    private GraphInputDialog inputDialog = new GraphInputDialog();

    private ObservableList<Vertex> vertices = FXCollections.observableArrayList();
    private ObservableSet<Edge> edges = FXCollections.observableSet();

    private GraphActionsController actionsController = new GraphActionsController();

    // constructor
    public GraphGroup() {
        super();

        initRects();
        getChildren().add(backgroundRect);
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
        if (selected != null) {
            selected.setSelected(false);
            selected = null;
        }
        movingVertex = null;
        movingEdge = null;

        this.currentAction = currentAction;
        // TODO проверка текущего действия для защиты от смены Action во время создания ребра или тп
    }

    public ObservableList<Vertex> getVertices() {
        return vertices;
    }

    public ObservableSet<Edge> getEdges() {
        return edges;
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
    public void setGraph(GraphData data, boolean createAction) {
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
        if (createAction) {
            ListOfActions actions = new ListOfActions();
            for (Edge edge : edges)
                actions.add(new DeleteEdge(edge, this));
            for (Vertex vertex : vertices)
                actions.add(new DeleteVertex(vertex, null, this));
            for (Vertex vertex : nameToVertex.values())
                actions.add(new CreateVertex(vertex, this));
            for (Edge edge : newEdges)
                actions.add(new CreateEdge(edge, this));
            actions.add(getChangeResolutionActionWithVerticesRedo(data.getResolution()));
            actionsController.addAction(actions);
        }

        // исполнение
        clear();
        for (Vertex vertex : nameToVertex.values())
            addVertex(vertex, false);
        for (Edge edge : newEdges)
            addEdge(edge, false);
        setResolution(data.getResolution(), false);

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
    }

    public void setHeight(double height, boolean createAction) {
        needToSave = true;
        if (createAction) {
            actionsController.addAction(
                    getChangeResolutionActionWithVerticesRedo(new Resolution(getWidth(), height)));
        }
        backgroundRect.setHeight(height);
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

    public void addVertex(double x, double y, boolean createAction) {
        needToSave = true;

        Vertex vertex = new Vertex(this, x, y);
        addVertex(vertex, createAction);
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
    private void onMouseClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (currentAction == Action.CreateVertex) {
                addVertex(event.getX(), event.getY(), true);
            }
        }
    }

    public void onMouseClick_vertex(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (currentAction == Action.CreateEdge) {
                if (selected == null) {
                    selected = (Vertex) event.getSource();
                    selected.setSelected(true);
                }
                else if (selected == event.getSource()) {
                    //addNewEdge(selected);
                    selected.setSelected(false);
                    selected = null;
                }
                else {
                    addEdge(selected, (Vertex) event.getSource(), true);
                    selected.setSelected(false);
                    selected = null;
                }
            }
            else if (currentAction == Action.Delete) {
                removeVertexWithEdges((Vertex) event.getSource(), true);
            }
        }
        else if (event.getButton() == MouseButton.SECONDARY) {
            if (selected != null || movingVertex != null || movingEdge != null)
                return;
            vertexContextMenu.configureFor((Vertex) event.getSource());
            vertexContextMenu.show((Vertex) event.getSource(), event.getScreenX(), event.getScreenY());
        }
    }

    public void onMouseClick_edge(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (currentAction == Action.Delete) {
                removeEdge((Edge) event.getSource(), true);
            }
        }
        else if (event.getButton() == MouseButton.SECONDARY) {
            if (selected != null || movingVertex != null || movingEdge != null)
                return;
            edgeContextMenu.configureFor((Edge) event.getSource());
            edgeContextMenu.show((Edge) event.getSource(), event.getScreenX(), event.getScreenY());
        }
    }

    public void onMousePress_vertex(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (currentAction == Action.Move) {
                movingVertex = (Vertex) event.getSource();
                mousePressedPos = new Vector2D(event.getX(), event.getY());
                vertexStartPos = new Vector2D(movingVertex.getCenterX(), movingVertex.getCenterY());
                MoveVertex.savePos(vertexStartPos);
            }
        }
    }

    public void onMousePress_edge(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && currentAction == Action.Move) {
            movingEdge = (Edge)event.getSource();
            if (movingEdge.getClass() == UnaryEdge.class) {
                MoveUnaryEdge.saveCirclePos(((UnaryEdge) movingEdge).getCirclePos());
            }
            else if (movingEdge.getClass() == BinaryEdge.class) {
                BinaryEdge edge = (BinaryEdge) movingEdge;
                MoveBinaryEdge.saveParams(edge.getPointAngle(), edge.getPointRadiusCoef());
            }
        }
    }

    private void onMouseDrag(MouseEvent event) {
        if (currentAction == Action.Move) {
            if (movingVertex != null) {
                Vector2D newVertexPos = vertexStartPos.add(new Vector2D(event.getX(), event.getY())).subtract(mousePressedPos);
                movingVertex.setCenter(newVertexPos);
            }
            else if (movingEdge != null) {
                movingEdge.setPosition(event.getX(), event.getY());
            }
        }
    }

    private void onMouseRelease(MouseEvent event) {
        if (movingVertex != null) {
            actionsController.addAction(new MoveVertex(movingVertex));
            movingVertex = null;
        }

        if (movingEdge != null) {
            if (movingEdge.getClass() == UnaryEdge.class) {
                UnaryEdge edge = (UnaryEdge) movingEdge;
                actionsController.addAction(new MoveUnaryEdge(edge));
            }
            else if (movingEdge.getClass() == BinaryEdge.class) {
                BinaryEdge edge = (BinaryEdge) movingEdge;
                actionsController.addAction(new MoveBinaryEdge(edge));
            }
            movingEdge = null;
        }
    }
    // context menus
    private void onVertexContextMenuAction(VertexEvent event) {
        Vertex vertex = event.getVertex();
        switch (event.getAction()) {
            case Rename:
                String newName = inputDialog.getVertexName(vertex.getName());
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
                Double res = inputDialog.getEdgeWeight(edge.getWeight());
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