package ru.matyunin.inno.homework10.app;

import ru.matyunin.inno.homework10.chatserver.ChatServer;

public class Application {

    public static void main(String[] args){
        ChatServer gameServerTwo = new ChatServer();
        gameServerTwo.start(12345);
    }

}
