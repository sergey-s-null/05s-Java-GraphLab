package sample.Graph;

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

    private void removeVertex(Vertex vertex) {
        for (Edge edge : vertex.getEdges()) {
            getChildren().remove(edge);
        }
        vertex.removeAllIncidentEdges();
        getChildren().remove(vertex);
    }

    private void removeEdge(Edge edge) {
        edge.disconnectVertexes();
        getChildren().remove(edge);
    }

    private void addVertex(double x, double y) {
        getChildren().add(new Vertex(this, x, y));
    }

    private void addEdge(Vertex vertex) {
        UnaryEdge edge = new UnaryEdge(this, vertex);
//        getChildren().add(1, edge);
        getChildren().add(edge);
    }

    private void addEdge(Vertex firstVertex, Vertex secondVertex) {
        BinaryEdge edge = new BinaryEdge(this, firstVertex, secondVertex);
        //getChildren().add(edge);
        getChildren().add(1, edge); //0-background
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

    private void onMouseClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (currentAction == Action.CreateVertex) {
                addVertex(event.getX(), event.getY());
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
                    //addEdge(selected);
                    selected.setSelected(false);
                    selected = null;
                }
                else {
                    addEdge(selected, (Vertex)event.getSource());
                    selected.setSelected(false);
                    selected = null;
                }
            }
            else if (currentAction == Action.Delete) {
                removeVertex((Vertex)event.getSource());
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
                removeEdge((Edge)event.getSource());
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
            }
        }
    }

    public void onMousePress_edge(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && currentAction == Action.Move) {
            movingEdge = (Edge)event.getSource();
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
        if (movingVertex != null)
            movingVertex = null;
        if (movingEdge != null)
            movingEdge = null;
    }

    public void onVertexContextMenuAction(Vertex vertex, VertexContextMenu.Action action) {
        switch (action) {
            case MakeLoop:
                addEdge(vertex);
                break;
            case Delete:
                removeVertex(vertex);
                break;
        }
    }

    public void onEdgeContextMenuAction(Edge edge, EdgeContextMenu.Action action) {
        switch (action) {
            case SelectBothDirection:
                edge.setDirection(Edge.Direction.Both);
                break;
            case SelectFirstDirection:
                edge.setDirection(Edge.Direction.FirstVertex);
                break;
            case SelectSecondDirection:
                edge.setDirection(Edge.Direction.SecondVertex);
                break;
            case Delete:
                removeEdge(edge);
                break;
        }

    }

}
