/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import entidades.Rol;
import utilidades.CounterResetTask;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

/**
 * Clase principal del servidor
 * @author rafam
 */
public class Server {

    
    private static final int PUERTO = 4444; //Puerto donde escucha las peticiones
    private static List<HiloCliente> listaClientes = new ArrayList<>(); //Usuarios conectados
    private static List<SesionCliente> sesiones = new ArrayList<>(); //Sesiones de clientes abiertas
    private static int contadorNuevosClientes = 0; //Contador de nuevos clientes

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PUERTO);
            System.out.println("Servidor iniciado en el puerto " + PUERTO);

            //Inicializa la tarea de comprobación, que reiniciará el contador de nuevos clientes a las 0:00
            iniciarContador();
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress().getHostAddress());
                
                HiloCliente hiloCliente = new HiloCliente(clientSocket);
                Thread thread = new Thread(hiloCliente);
                thread.start();
                agregarHiloCliente(hiloCliente); //Añade el hilo del cliente a la colección
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Añade un hilo de cliente a la colección del servidor
     * @param hiloCliente El hilo del cliente a añadir
     */
    public static synchronized void agregarHiloCliente(HiloCliente hiloCliente) {
        listaClientes.add(hiloCliente);
    }

    /**
     * Elimina un hilo de cliente de la colección del servidor
     * @param hiloCliente El hilo del cliente a eliminar
     */
    public static synchronized void eliminarHiloCliente(HiloCliente hiloCliente) {
        listaClientes.remove(hiloCliente);
        
        //Busca si es invitado de alguna sesión, y lo borra del listado de invitados
        for(SesionCliente sesion : getSesiones()){
            
            if(sesion.getInvitados().contains(hiloCliente)){
                
                sesion.getInvitados().remove(hiloCliente);
                break;
                
            }
            
        }
        
    }
    
    /**
     * Muestra el número de clientes conectados al servidor
     */
    public static synchronized void mostrarClientesConectados(){
        System.out.println("Num Clientes: "+listaClientes.size());
    }
    
    /**
     * Devuelve el listado de hilos de cliente conectados al servidor
     * @return La lista de hilos de cliente
     */
    public static synchronized List<HiloCliente> getListaClientes(){
        return listaClientes;
    }
    
    /**
     * Añade una sesión de cliente a la colección del servidor
     * @param sesionCliente La sesión a añadir
     */
    public static synchronized void agregarSesion(SesionCliente sesionCliente) {
        sesiones.add(sesionCliente);
    }

    /**
     * Elimina una sesión de cliente a la colección del servidor
     * @param sesionCliente La sesión a eliminar
     */
    public static synchronized void eliminarSesion(SesionCliente sesionCliente) {
        sesiones.remove(sesionCliente);
    }
    
    /**
     * Muestra el número de sesiones activas
     */
    public static synchronized void mostrarSesiones(){
        System.out.println("Num Sesiones: "+sesiones.size());
    }
    
    /**
     * Devuelve el listado de sesiones activas
     * @return La lista de sesiones de cliente
     */
    public static synchronized List<SesionCliente> getSesiones(){
        return sesiones;
    }

    
    /**
     * Devuelve el contador de clientes nuevos
     * @return El contador de clientes nuevos
     */
    public static synchronized int getNuevosClientes(){
        return contadorNuevosClientes;
    }

    /**
     * Incrementa el contador de clientes nuevos
     */
    public static synchronized void añadirNuevoCliente(){
        contadorNuevosClientes++;
    }

    /**
     * Ejecuta la tarea de comprobación cada minuto
     */
    private static void iniciarContador() {
        CounterResetTask tarea = new CounterResetTask(contadorNuevosClientes);

        //Programar la tarea para que se ejecute cada minuto
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(tarea, 0, 60 * 1000);
    }
}
