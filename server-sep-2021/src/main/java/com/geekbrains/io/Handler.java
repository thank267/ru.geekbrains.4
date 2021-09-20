package com.geekbrains.io;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Slf4j
public class Handler implements Runnable {

    private final Socket socket;

    public Handler(Socket socket) {
        this.socket = socket;
        IoIntro.createServerDir("root");
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        try (ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream is = new ObjectInputStream(socket.getInputStream())) {
            while (true) {
                File file = (File) is.readObject();

                File dst = new File(IoIntro.ROOT_DIR + file.getName());
                IoIntro.transfer(file, dst);

                os.writeObject(dst);
                os.flush();
            }
        } catch (Exception e) {
           log.error("stacktrace: ", e);
        }
    }
}
