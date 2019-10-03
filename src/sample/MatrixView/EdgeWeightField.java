package sample.MatrixView;

import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import sample.Graph.Elements.Edge;

import java.util.HashSet;
import java.util.Set;

public class EdgeWeightField extends SpreadsheetCellType.DoubleType {
    private Set<Edge> edges = new HashSet<>();
    private boolean inverted;

    public EdgeWeightField(MatrixView matrixView, int row, int column,
                           boolean inverted, Set<Edge> edges)
    {
        super();


    }

    public void addEdge(Edge edge) {

    }

    public void removeEdge(Edge edge) {

    }

    public Set<Edge> getEdges() {
        return edges;
    }
}
