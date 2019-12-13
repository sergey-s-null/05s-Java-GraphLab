package sample.Graph;

import sample.Graph.Elements.Spider;

import java.util.Timer;
import java.util.TimerTask;

public class SpiderMoveTask extends TimerTask {
    private Timer timer;
    private SpiderPath path;
    private Spider spider;
    private Runnable onEnd;
    private int current = 0;

    public SpiderMoveTask(Timer timer, SpiderPath path, Runnable onEnd) {
        this.timer = timer;
        this.path = path;
        this.spider = path.getSpider();
        this.onEnd = onEnd;
    }

    @Override
    public void run() {
        if (current >= path.getVertices().size()) {
            timer.cancel();
            onEnd.run();
            return;
        }
        spider.connect(path.getVertices().get(current++));
    }
}
