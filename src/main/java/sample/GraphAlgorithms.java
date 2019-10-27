package sample;

import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Element;
import sample.Graph.Elements.Vertex;

import java.util.*;

public class GraphAlgorithms {
    public static class GraphPath {
        private List<Element> path = new ArrayList<>();

        public GraphPath(Vertex firstVertex) {
            path.add(firstVertex);
        }

        public GraphPath(GraphPath other) {
            path.addAll(other.path);
        }

        public void add(Edge edge, Vertex nextVertex) {
            path.add(edge);
            path.add(nextVertex);
        }

        public Vertex getLastVertex() {
            return (Vertex) path.get(path.size() - 1);
        }

        public List<Element> getPathCopy() {
            return new ArrayList<>(path);
        }

        public int getEdgeCount() {
            return (path.size() - 1) / 2;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < path.size(); i += 2) {
                builder.append(((Vertex) path.get(i)).getName());
                if (i < path.size() - 1)
                    builder.append(" -> ");
            }
            return builder.toString();
        }
    }

    public static GraphPath breadthSearch(Vertex vertexFrom, Vertex vertexTo) {
        Set<Vertex> usedVertices = new HashSet<>();
        Deque<GraphPath> pathDeque = new ArrayDeque<>();
        pathDeque.addLast(new GraphPath(vertexFrom));

        while (pathDeque.size() > 0) {
            GraphPath currentPath = pathDeque.removeFirst();
            Vertex pathLastVertex = currentPath.getLastVertex();
            if (usedVertices.contains(pathLastVertex))
                continue;
            usedVertices.add(pathLastVertex);
            if (pathLastVertex == vertexTo) {
                return currentPath;
            }

            List<Vertex.EdgeWithVertex> nextVertices = pathLastVertex.getNextVertices();
            for (Vertex.EdgeWithVertex pair : nextVertices) {
                if (usedVertices.contains(pair.vertex))
                    continue;

                GraphPath newPath = new GraphPath(currentPath);
                newPath.add(pair.edge, pair.vertex);
                pathDeque.addLast(newPath);
            }
        }
        return null;
    }
}
