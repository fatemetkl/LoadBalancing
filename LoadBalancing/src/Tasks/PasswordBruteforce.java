package Tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import Client.BenchmarkTester;
import Common.Constants;
import Common.Task;

public class PasswordBruteforce extends Task {

	private static final long serialVersionUID = 8975901136253781195L;
	
	private String password;
	
	/**
	 * Create calculator with user specified parameters.
	 * Otherwise, default constructor calculates primes in random range 0-n,
	 * where 0 < n < 5,000,000, and writes results to a file.
	 */
	public PasswordBruteforce() {
		super();
		if ( BenchmarkTester.makeTasksRandom || (! getArguments()) ) {
			password = generatePassword();
		}
		description += String.format("\nAttempts to get password (%s) using bruteforce.", password);
	}
	
	/**
	 * Bruteforce specific password
	 * @param _password - password to bruteforce
	 */
	public PasswordBruteforce(String _password) {
		super();
		password = _password;
		description += String.format("\nAttempts to get password (%s) using bruteforce.", password);
	}
	
	/**
	 * Look for password and write checked options to file
	 */
	@Override
	public Object runCoreLogic() {
		String result = bruteforcePassword("");
		if (writer != null) {
			if (result != null) {
				writer.write("Password found! Password:" + result + "\n");
			} else {
				writer.write("Exhausted all legal options. Password was not found.");
			}
		}
		return result;
	}
	
	/**
	 * Bruteforce the password recursively.
	 * @param givenPart - starting point for the next recursive call
	 * @return cracked password, if was cracked
	 * 		   null, otherwise
	 */
	private String bruteforcePassword(String givenPart) {
		String result = null;
		if (givenPart.length() == password.length()) {
			if (writer != null) {
				writer.write(givenPart + "\n");
			}
			if (givenPart.equals(password)) {
				result = givenPart;
			} 
		} else {
			for (int i = 0; i < 36; i++) {
				int charIndex = i;
				if (charIndex < 10) {
					charIndex += '0';
				} else {
					charIndex = charIndex - 10 + 'A';
				}
				char nextChar = (char) charIndex;
				result = bruteforcePassword(givenPart + nextChar);
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Generates random password of random length (max >= len >= min)
	 * Password consists of upper case characters and decimal digits
	 * @return - generated password
	 */
	private String generatePassword() {
		Random rand = new Random();
		int minLen = Constants.PASSWORD_MIN_LENGTH;
		int maxLen = Constants.PASSWORD_MAX_LENGTH;
		int length = rand.nextInt(maxLen - minLen) + minLen;
		String password = "";
		for (int i = 0; i < length; i++) {
			int charIndex = rand.nextInt(36);
			if (charIndex < 10) {
				charIndex += '0';
			} else {
				charIndex = charIndex - 10 + 'A';
			}
			char nextChar = (char) charIndex;
			password = password + nextChar;
		}
		return password;
	}
	
	/**
	 * Get parameter values from the user input
	 * @return - true, if user's values should be used
	 * 			 false, if default values should be used
	 */
	private boolean getArguments() {
		boolean badInput = true;
		while (badInput) {
			System.out.print("Enter password (upper letters and digits): ");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String format = "^[A-Z0-9]{1,}$";
			try {
				String input = reader.readLine();
				input.replaceAll("\\s+", "");
				if (input.matches(format)) {
					password = input;
					badInput = false;
				} else if (input.isEmpty()) {
					return false;
				}				
			} catch (IOException e) {}
			if (badInput) {
				System.out.println("Password can only contain upper letters and digits.");
			}
		}
		return true;
	}

	@Override
	public int getTaskType() {
		return 1;
	}

	@Override
	public long getUID(){
		return serialVersionUID;
	}
}
