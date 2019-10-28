package sample;

import sample.Graph.Elements.Vertex;
import sample.Graph.GraphPath;

import java.util.*;

public class GraphAlgorithms {

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
