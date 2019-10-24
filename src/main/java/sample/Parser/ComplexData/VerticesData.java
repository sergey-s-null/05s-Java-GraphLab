package sample.Parser.ComplexData;

import sample.Graph.Elements.Vertex;
import sample.Parser.SimpleData.VertexData;

import java.util.*;

public class VerticesData {
    private Map<String, Integer> nameToIndex = new HashMap<>();
    private List<VertexData> vertices = new ArrayList<>();

    public boolean contains(String name) {
        return nameToIndex.get(name) != null;
    }

    public int count() {
        return vertices.size();
    }

    public String getName(int index) {
        return vertices.get(index).getName();
    }

    public boolean isNamesUnique() {
        return nameToIndex.size() == vertices.size();
    }

    public boolean isNamesValid() {
        for (VertexData data : vertices)
            if (!Vertex.isNameValid(data.getName())) return false;
        return true;
    }

    public void add(VertexData data) {
        vertices.add(data);
        if (!nameToIndex.containsKey(data.getName()))
            nameToIndex.put(data.getName(), vertices.size() - 1);
    }

    public Set<String> getNames() {
        return nameToIndex.keySet();
    }

    public List<VertexData> get() {
        return vertices;
    }

    public VertexData get(int index) {
        return vertices.get(index);
    }

    public Integer getIndex(String vertexName) {
        return nameToIndex.get(vertexName);
    }

}
