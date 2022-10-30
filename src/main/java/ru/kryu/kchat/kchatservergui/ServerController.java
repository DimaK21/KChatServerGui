package ru.kryu.kchat.kchatservergui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.Vector;

public class ServerController {
    @FXML
    private Label welcomeText;
    @FXML
    private TextArea textArea;
    @FXML
    private Button buttonConnect;


        private final Vector<ClientHandler> clients = new Vector<>();



    private final Vector<ClientHandler> allClients = new Vector<>();
        private AuthService authService;
        public ServerSocket serverSocket;

    public Vector<ClientHandler> getAllClients() {
        return allClients;
    }

    public void start(int port) {
        Thread t = new Thread(() -> {
            try  {
                serverSocket = new ServerSocket(port);
                System.out.println("Server started");
                authService = new AuthService();
                authService.connect();
                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Client connected " + socket.getInetAddress() + " " + socket.getPort());
                    allClients.add(new ClientHandler(this, socket));
                }
            }catch (SocketException e) {
                System.out.println("Server stopped");
            }catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException | ClassNotFoundException e) {
                System.out.println("Сервис авторизации не запущен");
                e.printStackTrace();
            } finally {
                try {
                    authService.disconnect();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();

        }

        public void stop(){
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (ClientHandler o: allClients) {
                try {
                    o.getSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public AuthService getAuthService() {
            return authService;
        }

        public void broadcastMessage(String message) {
            for (ClientHandler o : clients) {
                o.sendMessage(message);
            }
            System.out.println("Сервер делает рассылку всем " + message);
        }

        public void privateMessage(ClientHandler from, String toNick, String message) {
            for (ClientHandler o : clients) {
                if (o.getNick().equals(toNick)) {
                    o.sendMessage(from.getNick() + " to " + toNick + ": " + message);
                    from.sendMessage(from.getNick() + " to " + toNick + ": " + message);
                    break;
                }
            }
        }

        public void clientsListMessage() {
            StringBuilder stringBuilder = new StringBuilder("/clientslist ");
            textArea.clear();
            for (ClientHandler o : clients) {
                stringBuilder.append(o.getNick() + " ");
                textArea.appendText(o.getNick()+ " " +o.getSocket().getInetAddress() + " " + o.getSocket().getPort() +"\n");
            }
            String out = stringBuilder.toString();
            for (ClientHandler o : clients) {
                o.sendMessage(out);
            }
        }

        public void subscribe(ClientHandler clientHandler) {
            clients.add(clientHandler);
            System.out.println("Клиент в рассылке сервера");
            clientsListMessage();
        }

        public void unsubscribe(ClientHandler clientHandler) {
            clients.remove(clientHandler);
            System.out.println("Клиент вышел из рассылки сервера");
            clientsListMessage();
        }

        public boolean isNickBusy(String nick) {
            for (ClientHandler o : clients) {
                if (o.getNick().equals(nick)) return true;
            }
            return false;
        }



    @FXML
    protected void onButtonClick() {
        if (buttonConnect.getText().equals("Connect")) {

                start(9966);

            buttonConnect.setText("Disconnect");
        } else {
            stop();
            buttonConnect.setText("Connect");
        }
    }



}