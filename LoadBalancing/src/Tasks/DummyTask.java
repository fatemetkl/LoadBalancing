package Tasks;

import Client.BenchmarkTester;
import Common.Constants;
import Common.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class DummyTask extends Task {

    private static final long serialVersionUID = -6794273524550764597L;

    /**
     * How many seconds should timer count
     */
    private int seconds;

    /**
     * How many times per second write to file?
     */
    private int frequency;

    /**
     * Create timer with user specified parameters.
     * Otherwise, default constructor runs for 0 < seconds < 500
     * and writes with frequency 1 < writes/sec < 100
     */
    public DummyTask() {
        super();
        Random rand = new Random();
        seconds = rand.nextInt(Constants.TIMER_MAX_LENGTH);
        frequency = (new Random()).nextInt(100) + 1;
        description += String.format("\nCounts down %d seconds, writing to file %d times per second.",
                seconds, frequency);
    }

    /**
     * Execute count down and writing
     */
    public Object runCoreLogic() {
        if (1000 % frequency != 0) {
            writer.write(seconds + "\n");
        }
        int timeout = 1000 / frequency;
        for (int i = seconds * frequency; i > 0; i--) {
            //float stamp = i * timeout;
            writer.write(i + "\n");
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
            }
        }
        writer.write("0\nTimer done!");
        return seconds;
    }

    @Override
    public int getTaskType() {
        return 0;
    }

    @Override
    public long getUID() {
        return serialVersionUID;
    }
}
