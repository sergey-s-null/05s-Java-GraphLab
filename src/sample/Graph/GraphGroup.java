package sample.Graph;

import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
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
    private Vertex selected = null;

    private Vertex movingVertex = null;
    private Vector2D mousePressedPos = new Vector2D(0, 0),
                     vertexStartPos = new Vector2D(0, 0);

    private Edge movingEdge = null;

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
                    // TODO colorful vertex
                }
                else if (selected == event.getSource()) {
                    addEdge(selected);
                    selected = null;
                }
                else {
                    addEdge(selected, (Vertex)event.getSource());
                    selected = null;
                }
            }
            else if (currentAction == Action.Delete) {
                removeVertex((Vertex)event.getSource());
            }
        }
        else if (event.getButton() == MouseButton.SECONDARY) {
            // TODO
        }

    }

    public void onMouseClick_edge(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (currentAction == Action.Delete) {
                removeEdge((Edge)event.getSource());
            }
        }
        else if (event.getButton() == MouseButton.SECONDARY) {
            // TODO
        }
    }

    public void onMousePress_vertex(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (currentAction == Action.Move) {
                if (movingVertex != null)
                    System.out.println("WARNING! movingVertex != null");// TODO

                movingVertex = (Vertex) event.getSource();
                mousePressedPos = new Vector2D(event.getX(), event.getY());
                vertexStartPos = new Vector2D(movingVertex.getCenterX(), movingVertex.getCenterY());
            }
        }
    }

    public void onMousePress_edge(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && currentAction == Action.Move) {
            if (movingEdge != null)
                System.out.println("WARNING! movingEdge != null"); // TODO

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



}
