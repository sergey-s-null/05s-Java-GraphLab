package sample.Graph;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import sample.Graph.ContextMenus.EdgeContextMenu;
import sample.Graph.ContextMenus.VertexContextMenu;
import sample.Graph.Elements.BinaryEdge;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.UnaryEdge;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphActions.*;
import sample.Parser.GraphData;


public class GraphGroup extends Group {
    public enum Action {
        Empty,
        CreateVertex,
        CreateEdge,
        Delete,
        Move,
    }

    public static double width = 400, height = 400;

    private Action currentAction = Action.Empty;
    private Vertex selected = null; //dangerous
    private Edge movingEdge = null; //dangerous
    private Vertex movingVertex = null; //dangerous
    private Vector2D mousePressedPos = new Vector2D(0, 0),
                     vertexStartPos = new Vector2D(0, 0);

    private VertexContextMenu vertexContextMenu = new VertexContextMenu(this);
    private EdgeContextMenu edgeContextMenu = new EdgeContextMenu(this);
    private GraphInputDialog inputDialog = new GraphInputDialog();

    private ObservableList<Vertex> vertices = FXCollections.observableArrayList();
    private ObservableSet<Edge> edges = FXCollections.observableSet();


    public GraphGroup() {
        super();

        Rectangle rectangle = new Rectangle(width, height); // TODO save rectangle to class var
        rectangle.setFill(Color.WHITE);
        rectangle.setStrokeWidth(1);
        rectangle.setStrokeType(StrokeType.INSIDE);
        getChildren().add(rectangle);

        setClip(new Rectangle(width, height));

        setOnMouseClicked(this::onMouseClick);
        setOnMouseDragged(this::onMouseDrag);
        setOnMouseReleased(this::onMouseRelease);

    }

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

    public void changeGraph(GraphData data) {

    }

    //----------------|
    //   add/remove   |
    //----------------|
    public void addVertex(Vertex vertex) {
        getChildren().add(vertex);
        vertices.add(vertex);
    }

    public void addEdge(Edge edge) {
        edge.connectVertices();
        getChildren().add(1, edge);
        edges.add(edge);
    }

    public void removeVertexWithEdges(Vertex vertex) {
        for (Edge edge : vertex.getEdges()) {
            getChildren().remove(edge);
            edges.remove(edge);
        }
        vertex.removeAllIncidentEdges();
        getChildren().remove(vertex);
        vertices.remove(vertex);
    }

    public void removeEdge(Edge edge) {
        edge.disconnectVertices();
        getChildren().remove(edge);
        edges.remove(edge);
    }

    //-------------------|
    //   create/delete   |
    //-------------------|
    //controls actions in GraphActionController in contrast add/remove
    private void createVertex(double x, double y) {
        Vertex vertex = new Vertex(this, x, y);
        CreateVertex.create(vertex, this);
        addVertex(vertex);
    }

    private void createEdge(Vertex firstVertex, Vertex secondVertex) {
        BinaryEdge edge = new BinaryEdge(this, firstVertex, secondVertex);
        CreateEdge.create(edge, this);
        addEdge(edge);
    }

    private void createEdge(Vertex vertex) {
        UnaryEdge edge = new UnaryEdge(this,vertex);
        CreateEdge.create(edge, this);
        addEdge(edge);
    }

    private void deleteVertex(Vertex vertex) {
        DeleteVertex.create(vertex, vertex.getEdges(), this);
        removeVertexWithEdges(vertex);
    }

    private void deleteEdge(Edge edge) {
        DeleteEdge.create(edge, this);
        removeEdge(edge);
    }

    //------------|
    //   events   |
    //------------|
    private void onMouseClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (currentAction == Action.CreateVertex) {
                createVertex(event.getX(), event.getY());
            }
        }
    }

    public void onMouseClick_vertex(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (currentAction == Action.CreateEdge) {
                if (selected == null) {
                    selected = (Vertex)event.getSource();
                    selected.setSelected(true);
                }
                else if (selected == event.getSource()) {
                    //addNewEdge(selected);
                    selected.setSelected(false);
                    selected = null;
                }
                else {
                    createEdge(selected, (Vertex) event.getSource());
                    selected.setSelected(false);
                    selected = null;
                }
            }
            else if (currentAction == Action.Delete) {
                deleteVertex((Vertex) event.getSource());
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
                deleteEdge((Edge) event.getSource());
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
                MoveUnaryEdge.saveCirclePos(((UnaryEdge) movingEdge).getCirclePosRelativeVertex());
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
                movingVertex.move(newVertexPos);
            }
            else if (movingEdge != null) {
                movingEdge.move(event.getX(), event.getY());
            }
        }
    }

    private void onMouseRelease(MouseEvent event) {
        if (movingVertex != null) {
            MoveVertex.create(movingVertex);
            movingVertex = null;
        }

        if (movingEdge != null) {
            if (movingEdge.getClass() == UnaryEdge.class) {
                UnaryEdge edge = (UnaryEdge) movingEdge;
                MoveUnaryEdge.create(edge);
            }
            else if (movingEdge.getClass() == BinaryEdge.class) {
                BinaryEdge edge = (BinaryEdge) movingEdge;
                MoveBinaryEdge.create(edge);
            }
            movingEdge = null;
        }

    }

    public void onVertexContextMenuAction(Vertex vertex, VertexContextMenu.Action action) {
        switch (action) {
            case Rename:
                String newName = inputDialog.getVertexName(vertex.getName());
                if (newName != null)
                    vertex.changeName(newName);
                break;
            case MakeLoop:
                createEdge(vertex);
                break;
            case Delete:
                deleteVertex(vertex);
                break;
        }
    }

    public void onEdgeContextMenuAction(Edge edge, EdgeContextMenu.Action action) {
        switch (action) {
            case ChangeWeight:
                Double res = inputDialog.getEdgeWeight(edge.getWeight());
                if (res != null)
                    edge.changeWeight(res);
                break;
            case SelectBothDirection:
                edge.changeDirection(Edge.Direction.Both);
                break;
            case SelectFirstDirection:
                edge.changeDirection(Edge.Direction.FirstVertex);
                break;
            case SelectSecondDirection:
                edge.changeDirection(Edge.Direction.SecondVertex);
                break;
            case Delete:
                deleteEdge(edge);
                break;
        }

    }


}
