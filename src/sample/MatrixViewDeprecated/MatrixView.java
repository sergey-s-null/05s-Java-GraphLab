package sample.MatrixViewDeprecated;

import javafx.collections.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Vertex;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Deprecated
public class MatrixView extends GridPane {
    private List<Integer> verticesID = new ArrayList<>();
    private List<Label> topHeader = new ArrayList<>(),
                        leftHeader = new ArrayList<>();
    private List<List<TextField> > textFields = new ArrayList<>();


    public MatrixView(ObservableList<Vertex> vertices, ObservableSet<Edge> edges) {
        super();

        vertices.addListener((ListChangeListener<? super Vertex>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Vertex vertex : change.getAddedSubList())
                        wasAddedVertex(vertex);
                }
                // TODO thing about else
                if (change.wasRemoved()) {
                    for (Vertex vertex : change.getRemoved())
                        wasRemovedVertex(vertex);
                }
            }
        });
        edges.addListener((SetChangeListener<? super Edge>) change -> {
            if (change.wasAdded()) {
                wasAddedEdge(change.getElementAdded());
            }
            // TODO think about else
            if (change.wasRemoved()) {
                wasRemovedEdge(change.getElementRemoved());
            }
        });

    }

    private int getInsertIndex(int vertexID) {
        if (verticesID.isEmpty())
            return 0;

        int res = 0;
        while (res < verticesID.size() && vertexID > verticesID.get(res))
            res++;
        return res;
    }

    private void updateGrid() {
        Date start = new Date();

        getChildren().clear();
        for (int i = 0; i < verticesID.size(); ++i) {
            add(topHeader.get(i), i + 1, 0);
            add(leftHeader.get(i), 0, i + 1);
        }

        for (int row = 0; row < verticesID.size(); ++row) {
            for (int col = 0; col < verticesID.size(); ++col) {
                add(textFields.get(row).get(col), col + 1, row + 1);
            }
        }

        Date end = new Date();
        System.out.println("Grid updated in " + (end.getTime() - start.getTime()));
    }

    private void createHeaders(int index, Vertex vertex) {
        Label topLabel = new VertexNameLabel(vertex),
              leftLabel = new VertexNameLabel(vertex);

        topHeader.add(index, topLabel);
        leftHeader.add(index, leftLabel);
    }

    private void createFields(int index) {
        textFields.add(index, new ArrayList<>());

        // создание
        for (int i = 0; i < verticesID.size() - 1; ++i)
            textFields.get(index).add(new EdgeWeightField());
        for (int row = 0; row < verticesID.size(); ++row)
            textFields.get(row).add(index, new EdgeWeightField());
    }

    private void deleteHeaders(int index) {
        topHeader.remove(index);
        leftHeader.remove(index);
    }

    private void deleteFields(int index) {
        textFields.remove(index);

        for (List<TextField> row : textFields)
            row.remove(index);

    }

    //----------------------|
    //   vertices methods   |
    //----------------------|
    private void wasAddedVertex(Vertex vertex) {
        int insertIndex = getInsertIndex(vertex.getVertexId());
        verticesID.add(insertIndex, vertex.getVertexId());
        createHeaders(insertIndex, vertex);
        createFields(insertIndex);
        updateGrid();
    }

    private void wasRemovedVertex(Vertex vertex) {
        int removeIndex = verticesID.indexOf(vertex.getVertexId());
        verticesID.remove(removeIndex);
        deleteHeaders(removeIndex);
        deleteFields(removeIndex);
        updateGrid();
    }

    //-------------------|
    //   edges methods   |
    //-------------------|
    private void wasAddedEdge(Edge edge) {

        int rowIndex = verticesID.indexOf(edge.getFirstVertex().getVertexId());
        int colIndex = verticesID.indexOf(edge.getSecondVertex().getVertexId());

        EdgeWeightField field = (EdgeWeightField) textFields.get(rowIndex).get(colIndex);
        field.addEdge(edge);
    }

    private void wasRemovedEdge(Edge edge) {
        int rowIndex = verticesID.indexOf(edge.getFirstVertex().getVertexId());
        int colIndex = verticesID.indexOf(edge.getSecondVertex().getVertexId());

        EdgeWeightField field = (EdgeWeightField) textFields.get(rowIndex).get(colIndex);
        field.removeEdge(edge);
    }



}
