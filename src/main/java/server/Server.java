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
    private static List<HiloCliente> listaClientes = new ArrayList<>();
    
    Map<HiloCliente, List<Exception>> exceptionsMap = new HashMap<>();

    private static int contadorNuevosClientes = 0;
    private static LocalDate fechaUltimoAlta = null;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            //Inicializa la tarea de comprobación, que reiniciará el contador de nuevos clientes a las 0:00
            CounterResetApp app = new CounterResetApp();
            app.startCounterResetTask();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());
                HiloCliente hiloCliente = new HiloCliente(clientSocket);
                Thread thread = new Thread(hiloCliente);
                thread.start();
                agregarHiloCliente(hiloCliente);
                mostrarClientesConectados();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //Agrega un hilo de cliente a la lista
    public static synchronized void agregarHiloCliente(HiloCliente hiloCliente) {
        listaClientes.add(hiloCliente);
    }

    //Elimina un hilo de cliente de la lista
    public static synchronized void eliminarHiloCliente(HiloCliente hiloCliente) {
        listaClientes.remove(hiloCliente);
    }
    
    //Muestra el número de clientes activos
    public static synchronized void mostrarClientesConectados(){
        System.out.println("Num Clientes: "+listaClientes.size());
    }
    
    //Devuelve el listado de hilos de clientes activos
    public static synchronized List<HiloCliente> getListaClientes(){
        return listaClientes;
    }

    //Devuelve el contador de clientes nuevos
    public static synchronized int getNuevosClientes(){
        return contadorNuevosClientes;
    }

    //Incrementa el contador de clientes nuevos
    public static synchronized void añadirNuevoCliente(){
        contadorNuevosClientes++;
    }

    //Devuelve la fecha del último alta de cliente
    public static synchronized LocalDate getUltimaFechaAlta(){
        return fechaUltimoAlta;
    }

    //Establece la nueva fecha de último alta de cliente
    public static synchronized void setUltimaFechaAlta(LocalDate fecha){
        fechaUltimoAlta = fecha;
    }

    //Ejecuta la tarea de comprobación cada minuto
    private void startCounterResetTask() {
        CounterResetTask task = new CounterResetTask();

        // Programar la tarea para que se ejecute cada minuto
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(task, 0, 60 * 1000);
    }
}
