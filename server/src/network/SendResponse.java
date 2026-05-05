package network;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/** Модуль 4 — сериализация и отправка Response клиенту. */
public class SendResponse {

    public static void send(SocketChannel channel, Response response) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(response);
        oos.flush();
        ByteBuffer buf = ByteBuffer.wrap(baos.toByteArray());
        while (buf.hasRemaining()) {
            channel.write(buf);
        }
    }
}
