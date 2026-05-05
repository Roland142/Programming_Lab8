package network;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/** Читает байты из SocketChannel и десериализует Response. */
public class ResponseReceiver {

    public static Response receive(SocketChannel channel) {
        try {
            int bufferSize = Integer.parseInt(
                    System.getenv("BUFFER_SIZE") != null ? System.getenv("BUFFER_SIZE") : "65536");
            ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize);
            ByteBuffer recvBuf = ByteBuffer.allocate(bufferSize);
            long deadline = System.currentTimeMillis() + 5000;

            while (System.currentTimeMillis() < deadline) {
                recvBuf.clear();
                int n = channel.read(recvBuf);

                if (n == -1) return null;

                if (n > 0) {
                    baos.write(recvBuf.array(), 0, n);
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                         ObjectInputStream ois = new ObjectInputStream(bais)) {
                        return (Response) ois.readObject();
                    } catch (ClassNotFoundException e) {
                        System.out.println("Ошибка: неизвестный класс в ответе: " + e.getMessage());
                        return null;
                    } catch (IOException ignored) {}
                }
            }

            System.out.println("Таймаут: сервер не ответил за 5 секунд.");
            return null;

        } catch (IOException e) {
            return null;
        }
    }
}
