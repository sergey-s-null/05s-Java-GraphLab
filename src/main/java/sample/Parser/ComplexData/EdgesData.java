package sample.Parser.ComplexData;

import sample.Graph.Elements.Vertex;
import sample.Parser.SimpleData.EdgeData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EdgesData {
    private Set<String> verticesNames = new HashSet<>();
    private List<EdgeData> edges = new ArrayList<>();

    public void add(EdgeData data) {
        verticesNames.add(data.getVertexName1());
        verticesNames.add(data.getVertexName2());
        edges.add(data);
    }

    public int count() {
        return edges.size();
    }

    public boolean isVerticesNamesValid() {
        for (String name : verticesNames)
            if (!Vertex.isNameValid(name)) return false;
        return true;
    }

    public List<EdgeData> getEdges() {
        return edges;
    }

    public EdgeData get(int index) {
        return edges.get(index);
    }

    public Set<String> getVerticesNames() {
        return verticesNames;
    }
}
