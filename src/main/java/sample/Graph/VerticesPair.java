package sample.Graph;

import sample.Graph.Elements.Vertex;

class VerticesPair {
    private Vertex v1, v2;

    VerticesPair(Vertex v1, Vertex v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public int hashCode() {
        return v1.hashCode() + v2.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VerticesPair) {
            VerticesPair another = (VerticesPair) obj;
            return (v1 == another.v1 && v2 == another.v2) ||
                    (v1 == another.v2 && v2 == another.v1);
        }
        else {
            return false;
        }
    }
}
