package ru.matyunin.inno.homework11.client;

import java.net.*;
import java.io.*;
import java.util.Scanner;

/**
 * @author Артём Матюнин
 * реализует ввод-вывод сообщений, взаимодействие с сервером
 */

public class ChatClient {

    private Socket socket;

    public ChatClient(String addres, int port) {
        try {
            this.socket = new Socket(addres, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //запустим потоки
    public void clientStart() {
        new ReadMsg().start();
        new WriteMsg().start();
    }

    //закроем сокет и пометим потоки на закрытие
    private void shutDownClient() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                WriteMsg.currentThread().interrupt();
                ReadMsg.currentThread().interrupt();
            }
        } catch (IOException ignored) {
        }
    }

    // читаем сообщения с сервера
    private class ReadMsg extends Thread {
        @Override
        public void run() {

            String str;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                while (true) {
                    str = in.readLine();
                    if (str.equals("quit")) {
                        ChatClient.this.shutDownClient();
                        break;
                    }
                    System.out.println(str);
                }
            } catch (IOException e) {
                ChatClient.this.shutDownClient();
            }
        }
    }

    // Отправляем сообщения на сервер
    private class WriteMsg extends Thread {

        @Override
        public void run() {

            String message;

            try (Scanner scanner = new Scanner(System.in);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                System.out.println("Как вас зовут?");

                //первое сообщение - никнейм
                message = scanner.nextLine();
                out.println(message);
                while (!isInterrupted()) {
                    message = scanner.nextLine();
                    if (message.equals("quit")) {
                        out.println("quit");
                        ChatClient.this.shutDownClient();
                        break;
                    } else {
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
