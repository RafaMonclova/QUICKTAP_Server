/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import bdd.Conexion;
import entidades.Usuario;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.Message;

public class HiloCliente implements Runnable {

    private Socket socketCliente;
    //private List<Thread> listaClientes;
    private Usuario usuario;

    Conexion conexion = new Conexion();

    public HiloCliente(Socket clientSocket) {
        this.socketCliente = clientSocket;
        //this.listaClientes = clientList;
        this.usuario = null;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public void run() {
        try {

            ObjectInputStream in = new ObjectInputStream(socketCliente.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(socketCliente.getOutputStream());

            while (true) {
                Message peticion = (Message) in.readObject();
                if (peticion == null) {
                    //listaClientes.remove(this);
                    System.out.println("Cliente desconectado: " + socketCliente.getInetAddress().getHostAddress());
                    break;
                }
                System.out.println("----------------------------------------------------");
                System.out.println("Petición desde: " + socketCliente.getInetAddress().getHostAddress());
                System.out.println("Usuario: " + usuario);
                System.out.println("TIPO DE PETICIÓN: " + peticion.getRequestType());
                System.out.println("PARÁMETROS: " + peticion.getData());

                switch (peticion.getRequestType()) {

                    case "LOGIN":

                        String mail = (String) peticion.getData().get(0);
                        String password = (String) peticion.getData().get(1);

                        synchronized (this) {
                            Usuario usuario = conexion.getUsuario(mail, password);

                            System.out.println(usuario);

                            //Si no es null, significa que se encontró un usuario con esa combinación de correo y contraseña
                            if (usuario != null) {
                                ArrayList<Object> datos = new ArrayList<Object>();
                                datos.add(true);
                                datos.add(usuario.getNombre());
                                Message respuesta = new Message("LOGIN", datos);
                                out.writeObject(respuesta);

                                //Se asigna al cliente el usuario con el que logea
                                setUsuario(usuario);

                            } //Si no, es que los datos no son correctos y no se encontró ningún usuario
                            else {
                                ArrayList<Object> datos = new ArrayList<Object>();
                                datos.add(false);
                                Message respuesta = new Message("LOGIN", datos);
                                out.writeObject(respuesta);
                            }
                        }

                        break;
                    case "ROLE_QUERY":

                        synchronized (this) {
                            ArrayList<String> roleList = conexion.getRoles();
                            ArrayList<Object> datos_role_query = new ArrayList<Object>();
                            datos_role_query.add(roleList);
                            Message respuesta_role_query = new Message("ROLE_QUERY", datos_role_query);
                            out.writeObject(respuesta_role_query);
                        }

                        break;

                    case "ESTABL_QUERY":

                        synchronized (this) {
                            ArrayList<String> establList = conexion.getEstablecimientos();
                            ArrayList<Object> datos_establ_query = new ArrayList<Object>();
                            datos_establ_query.add(establList);
                            Message respuesta_establ_query = new Message("ESTABL_QUERY", datos_establ_query);
                            out.writeObject(respuesta_establ_query);
                        }

                        break;

                    case "INSERT_USER":

                        
                        synchronized (this) {
                            //ROLES DEL USUARIO
                            ArrayList<String> roles = (ArrayList<String>) peticion.getData().get(3);

                            //USUARIO TRABAJADOR O PROPIETARIO
                            if (roles.get(0).equals("propietario") || roles.get(0).equals("trabajador")) {

                                boolean id_usuario = conexion.insertarUsuario((String) peticion.getData().get(0), (String) peticion.getData().get(1), (String) peticion.getData().get(2), (ArrayList<String>) peticion.getData().get(3), (ArrayList<String>) peticion.getData().get(4));

                                //Ejecución sin errores
                                if (id_usuario) {
                                    ArrayList<Object> datos_insert = new ArrayList<Object>();
                                    datos_insert.add(true);
                                    Message respuesta_insert = new Message("INSERT_USER", datos_insert);
                                    out.writeObject(respuesta_insert);
                                } //Si es != 0, significa que ocurrió un error
                                else {
                                    ArrayList<Object> datos_insert = new ArrayList<Object>();
                                    datos_insert.add(false);
                                    Message respuesta_insert = new Message("INSERT_USER", datos_insert);
                                    out.writeObject(respuesta_insert);
                                }

                            } //OTRO USUARIO
                            else {
                                boolean resultadoInsertarUsuario = conexion.insertarUsuario((String) peticion.getData().get(0), (String) peticion.getData().get(1), (String) peticion.getData().get(2), (ArrayList<String>) peticion.getData().get(3));

                                //Ejecución sin errores
                                if (resultadoInsertarUsuario) {
                                    ArrayList<Object> datos_insert = new ArrayList<Object>();
                                    datos_insert.add(true);
                                    Message respuesta_insert = new Message("INSERT_USER", datos_insert);
                                    out.writeObject(respuesta_insert);
                                } else {
                                    ArrayList<Object> datos_insert = new ArrayList<Object>();
                                    datos_insert.add(false);
                                    Message respuesta_insert = new Message("INSERT_USER", datos_insert);
                                    out.writeObject(respuesta_insert);
                                }
                            }
                        }

                        break;

                    case "INSERT_ESTABL":

                        synchronized (this) {
                            boolean resultadoInsertarEstabl = conexion.insertarEstabl((String) peticion.getData().get(0), (String) peticion.getData().get(1), (String) peticion.getData().get(2));

                            //Ejecución sin errores
                            if (resultadoInsertarEstabl) {
                                ArrayList<Object> datos_insert = new ArrayList<Object>();
                                datos_insert.add(true);
                                Message respuesta_insert = new Message("INSERT_ESTABL", datos_insert);
                                out.writeObject(respuesta_insert);
                            } else {
                                ArrayList<Object> datos_insert = new ArrayList<Object>();
                                datos_insert.add(false);
                                Message respuesta_insert = new Message("INSERT_ESTABL", datos_insert);
                                out.writeObject(respuesta_insert);
                            }
                        }

                        break;

                }

            }

            socketCliente.close();
        } catch (IOException e) {
            if (usuario != null) {
                System.out.println("Cliente desconectado: " + usuario.getNombre() + "@" + socketCliente.getInetAddress().getHostAddress());
            } else {
                System.out.println("Cliente desconectado: " + socketCliente.getInetAddress().getHostAddress());
            }
            //Server.eliminarHiloCliente(Thread.currentThread());
            Server.eliminarHiloCliente(this);
            Server.mostrarClientesConectados();
            //e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
