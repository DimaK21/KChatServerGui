package ru.kryu.kchat.kchatservergui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {


    private Socket socket;
    private ServerController server;
    private DataInputStream input;
    private DataOutputStream output;
    private String nick;

    ClientHandler(ServerController server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.input = new DataInputStream(socket.getInputStream());
            this.output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(() -> {
            String message = null;
            try {
                while (true ) {
                    String string = input.readUTF();
                    System.out.println("Сервер получил сообщение " + string);
                    if (string.startsWith("/auth")) {
                        String[] strings = string.split("\\s");
                        if ((strings.length == 3)) {
                            System.out.println("Сервер сделал массив для авторизации ");
                            String newNick = server.getAuthService().getNickByLoginAndPass(strings[1], strings[2]);
                            if (newNick != null) {
                                if (!server.isNickBusy(newNick)) {
                                    nick = newNick;
                                    sendMessage("/authok "+nick);
                                    System.out.println("Сервер отправляет /authok");
                                    server.subscribe(this);
                                    break;
                                } else {
                                    sendMessage("Учетная запись занята");
                                    System.out.println("Учетная запись занята");
                                }
                            } else {
                                sendMessage("Неверный логин или пароль");
                                System.out.println("Неверный логин или пароль");
                            }
                        }
                    }
                }
                while (true) {
                    message = input.readUTF();
                    System.out.println(message);
                    if (message.startsWith("/")) {
                        if (message.equals("/end")) {
                            System.out.println("Клиент " + socket.getInetAddress() + " " + socket.getPort() + " отключается");
                            sendMessage("/end");
                            break;
                        }
                        if (message.startsWith("/w ")) {
                            String[] strings = message.split("\\s", 3);
                            if (strings.length == 3) {
                                server.privateMessage(this, strings[1], strings[2]);
                            }
                        }
                    } else {
                        server.broadcastMessage(nick + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println("Клиент " + socket.getInetAddress() + " " + socket.getPort() + " отключается");
            } finally {
                nick = null;
                server.unsubscribe(this);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String getNick() {
        return nick;
    }

    public void sendMessage(String message) {
        try {
            output.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

}
