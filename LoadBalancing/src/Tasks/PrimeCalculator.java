package Tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import Client.BenchmarkTester;
import Common.Constants;
import Common.Task;

/**
 * This task finds all prime numbers in a given range.
 * If specified, will write results to file.
 * This is done to free up CPU time for other tasks and
 * better imitate load balancing mechanism
 * @author Sviatoslav Sivov
 */
public class PrimeCalculator extends Task {

	private static final long serialVersionUID = 5335331520483705793L;

	/** Find primes larger than this */
	private long min;
	
	/** Find primes less than max */
	private long max;
	
	
	/**
	 * Create calculator with user specified parameters.
	 * Otherwise, default constructor calculates primes in random range 0-n,
	 * where 0 < n < 5,000,000, and writes results to a file.
	 */
	public PrimeCalculator() {
		super();
		if ( BenchmarkTester.makeTasksRandom || (! getArguments()) ) {
			min = 0;
			max = Math.abs((new Random()).nextInt(Constants.PRIME_UPPER_BOUNDARY));
		}
		description += String.format("\nCalculates prime numbers in range %d-%d.", min, max);
	}
	
	/**
	 * Specific constructor
	 * @param _min - min of the range
	 * @param _max - max of the range
	 */
	public PrimeCalculator(int _min, int _max) {
		super();
		min = _min;
		max = _max;
		description += String.format("\nCalculates prime numbers in range %d-%d.", min, max);
	}
	
	/**
	 * Perform calculation
	 * At the end, set result field to the result
	 */
	public Object runCoreLogic() {
		Integer primeCount = 0;
		for (long num = min; num < max + 1; num ++) {
			if (isPrime(num)) {
				primeCount++;
				if (writer != null) {
					writer.write(String.valueOf(num) + "\n");
				}
			}
		}
		return primeCount;
	}
	
	/**
	 * Determines whether number is a prime
	 * @param num - number to check
	 * @return true - if prime
	 * 		   false - otherwise
	 */
	private boolean isPrime(long num) {
		boolean isPrime = true;
		// if divisible by 2, then not prime --> exit
		if (num % 2 == 0) {
			isPrime = false;
		} else {
			// if has a divisor, then not prime --> exit
			for (long divisor = 3; divisor < Math.sqrt(num); divisor += 2) {
				if (num % divisor == 0) {
					isPrime = false;
					break;
				}
			}
		}
		return isPrime;
	}
	
	/**
	 * Get parameter values from the user input
	 * @return - true, if user's values should be used
	 * 			 false, if default values should be used
	 */
	private boolean getArguments() {
		boolean badInput = true;
		while (badInput) {
			System.out.print("Enter calculation range 'min-max' : ");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//			String format = "^([1-9]\\d*|0)[-]([1-9][0-9]*)$";
			String format = "^\\d*-\\d*$";
			try {
				String input = reader.readLine();
				input.replaceAll("\\s+", "");
				if (input.matches(format)) {
					String[] minmax = input.split("-");
					min = Long.parseLong(minmax[0]);
					max = Long.parseLong(minmax[1]);
					if (max >= min) {
						badInput = false;
					}
				} else if (input.isEmpty()) {
					return false;
				}				
			} catch (IOException e) {}
			if (badInput) {
				System.out.println("Invalid range given. Please try again.");
			}
		}
		return true;
	}

	@Override
	public int getTaskType() {
		return 3;
	}

	@Override
	public long getUID(){
		return serialVersionUID;
	}
}
