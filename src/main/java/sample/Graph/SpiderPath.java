package sample.Graph;

import sample.Graph.Elements.Edge;
import sample.Graph.Elements.Spider;
import sample.Graph.Elements.Vertex;

import java.util.HashMap;
import java.util.Map;

public class SpiderPath extends GraphPath {
    public static SpiderPath of(GraphPath path) {
        SpiderPath result = new SpiderPath(path.getFirstVertex());
        result.vertices.addAll(path.vertices.subList(1, path.vertices.size()));
        result.edges.addAll(path.edges);
        result.length = path.length;

        for (int i = 0; i < result.vertices.size(); ++i) {
            Vertex vertex = result.vertices.get(i);
            if (vertex.isBug() && !result.bugToIndex.containsKey(vertex))
                result.bugToIndex.put(vertex, i);
        }
        return result;
    }

    public static SpiderPath concat(SpiderPath first, SpiderPath second) {
        if (first.getLastVertex() != second.getFirstVertex())
            throw new RuntimeException("Last and first vertex of another path does not match.");

        SpiderPath result = new SpiderPath(first);

        result.vertices.addAll(second.vertices.subList(1, second.vertices.size()));
        result.edges.addAll(second.edges);
        result.length += second.length;

        for (Vertex bug : second.bugToIndex.keySet()) {
            if (!first.bugToIndex.containsKey(bug))
                result.bugToIndex.put(bug, first.vertices.size() - 1 + second.bugToIndex.get(bug));
        }

        return result;
    }


    private Map<Vertex, Integer> bugToIndex = new HashMap<>();

    public SpiderPath(Vertex spiderVertex) {
        super(spiderVertex);
        if (spiderVertex.isBug())
            bugToIndex.put(spiderVertex, 0);
    }

    public SpiderPath(SpiderPath another) {
        super(another);
        bugToIndex.putAll(another.bugToIndex);
    }

    @Override
    public void add(Edge edge, Vertex nextVertex) {
        super.add(edge, nextVertex);

        if (nextVertex.isBug() && bugToIndex.get(nextVertex) == null)
            bugToIndex.put(nextVertex, vertices.size() - 1);
    }

    @Override
    public void removeLast() {
        if (bugToIndex.get(getLastVertex()) == vertices.size() - 1)
            bugToIndex.remove(getLastVertex());

        super.removeLast();
    }

    public boolean containsBug(Vertex bugVertex) {
        return bugToIndex.containsKey(bugVertex);
    }

    public int getBugsCount() {
        return bugToIndex.size();
    }

    public Spider getSpider() {
        return getFirstVertex().getSpider();
    }
}
