package network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/** Сериализует Request и отправляет на сервер через SocketChannel. */
public class RequestSender {

    public static boolean send(SocketChannel channel, Request request) {
        if (channel == null) return false;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(request);
            oos.flush();

            ByteBuffer sendBuf = ByteBuffer.wrap(baos.toByteArray());

            while (sendBuf.hasRemaining()) {
                channel.write(sendBuf);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
