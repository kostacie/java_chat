import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter writer;
    private String nickname;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.writer = writer;

            nickname = reader.readLine();
            System.out.println(nickname + " joined the chat.");
            ChatServer.output(nickname + " joined the chat.", this);

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println(nickname + ": " + message);
                ChatServer.output(nickname + ": " + message, this);
            }
        } catch (IOException e) {
            System.err.println("Connection with client lost.");
        } finally {
            ChatServer.removeClient(this);
            ChatServer.output(nickname + " left the chat.", this);
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
        }
    }
}