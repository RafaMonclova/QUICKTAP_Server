/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final int PUERTO = 4444;
    private static List<Thread> listaClientes = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());
                Thread hiloCliente = new Thread(new HiloCliente(clientSocket, listaClientes));
                hiloCliente.start();
                agregarHiloCliente(hiloCliente);
                mostrarClientesConectados();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //Agrega un hilo de cliente a la lista
    public static synchronized void agregarHiloCliente(Thread hiloCliente) {
        listaClientes.add(hiloCliente);
    }

    //Elimina un hilo de cliente de la lista
    public static synchronized void eliminarHiloCliente(Thread hiloCliente) {
        listaClientes.remove(hiloCliente);
    }
    
    //Muestra el número de clientes activos
    public static synchronized void mostrarClientesConectados(){
        System.out.println("Num Clientes: "+listaClientes.size());
    }
}
