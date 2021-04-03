package ru.matyunin.inno.homework10.chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Артём Матюнин
 * релаизует взаимодействие сервера и клиентов
 * */

public class ChatServer {

    private static final Set<ClientThread> clientThreadList = new HashSet<>();

    public void start(int port) {
        try {
            // запускаем сервер
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Сервер запущен. Ждем подключений.");

            // будем ожидать подключения клиентов
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("Новое подключение из "
                        + client.getInetAddress().getHostAddress()
                        + ":" + client.getPort());
                clientThreadList.add(new ClientThread(client));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // поток для клиента
    private static class ClientThread extends Thread {

        private final Socket player;

        //никнейм сохраняю, но не применяю (предполагал, что может пригодиться при unicast, но до реализации не дошел)
        private String nickname;
        private final BufferedReader fromClient;
        private final PrintWriter toClient;

        public ClientThread(Socket player) {
            this.player = player;
            try {
                this.fromClient = new BufferedReader(new InputStreamReader(player.getInputStream()));
                this.toClient = new PrintWriter(player.getOutputStream(), true);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
            start();
        }

        @Override
        public void run() {
            try {
                String messageFromClient;

                //считываем первое сообщение, как никнейм
                messageFromClient = fromClient.readLine();
                nickname = messageFromClient;
                toClient.println("Привет, " + messageFromClient + "!");

                //а дальше, пока поток живой, получаем и отправляем сообщения
                while (!isInterrupted()) {
                    messageFromClient = fromClient.readLine();
                    if (messageFromClient != null) {
                        System.out.println("Получили сообщение от "
                                + player.getInetAddress().getHostAddress() + ":"
                                + player.getPort() + " (" + nickname + ") - "
                                + messageFromClient);
                        String[] parsMessage = messageFromClient.split(" ");

                        //если получим quit от клиента, то завершаем работу с ним и оповестим других клиентов
                        if (parsMessage[0].equals("quit")) {
                            messageFromClient = closeSocket(parsMessage[0]);
                        }
                        for (ClientThread p : ChatServer.clientThreadList) {
                            p.toClient.println(this.nickname + " сказал: " + messageFromClient);
                        }
                    }
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        /**
         * Закрывам сокет и все потоки ввода-вывода поочередно, затем удаляем себя из общего списка потоков.
         * Если список участников пуст(то есть все удалились), то останавливаем сервер.
         * @param message получем никнейм, который передадим для оповещения других клиентов
         * */

        private String closeSocket(String message) {
            try {
                if (!this.player.isClosed()) {
                    this.player.close();
                    this.fromClient.close();
                    this.toClient.close();
                    ChatServer.clientThreadList.remove(this);
                    this.interrupt();
                    if (clientThreadList.isEmpty()) {
                        System.exit(0);
                    }
                }
            } catch (IOException ignored) {
            }

            return message + " покинул чат.";
        }
    }
}
