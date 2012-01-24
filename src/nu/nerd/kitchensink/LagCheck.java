package nu.nerd.kitchensink;

import java.util.LinkedList;


public class LagCheck implements Runnable
{
    private long last = System.currentTimeMillis();
    public LinkedList<Long> history = new LinkedList<Long>();

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        long duration = now - last;
        if (duration < 1000)
            duration = 1000;
        history.add(duration);
        if (history.size() > 10)
            history.poll();
        last = now;
    }
}
