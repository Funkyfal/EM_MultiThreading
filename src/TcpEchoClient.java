import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public record TcpEchoClient(String host, int port) {

    public void start() {
        System.out.printf("Connecting to %s:%d...", host, port);
        try (
                Socket socket = new Socket(host, port);
                OutputStream out = socket.getOutputStream();
                InputStream in = socket.getInputStream();
                Scanner console = new Scanner(System.in)
        ) {
            System.out.println("\nConnected. Type 'exit' to leave.");
            while (true) {
                System.out.print(">");
                String line = console.nextLine();
                if (line.equalsIgnoreCase("exit")) {
                    System.out.println("Closing connection...");
                    break;
                }

                byte[] data = (line + "\n").getBytes();
                out.write(data);
                out.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String response = reader.readLine();
                if (response == null) {
                    System.out.println("Something wrong with the server, may be it has closed a connection.");
                    break;
                }

                System.out.println("Got an echo: " + response);
            }
        } catch (IOException e) {
            System.err.println("\nClient error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;
        if (args.length >= 1) host = args[0];
        if (args.length >= 2) port = Integer.parseInt(args[1]);
        new TcpEchoClient(host, port).start();
    }
}
