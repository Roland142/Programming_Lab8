package core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;


public class Connection {

    private static final int PORT = Integer.parseInt(
            System.getenv("PORT") != null ? System.getenv("PORT") : "1111");

    public static SocketChannel connect() {
        try {
            SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress("localhost", PORT));

            long deadline = System.currentTimeMillis() + 5000;
            while (!channel.finishConnect()) {
                if (System.currentTimeMillis() > deadline) {
                    channel.close();
                    System.out.println("Таймаут подключения к серверу.");
                    return null;
                }
            }
            return channel;
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
            return null;
        }
    }

    /** Пытается переподключиться 3 раза с паузой 2 секунды между попытками. */
    public static SocketChannel reconnect() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.println("Попытка " + attempt + "/3...");
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            SocketChannel channel = connect();
            if (channel != null) {
                return channel;
            }
        }
        return null;
    }

    public static int getPort() { return PORT; }
}
