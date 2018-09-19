package Tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import Client.BenchmarkTester;
import Common.Constants;
import Common.Task;

/**
 * This task just wastes time, counting down specified
 * amount of seconds. For different CPU load, different
 * tasks write 'check points' to file with different
 * frequencies (# writes per second)
 * @author Sviatoslav Sivov	
 */
public class Timer extends Task {
	
	private static final long serialVersionUID = -6794273524550764597L;

	/** How many seconds should timer count */
	private int seconds;
	
	/** How many times per second write to file? */
	private int frequency;
	
	/**
	 * Create timer with user specified parameters.
	 * Otherwise, default constructor runs for 0 < seconds < 500
	 * and writes with frequency 1 < writes/sec < 100
	 */
	public Timer() {
		super();
		if ( BenchmarkTester.makeTasksRandom || (! getArguments()) ){
			Random rand = new Random();
			seconds = rand.nextInt(Constants.TIMER_MAX_LENGTH) + 1;
			frequency = rand.nextInt(100) + 1;
		}
		frequency = (new Random()).nextInt(100) + 1;
		description += String.format("\nCounts down %d seconds, writing to file %d times per second.",
									seconds, frequency);
	}
	
	/**
	 * Constructor for creating deterministic task. 
	 * @param length - length of the timer
	 * @param _frequency - frequency of writing to disk
	 */
	public Timer(int length, int _frequency) {
		super();
		seconds = length;
		frequency = _frequency;
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
			float stamp = i * timeout;
			writer.write(stamp/1000 + "\n");
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {}
		}
		writer.write("0\nTimer done!");
		return seconds;
	}

	/**
	 * Get parameter values from the user input
	 * @return - true, if user's values should be used
	 * 			 false, if default values should be used
	 */
	private boolean getArguments() {
		boolean badInput = true;
		while (badInput) {
			System.out.print("Enter timer length in seconds: ");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String format = "^\\d+$";
			try {
				String input = reader.readLine();
				input.replaceAll("\\s+", "");
				if (input.matches(format)) {
					seconds = Integer.parseInt(input);
					badInput = false;
				} else if (input.isEmpty()) {
					return false;
				}				
			} catch (IOException e) {}
			if (badInput) {
				System.out.println("Timer length has to be a positive integer.\n");
			}
		}
		return true;
	}

	@Override
	public int getTaskType() {
		return 5;
	}

	@Override
	public long getUID(){
		return serialVersionUID;
	}	
}
