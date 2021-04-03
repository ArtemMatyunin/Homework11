package ru.matyunin.inno.homework11.client;

public class Main {
    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient("localhost", 12345);
        chatClient.clientStart();
    }
}
