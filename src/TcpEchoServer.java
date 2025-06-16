import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpEchoServer {
    public final int port;

    public TcpEchoServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Server started at port: " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new TcpThread(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static class TcpThread implements Runnable {
        private final Socket socket;

        public TcpThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String clientInfo = socket.getRemoteSocketAddress().toString();
            System.out.println("Connected: " + clientInfo);
            try (
                    InputStream in = socket.getInputStream();
                    OutputStream out = socket.getOutputStream()
            ) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    //для проверки многопоточности, можно запустить в двух терминалах и будет видно, что
                    //действительно все происходит в многопоточной среде. можно закомментить, но придется и
                    //InterruptedException убрать
                    Thread.sleep(2000);
                    out.write(buffer, 0, read);
                    out.flush();
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Problem with " + clientInfo + ": " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {}
                System.out.println("Disconnected: " + clientInfo);
            }
        }
    }
}
