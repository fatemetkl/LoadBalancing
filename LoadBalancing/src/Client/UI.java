package Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Common.Constants;
import Common.Task;

/**
 * This class is responsible for presenting CLI
 * (command line interface) to the user and is responsible
 * for interaction with the user. Users must be able to
 * select a computational task, configure it with appropriate
 * parameters and send it over to the server for computation.
 * When result is returned, it is presented to the user.
 *
 * @author Sviatoslav Sivov
 */
public class UI {

    /**
     * Object, representing the state of the client
     */
    private final Client client;

    /**
     * BufferedReader for non-blocking input
     */
    private BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Constructor only needs state representation object (Model)
     *
     * @param _client - client state object
     */
    public UI(Client _client) {
        client = _client;
    }

    /**
     * Ask user for connection info and then let user pick
     * computational tasks, until he quits or connection is terminated
     */
    public void run() {

        InetAddress serverAddress = client.getServerAddress();
        int serverPort = client.getServerPort();

        if (serverAddress == null) {
            try {
                serverAddress = getServerAddress();
            } catch (UnknownHostException e) {
                System.out.println(e.getMessage());
                return;
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return;
            }
        }

        if (serverPort < 0) {
            try {
                serverPort = getPort();
            } catch (IOException e) {
                // ignore
            }
        }

        client.setServerInfo(serverAddress, serverPort);

        try {
            invokeConnectionMenu(client.getServerAddress(), client.getServerPort());
            // close reader, when done
            inputReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Menu for connecting to the server
     *
     * @param address - server address
     * @param port    - port number
     * @throws IOException
     */
    private void invokeConnectionMenu(InetAddress address, int port) throws IOException {
        List<String> menu = Arrays.asList("Connect with random ID", "Connect with specific ID", "Exit");
        while (true) {
            printListAsMenu(menu, "Connect to the server");
            System.out.print("Enter option number: ");
            int clientId = Constants.NULL_ID;
            int option = getOption(1, menu.size());
            switch (option) {
                case (1): // do nothing
                    break;
                case (2):
                    boolean badId = true;
                    while (badId) {
                        System.out.print("Enter client id: ");
                        String id = inputReader.readLine();
                        try {
                            clientId = Integer.parseInt(id);
                            if (clientId > 0) {
                                badId = false;
                            }
                        } catch (NumberFormatException e) {
                        }
                        if (badId) {
                            System.out.println("ID has to be a positive integer.\n");
                        }
                    }

                    break;
                case (3):
                    return;
            }
            if (client.sync(clientId)) {
                invokeTaskSelectionMenu();
            } else {
                System.out.println("Unable to talk to authenticate with the server.");
            }
        }
    }

    /**
     * Menu, which displays available tasks, that are defined
     * in the 'Tasks' package. Lets user select a task to compute.
     *
     * @throws IOException
     */
    private void invokeTaskSelectionMenu() throws IOException {
        List<Class<?>> tasks = getTasks();
        List<String> taskNames = new ArrayList<String>();
        for (Class<?> taskType : tasks) {
            taskNames.add(taskType.getSimpleName());
        }
        taskNames.add("Exit");
        while (true) {
            printListAsMenu(taskNames, "Available tasks");
            System.out.print("Enter option: ");
            int option = getOption(1, taskNames.size());
            System.out.println();
            if (option == taskNames.size()) {
                return;
            } else {
                Class<?> taskType = tasks.get(option - 1);
                try {
                    for (int i = 0; i < 100; i++) {
                        Task task = (Task) taskType.newInstance();
                        System.out.println("\nNew task created!\n" + task.getDescription());
                        client.sendNewTask(task);
                    }
//					try {
//						task.call();
//					} catch (Exception e) {
//						// unable to perform task
//					}
//					task.openResultFile();
                } catch (InstantiationException | IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Finds out what tasks exist (define in the Tasks package)
     *
     * @return - list of Tasks
     */
    private List<Class<?>> getTasks() {
        String packageName = "Tasks";
        File unixDirectory = new File("Tasks");
        File eclipseDirectory = new File("bin/Tasks");
        File directory = unixDirectory;
        List<Class<?>> tasks = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            if (eclipseDirectory.exists()) {
                directory = eclipseDirectory;
            }
            if (!directory.isDirectory()) {
                return tasks;
            }
        }
        if (!(directory.exists() && directory.isDirectory())) {
            return tasks;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".class")) {
                String className = String.format("%s.%s", packageName, file.getName().substring(0, file.getName().length() - 6));
                try {
                    Class<?> toAdd = Class.forName(className);
                    if (Task.class.isAssignableFrom(toAdd)) {
                        tasks.add(toAdd);
                    }
                } catch (ClassNotFoundException e) {
                }

            }
        }
        return tasks;
    }

    /**
     * Utility method for print a list of menu items as a nicely formatted menu with a title
     *
     * @param menuItems - list of menu items
     * @param menuTitle - menu title
     */
    private void printListAsMenu(List<String> menuItems, String menuTitle) {
        int i = 1;
        System.out.println("\n~~~~ " + menuTitle + " ~~~~~");
        for (String s : menuItems) {
            System.out.println(i + ". " + s);
            i++;
        }
    }

    /**
     * Function for getting numerical input in a specified range
     *
     * @param minChoice - minimum allowed input value
     * @param maxChoice - maximum allowed input value
     * @return - input value
     * @throws IOException
     */
    private int getOption(int minChoice, int maxChoice) throws IOException {
        int input = -1;
        boolean inputOk = false;
        while (!inputOk) {
            String optionNumberStr = null;
            while (optionNumberStr == null) {
                if (inputReader.ready()) {
                    optionNumberStr = inputReader.readLine();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw (new IOException("getOption was interrupted!"));
                }
            }
            try {
                input = Integer.parseInt(optionNumberStr);
                if (input >= minChoice && input <= maxChoice) {
                    inputOk = true;
                } else {
                    System.out.println("  > Please enter an integer 1-" + String.valueOf(maxChoice));
                }
            } catch (NumberFormatException e) {
                System.out.println("  > Please enter an integer 1-" + String.valueOf(maxChoice));
            }
        }
        return input;
    }

    /**
     * Method for getting input for server address and verifying it is valid
     *
     * @return - InetAddress object instantiated from the input
     * @throws IOException
     */
    private InetAddress getServerAddress() throws IOException {
        InetAddress serverAddress = null;
        while (serverAddress == null) {
            System.out.print("Enter server address: ");
            String input = inputReader.readLine();
            if (input.equals("")) input = Constants.SERVER_HOST;
            try {
                serverAddress = InetAddress.getByName(input);
            } catch (UnknownHostException e) {
                System.out.println("Unknown host exception: " + e.getMessage());
            }
        }
        return serverAddress;
    }

    /**
     * Method for getting port number
     *
     * @return - port number
     * @throws IOException
     */
    private int getPort() throws IOException {
        Integer port = null;
        while (port == null) {
            System.out.print("Enter port number: ");
            String portString = inputReader.readLine();
            if (portString.equals("")) portString = Constants.SERVER_PORT + "";
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                System.out.println("Port number needs to be an integer.");
            }
        }
        return port;
    }

}
