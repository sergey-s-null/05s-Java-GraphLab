package sample.MatrixView;

import Jama.Matrix;
import javafx.beans.value.ChangeListener;
import javafx.collections.*;
import org.controlsfx.control.spreadsheet.*;
import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Vertex;
import sample.Parser.VertexData;
import sample.Parser.VerticesData;

import java.util.*;

public class MatrixView extends SpreadsheetView {
    private List<Vertex> vertices = new ArrayList<>();
    private EdgesContainer edgesContainer = new EdgesContainer();

    public MatrixView(ObservableList<Vertex> vertices, ObservableSet<Edge> edges) {
        super(new GridBase(0, 0));

        vertices.addListener((ListChangeListener<? super Vertex>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Vertex vertex : change.getAddedSubList())
                        wasAddedVertex(vertex);
                }
                else if (change.wasRemoved()) {
                    for (Vertex vertex : change.getRemoved())
                        wasRemovedVertex(vertex);
                }
            }
        });

        edges.addListener((SetChangeListener<? super Edge>) change -> {
            if (change.wasAdded()) {
                wasAddedEdge(change.getElementAdded());
            }
            else if (change.wasRemoved()) {
                wasRemovedEdge(change.getElementRemoved());
            }
        });

    }

    // for save in file
    public Matrix getMatrix() {
        Grid grid = getGrid();
        if (grid.getRowCount() == 0 || grid.getColumnCount() == 0)
            return null;

        Matrix result = new Matrix(grid.getRowCount(), grid.getColumnCount());
        for (int row = 0; row < grid.getRows().size(); ++row) {
            ObservableList<SpreadsheetCell> rowList = grid.getRows().get(row);
            for (int col = 0; col < rowList.size(); ++col) {
                SpreadsheetCell cell = rowList.get(col);
                result.set(row, col, (Double) cell.getItem());
            }
        }
        return result;
    }

    public VerticesData getVerticesData() {
        VerticesData data = new VerticesData();
        for (Vertex vertex : vertices)
            data.add(new VertexData(vertex.getName(), vertex.getCenterX(), vertex.getCenterY()));
        return data;
    }

    //
    private int findInsertIndex(Vertex vertex) {
        int res = 0;
        while (res < vertices.size() &&
               vertex.getVertexId() > vertices.get(res).getVertexId())
            ++res;
        return res;
    }

    private void onGridChange(GridChange change) {
        int row = change.getRow(), col = change.getColumn();
        if (row == col) {
            Vertex vertex = vertices.get(row);
            Set<Edge> edges = edgesContainer.get(vertex);
            if (edges.size() != 1) {
                // возврат к предыдущему значению
                getGrid().getRows().get(row).get(col).setItem(change.getOldValue());
            }
            for (Edge edge : edges)
                edge.setWeight((Double) change.getNewValue(), true);
        }
        else {
            Vertex vertex1 = vertices.get(row), vertex2 = vertices.get(col);
            Set<Edge> edges = edgesContainer.get(vertex1, vertex2);
            edges.removeIf(edge -> !edge.isDirectionTo(vertex2));
            if (edges.size() != 1) {
                getGrid().getRows().get(row).get(col).setItem(change.getOldValue());
            }
            for (Edge edge : edges)
                edge.setWeight((Double) change.getNewValue(), true);
        }

    }

    // updates
    private void updateHeaders() {
        for (int i = 0; i < vertices.size(); ++i) {
            getGrid().getRowHeaders().set(i, vertices.get(i).getName());
            getGrid().getColumnHeaders().set(i, vertices.get(i).getName());
        }
    }

    private void setCellEditable(SpreadsheetCell cell, boolean editable) {
        if (editable) {
            cell.setStyle("-fx-text-fill: green;");
        }
        else {
            cell.setStyle("-fx-text-fill: black;");
        }
        cell.setEditable(editable);
    }

    private void updateWeights() {
        // нижний треугольник без диагонали
        for (int row = 0; row < vertices.size(); ++row) {
            for (int col = 0; col < row; ++col) {
                Vertex vertex1 = vertices.get(row),
                       vertex2 = vertices.get(col);
                Set<Edge> edges = edgesContainer.get(vertex1, vertex2);

                SpreadsheetCell cellTo1st = getGrid().getRows().get(col).get(row),
                                cellTo2nd = getGrid().getRows().get(row).get(col);

                int countTo1st = 0, countTo2nd = 0;
                double weightTo1st = 0, weightTo2nd = 0;
                for (Edge edge : edges) {
                    if (edge.isDirectionTo(vertex1)) {
                        countTo1st++;
                        weightTo1st += edge.getWeight();
                    }

                    if (edge.isDirectionTo(vertex2)) {
                        countTo2nd++;
                        weightTo2nd += edge.getWeight();
                    }
                }

                cellTo1st.setEditable(true);
                cellTo2nd.setEditable(true);
                cellTo1st.setItem(weightTo1st);
                cellTo2nd.setItem(weightTo2nd);
                setCellEditable(cellTo1st, countTo1st == 1);
                setCellEditable(cellTo2nd, countTo2nd == 1);
            }
        }

        // диагональ
        for (int i = 0; i < vertices.size(); ++i) {
            Vertex vertex = vertices.get(i);
            Set<Edge> edges = edgesContainer.get(vertex);

            SpreadsheetCell cell = getGrid().getRows().get(i).get(i);

            int countEdges = 0;
            double sumWeight = 0;
            for (Edge edge : edges) {
                countEdges++;
                sumWeight += edge.getWeight();
            }

            cell.setEditable(true);
            cell.setItem(sumWeight);
            setCellEditable(cell, countEdges == 1);
        }
    }

    // row/col modifications
    private void addRowAndColumn() {
        ObservableList<ObservableList<SpreadsheetCell> > rows = getGrid().getRows();
        int newSize = rows.size() + 1;

        // add fields
        ObservableList<SpreadsheetCell> newRow = FXCollections.observableArrayList();
        for (int i = 0; i < newSize - 1; ++i)
            newRow.add(SpreadsheetCellType.DOUBLE.createCell(newSize - 1, i, 1, 1, 0.0));
        rows.add(newRow);
        //
        for (int row = 0; row < newSize; ++row)
            rows.get(row).add(SpreadsheetCellType.DOUBLE.createCell(row, newSize - 1, 1, 1, 0.0));

        GridBase grid = new GridBase(newSize, newSize);
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, this::onGridChange);
        grid.setRows(rows);
        // add headers
        for (int i = 0; i < newSize; ++i) {
            grid.getRowHeaders().add("");
            grid.getColumnHeaders().add("");
        }

        setGrid(grid);

        updateHeaders();
        updateWeights();
    }

    private void removeRowAndColumn() {
        ObservableList<ObservableList<SpreadsheetCell> > rows = getGrid().getRows();
        int oldSize = rows.size();
        if (oldSize == 0)
            return;

        rows.remove(oldSize - 1);
        for (ObservableList<SpreadsheetCell> row : rows)
            row.remove(oldSize - 1);

        GridBase grid = new GridBase(oldSize - 1, oldSize - 1);
        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, this::onGridChange);
        grid.setRows(rows);
        // add headers
        for (int i = 0; i < oldSize - 1; ++i) {
            grid.getRowHeaders().add("");
            grid.getColumnHeaders().add("");
        }

        setGrid(grid);

        updateHeaders();
        updateWeights();
    }

    //--------------------|
    //   vertex methods   |
    //--------------------|
    private Map<Vertex, ChangeListener<String> > vertexIdToNameListener = new HashMap<>();

    private ChangeListener<String> getListener(Vertex vertex) {
        return ((observable, oldValue, newValue) -> {
            changedVertexName(vertex, newValue);
        });
    }

    private void wasAddedVertex(Vertex vertex) {
        // вытащить старый rows
        // создать новый rows (чтобы выставить новые row, col для каждой ячейки)
        // создать новую GridBase с новым размером
        //    новый EventHandler
        // вставить в GridBase rows
        // выставить GridBase для SpreadsheetView

        int index = findInsertIndex(vertex);
        vertices.add(index, vertex);

        addRowAndColumn();

        ChangeListener<String> listener = getListener(vertex);
        vertexIdToNameListener.put(vertex, listener);
        vertex.nameProperty().addListener(listener);
    }

    private void wasRemovedVertex(Vertex vertex) {
        vertices.remove(vertex);

        removeRowAndColumn();

        vertex.nameProperty().removeListener(vertexIdToNameListener.remove(vertex));
    }

    private void changedVertexName(Vertex vertex, String newName) {
        int index = vertices.indexOf(vertex);

        getGrid().getRowHeaders().set(index, newName);
        getGrid().getColumnHeaders().set(index, newName);
    }

    //------------------|
    //   edge methods   |
    //------------------|
    private Map<Edge, ChangeListener<Number> > edgeToWeightListener = new HashMap<>();
    private Map<Edge, ChangeListener<Edge.Direction> > edgeToDirectionListener = new HashMap<>();

    private ChangeListener<Number> getWeightListener(Edge edge) {
        return ((observable, oldValue, newValue) -> {
            changedEdgeWeight(edge, (Double) newValue);
        });
    }

    private ChangeListener<Edge.Direction> getDirectionListener(Edge edge) {
        return ((observable, oldValue, newValue) -> {
            changedEdgeDirection(edge, newValue);
        });
    }

    private void wasAddedEdge(Edge edge) {
        edgesContainer.add(edge);

        ChangeListener<Number> weightListener = getWeightListener(edge);
        edgeToWeightListener.put(edge, weightListener);
        edge.weightProperty().addListener(weightListener);

        ChangeListener<Edge.Direction> directionListener = getDirectionListener(edge);
        edgeToDirectionListener.put(edge, directionListener);
        edge.directionProperty().addListener(directionListener);

        updateWeights();// TODO think about update only 1-2 fields
    }

    private void wasRemovedEdge(Edge edge) {
        edgesContainer.remove(edge);

        edge.weightProperty().removeListener(edgeToWeightListener.remove(edge));

        edge.directionProperty().removeListener(edgeToDirectionListener.remove(edge));

        updateWeights();// TODO think about update only 1-2 fields
    }

    private void changedEdgeWeight(Edge edge, double newWeight) {
        // parameters can be use for optimal updating of grid
        updateWeights();
    }

    private void changedEdgeDirection(Edge edge, Edge.Direction newDirection) {
        // parameters can be use for optimal updating of grid
        updateWeights();
    }


}
