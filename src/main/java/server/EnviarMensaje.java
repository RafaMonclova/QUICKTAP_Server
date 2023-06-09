/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author rafam
 */
public class EnviarMensaje extends Thread {

    private ServerSocket serverSocket;

    public EnviarMensaje(int puerto) throws IOException {
        serverSocket = new ServerSocket(puerto);
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                // Aquí puedes enviar mensajes a todos los clientes conectados
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

