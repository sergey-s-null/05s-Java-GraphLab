package sample.Parser;

import sample.Graph.Elements.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VerticesData {
    private Set<String> names = new HashSet<>();
    private List<VertexData> vertices = new ArrayList<>();

    public boolean contains(String name) {
        return names.contains(name);
    }

    public int count() {
        return vertices.size();
    }

    public String getName(int index) {
        return vertices.get(index).getName();
    }

    public boolean isNamesUnique() {
        return names.size() == vertices.size();
    }

    public boolean isNamesValid() {
        for (VertexData data : vertices)
            if (!Vertex.isNameValid(data.getName())) return false;
        return true;
    }

    public void add(VertexData data) {
        names.add(data.getName());
        vertices.add(data);
    }

    public Set<String> getNames() {
        return names;
    }

    public List<VertexData> get() {
        return vertices;
    }

}
