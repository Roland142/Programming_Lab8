package network;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/** Модуль 2 — чтение и десериализация Request из канала клиента. */
public class ReadRequest {

    public static Request read(SocketChannel channel) throws IOException, ClassNotFoundException {
        ByteBuffer buffer = ByteBuffer.allocate(65536);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        long deadline = System.currentTimeMillis() + 5000;

        while (System.currentTimeMillis() < deadline) {
            buffer.clear();
            int bytesRead = channel.read(buffer);

            if (bytesRead == -1) throw new IOException("Клиент закрыл соединение");

            if (bytesRead > 0) {
                baos.write(buffer.array(), 0, bytesRead);
                try {
                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    return (Request) ois.readObject();
                } catch (IOException ignored) {}
            }
        }

        return null;
    }
}
