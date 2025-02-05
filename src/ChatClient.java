import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ChatClient {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private PrintWriter writer;
    private BufferedReader reader;
    private String nickname;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient::new);
    }

    public ChatClient() {
        showConnectionDialog();
        setupChat();
        connectToServer();
    }

    private void showConnectionDialog() {
        JTextField nicknameField = new JTextField();
        JTextField ipField = new JTextField("127.0.0.1");
        Object[] message = {
                "Enter your nickname:", nicknameField,
                "Enter server IP:", ipField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Connect to Chat Server", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            nickname = nicknameField.getText();
            String ipAddress = ipField.getText();

            if (nickname.isEmpty() || ipAddress.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Nickname and IP address cannot be empty!");
                showConnectionDialog();
            }

            try {
                Socket socket = new Socket(ipAddress, 12345);
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Отправляем никнейм на сервер
                writer.println(nickname);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Unable to connect to server: " + e.getMessage());
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }

    private void setupChat() {
        frame = new JFrame("Chat Client - " + nickname);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        JButton sendButton = new JButton("Send");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(messageField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        frame.setVisible(true);
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    chatArea.append(message + "\n");
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Disconnected from server.");
                System.exit(0);
            }
        }).start();
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            writer.println(message);
            chatArea.append("You: " + message + "\n");
            messageField.setText("");
        }
    }
}
