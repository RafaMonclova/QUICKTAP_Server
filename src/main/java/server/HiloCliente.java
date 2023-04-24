/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import bdd.Conexion;
import entidades.Categoria;
import entidades.Establecimiento;
import entidades.Rol;
import entidades.Usuario;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import message.Message;

public class HiloCliente implements Runnable {

    private static final int WAITING = 0;//Poner comentario de cada estado del protocolo
    private static final int LOGIN = 1;
    private static final int RUNNING = 2;
    private static final int EXIT = 3;

    private int estado;

    private Socket socketCliente;
    private Usuario usuario;

    Conexion conexion = new Conexion();

    public HiloCliente(Socket clientSocket) {
        this.socketCliente = clientSocket;
        this.usuario = null;
        estado = WAITING;
    }

    public Usuario getUsuario() {
        return usuario;
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
                System.out.println("ACCIÓN: " + peticion.getRequestAction());
                System.out.println("PARÁMETROS: " + peticion.getData());
                System.out.println(this.usuario+" "+estado);
                //LOGIN
                //LOGIN
                //rafa@gmail.com,1234
                switch (estado) {

                    case WAITING:

                        switch (peticion.getRequestType()) {

                            case "CONEXION":
                                ArrayList<Object> datos = new ArrayList<>();
                                datos.add("Conectado al servidor");
                                Message respuesta = new Message("CONEXION", "CONEXION", datos);
                                out.writeObject(respuesta);

                                estado = LOGIN;

                            break;

                        }

                        break;
                    case LOGIN:

                        switch (peticion.getRequestType()) {

                            case "LOGIN":

                                switch (peticion.getRequestAction()) {

                                    case "LOGIN":

                                        String correo = (String) peticion.getData().get(0);
                                        String passw = (String) peticion.getData().get(1);

                                        synchronized (this) {
                                            Usuario usuario = conexion.getUsuario(correo, passw);

                                            System.out.println(usuario);

                                            //Si no es null, significa que se encontró un usuario con esa combinación de correo y contraseña
                                            if (usuario != null) {
                                                ArrayList<Object> datos = new ArrayList<Object>();
                                                datos.add(true);
                                                datos.add(usuario.getNombre());

                                                ArrayList<String> listaRoles = new ArrayList<>();

                                                for (Rol r : usuario.getRols()) {
                                                    listaRoles.add(r.getNombre());
                                                }

                                                datos.add(listaRoles);

                                                Message respuesta = new Message("LOGIN", "LOGIN", datos);
                                                out.writeObject(respuesta);

                                                //Se asigna al cliente el usuario con el que logea
                                                setUsuario(usuario);
                                                estado = RUNNING;
                                                System.out.println("ok");

                                            } //Si no, es que los datos no son correctos y no se encontró ningún usuario
                                            else {
                                                ArrayList<Object> datos = new ArrayList<Object>();
                                                datos.add(false);
                                                Message respuesta = new Message("LOGIN", "LOGIN", datos);
                                                out.writeObject(respuesta);
                                            }
                                        }

                                        break;
                                    

                                }

                                break;

                            case "SALIR":

                                estado = EXIT;

                                break;

                        }

                        break;
                    case RUNNING:

                        switch (peticion.getRequestType()) {

                            case "SERVIDOR":

                                switch (peticion.getRequestAction()) {

                                    case "DATOS_SERVIDOR":
                                        
                                        synchronized (this) {
                                            ArrayList<Object> datos_server_query = new ArrayList<Object>();

                                            int nuevosClientes = Server.getNuevosClientes();
                                            String ultimoAlta = conexion.getUltimoCliente();
                                            datos_server_query.add(nuevosClientes);
                                            datos_server_query.add(ultimoAlta);

                                            int usuariosConectados = Server.getListaClientes().size();
                                            InetAddress localAddress = InetAddress.getLocalHost();
                                            String ipServidor = localAddress.getHostAddress();
                                            datos_server_query.add(usuariosConectados);
                                            datos_server_query.add(ipServidor);

                                            long totalClientes = conexion.getTotalClientes();
                                            String fechaRecarga = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
                                            datos_server_query.add(totalClientes);
                                            datos_server_query.add(fechaRecarga);

                                            int numExcepciones = conexion.getExcepciones().size();
                                            datos_server_query.add(numExcepciones);

                                            Message respuesta_server_query = new Message("SERVIDOR", "DATOS_SERVIDOR", datos_server_query);
                                            out.writeObject(respuesta_server_query);
                                            
                                        }

                                    break;

                                    //Devuelve los datos de los establecimientos del usuario que hace login
                                    //Desde el dashboard, el usuario puede elegir ver los datos del establecimiento elegido    
                                    case "DATOS_ESTABL":

                                        synchronized (this) {
                                            Usuario usuario = conexion.getUsuario((String) peticion.getData().get(0));

                                            ArrayList<Object> datos_establ_query = new ArrayList<Object>();

                                            double cajaHoy = 0;
                                            for (Establecimiento e : usuario.getEstablecimientos()) {
                                                System.out.println(e.getNombre());
                                                cajaHoy += conexion.getCajaHoy(e.getNombre());
                                            }

                                            datos_establ_query.add(cajaHoy);

                                            //Comprueba qué usuarios de los establecimientos están conectados al servidor
                                            int trabajadoresConectados = 0;
                                            for (Usuario u : conexion.getUsuarios(usuario.getEstablecimientos())) {

                                                for (HiloCliente h : Server.getListaClientes()) {

                                                    if (u.getNombre().equals(h.getUsuario().getNombre())) {

                                                        trabajadoresConectados++;

                                                    }

                                                }

                                            }

                                            datos_establ_query.add(trabajadoresConectados);

                                            long totalTrabajadores = conexion.getUsuarios(usuario.getEstablecimientos()).size();
                                            String fechaRecarga = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
                                            datos_establ_query.add(totalTrabajadores);
                                            datos_establ_query.add(fechaRecarga);

                                            long totalPedidos = conexion.getNumPedidos("TODOS");
                                            datos_establ_query.add(totalPedidos);
                                            long pedidosFin = 1;//conexion.getNumPedidos("FINALIZADOS");
                                            datos_establ_query.add(pedidosFin);
                                            long pedidosPen = 1;//conexion.getNumPedidos("PENDIENTES");
                                            datos_establ_query.add(pedidosPen);

                                            Message respuesta_establ_query = new Message("SERVIDOR", "DATOS_ESTABL", datos_establ_query);
                                            out.writeObject(respuesta_establ_query);
                                        }
                                    break; 
                                    
                                    case "LOGOUT":

                                        String usuario_logout = (String) peticion.getData().get(0);
                                        boolean sesion_cerrada = false;

                                        synchronized (this) {

                                            for (HiloCliente cliente : Server.getListaClientes()) {

                                                if (cliente.getUsuario().getNombre().equals(usuario_logout)) {
                                                    System.out.println(usuario_logout + " ha cerrado sesión");
                                                    setUsuario(null); //El usuario vuelve a ser null, ya que no ha iniciado sesión
                                                    sesion_cerrada = true;
                                                    break;
                                                }

                                            }

                                            System.out.println(usuario);

                                            //Se cierra sesión sin errores
                                            if (sesion_cerrada) {
                                                ArrayList<Object> datos = new ArrayList<Object>();
                                                datos.add(true);
                                                datos.add(usuario_logout);
                                                Message respuesta = new Message("LOGIN", "LOGOUT", datos);
                                                out.writeObject(respuesta);
                                                
                                                estado = LOGIN;

                                            } else {
                                                ArrayList<Object> datos = new ArrayList<Object>();
                                                datos.add(false);
                                                Message respuesta = new Message("LOGIN", "LOGOUT", datos);
                                                out.writeObject(respuesta);
                                            }
                                        }

                                    break;
                                }

                            break;
                                

                            case "USUARIO":

                                switch (peticion.getRequestAction()) {

                                    case "GET_PROPIETARIOS":
                                        
                                        synchronized (this) {
                                            ArrayList<Usuario> propList = conexion.getUsuarios("propietario");

                                            //Lista de "usuarios"
                                            ArrayList<Object> datos_prop_query = new ArrayList<Object>();

                                            //Recorre la lista de usuarios de la base de datos
                                            //Por cada usuario, se crea una lista para añadir sus campos, y se añade a la lista inicial que se envía
                                            //al cliente.
                                            for (Usuario u : propList) {

                                                ArrayList<Object> camposUsuario = new ArrayList<>();
                                                camposUsuario.add(u.getNombre());
                                                camposUsuario.add(u.getCorreo());
                                                camposUsuario.add(u.getPassw());

                                                ArrayList<String> rolesUsuario = new ArrayList<>();

                                                for (Rol r : u.getRols()) {
                                                    rolesUsuario.add(r.getNombre());
                                                }

                                                camposUsuario.add(rolesUsuario);

                                                ArrayList<String> establUsuario = new ArrayList<>();

                                                for (Establecimiento e : u.getEstablecimientos()) {
                                                    establUsuario.add(e.getNombre());
                                                }

                                                camposUsuario.add(establUsuario);

                                                datos_prop_query.add(camposUsuario);

                                            }

                                            Message respuesta_prop_query = new Message("USUARIO", "GET_PROPIETARIOS", datos_prop_query);
                                            out.writeObject(respuesta_prop_query);
                                        }

                                    break;

                                    case "GET_DATOS_USUARIO":
                                        
                                        synchronized (this) {

                                            Usuario usuarioModificar = conexion.getUsuario((String) peticion.getData().get(0));
                                            ArrayList<Object> datos_user_query = new ArrayList<Object>();
                                            datos_user_query.add(usuarioModificar.getNombre());
                                            datos_user_query.add(usuarioModificar.getCorreo());
                                            datos_user_query.add(usuarioModificar.getPassw());

                                            ArrayList<String> listaRoles = new ArrayList<String>();

                                            for (Rol r : usuarioModificar.getRols()) {
                                                listaRoles.add(r.getNombre());
                                            }

                                            datos_user_query.add(listaRoles);

                                            //Si contiene el rol propietario o trabajador, tambíen se añaden sus establecimientos
                                            if (listaRoles.contains("propietario") || listaRoles.contains("trabajador")) {
                                                ArrayList<String> listaEstabl = new ArrayList<String>();

                                                for (Establecimiento e : usuarioModificar.getEstablecimientos()) {
                                                    listaEstabl.add(e.getNombre());
                                                }

                                                datos_user_query.add(listaEstabl);
                                            }

                                            System.out.println(usuarioModificar);
                                            Message respuesta_user_query = new Message("USUARIO", "GET_DATOS_USUARIO", datos_user_query);
                                            out.writeObject(respuesta_user_query);

                                        }

                                    break;

                                    case "ACTUALIZAR_ROLES_ESTABL":
                                        
                                        synchronized (this) {

                                            String usuario = (String) peticion.getData().get(0);
                                            ArrayList<String> roles = (ArrayList<String>) peticion.getData().get(1);
                                            ArrayList<String> establecimientos = (ArrayList<String>) peticion.getData().get(2);

                                            boolean resultadoActualizarUsuario = conexion.actualizarUsuario(usuario, roles, establecimientos);

                                            //Ejecución sin errores
                                            if (resultadoActualizarUsuario) {
                                                ArrayList<Object> datos_actualizar = new ArrayList<Object>();
                                                datos_actualizar.add(true);
                                                Message respuesta_actualizar = new Message("USUARIO", "ACTUALIZAR_ROLES_ESTABL", datos_actualizar);
                                                out.writeObject(respuesta_actualizar);
                                            } else {
                                                ArrayList<Object> datos_actualizar = new ArrayList<Object>();
                                                datos_actualizar.add(false);
                                                Message respuesta_actualizar = new Message("USUARIO", "ACTUALIZAR_ROLES_ESTABL", datos_actualizar);
                                                out.writeObject(respuesta_actualizar);
                                            }

                                        }

                                    break;

                                    case "ACTUALIZAR_DATOS":
                                        
                                        synchronized (this) {

                                            Usuario usuario = conexion.getUsuario((String) peticion.getData().get(0));

                                            boolean resultadoActualizarUsuario = conexion.actualizarUsuario((String) peticion.getData().get(0), (String) peticion.getData().get(1), (String) peticion.getData().get(2), (String) peticion.getData().get(3));

                                            //Ejecución sin errores
                                            if (resultadoActualizarUsuario) {
                                                ArrayList<Object> datos_actualizar = new ArrayList<Object>();
                                                datos_actualizar.add(true);
                                                Message respuesta_actualizar = new Message("USUARIO", "ACTUALIZAR_DATOS", datos_actualizar);
                                                out.writeObject(respuesta_actualizar);
                                            } else {
                                                ArrayList<Object> datos_actualizar = new ArrayList<Object>();
                                                datos_actualizar.add(false);
                                                Message respuesta_actualizar = new Message("USUARIO", "ACTUALIZAR_DATOS", datos_actualizar);
                                                out.writeObject(respuesta_actualizar);
                                            }

                                        }

                                    break;

                                    case "INSERTAR":
                                        
                                        synchronized (this) {
                                            //ROLES DEL USUARIO
                                            ArrayList<String> roles = (ArrayList<String>) peticion.getData().get(3);

                                            //USUARIO TRABAJADOR O PROPIETARIO
                                            if (roles.contains("propietario") || roles.contains("trabajador")) {

                                                boolean id_usuario = conexion.insertarUsuario((String) peticion.getData().get(0), (String) peticion.getData().get(1), (String) peticion.getData().get(2), (ArrayList<String>) peticion.getData().get(3), (ArrayList<String>) peticion.getData().get(4));

                                                //Ejecución sin errores
                                                if (id_usuario) {
                                                    ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                    datos_insert.add(true);
                                                    Message respuesta_insert = new Message("USUARIO", "INSERTAR", datos_insert);
                                                    out.writeObject(respuesta_insert);
                                                } //Si es != 0, significa que ocurrió un error
                                                else {
                                                    ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                    datos_insert.add(false);
                                                    Message respuesta_insert = new Message("USUARIO", "INSERTAR", datos_insert);
                                                    out.writeObject(respuesta_insert);
                                                }

                                            } //OTRO USUARIO
                                            else {
                                                boolean resultadoInsertarUsuario = conexion.insertarUsuario((String) peticion.getData().get(0), (String) peticion.getData().get(1), (String) peticion.getData().get(2), (ArrayList<String>) peticion.getData().get(3));

                                                //Ejecución sin errores
                                                if (resultadoInsertarUsuario) {
                                                    ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                    datos_insert.add(true);
                                                    Message respuesta_insert = new Message("USUARIO", "INSERTAR", datos_insert);
                                                    out.writeObject(respuesta_insert);
                                                } else {
                                                    ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                    datos_insert.add(false);
                                                    Message respuesta_insert = new Message("USUARIO", "INSERTAR", datos_insert);
                                                    out.writeObject(respuesta_insert);
                                                }
                                            }
                                        }

                                    break;
                                }

                            break;
                            
                            case "ROL":

                                switch (peticion.getRequestAction()) {

                                    case "GET_ROLES":
                                        synchronized (this) {
                                            ArrayList<String> roleList = conexion.getRoles();
                                            ArrayList<Object> datos_role_query = new ArrayList<Object>();
                                            datos_role_query.add(roleList);
                                            Message respuesta_role_query = new Message("ROL", "GET_ROLES", datos_role_query);
                                            out.writeObject(respuesta_role_query);
                                        }
                                    break;

                                }

                            break;
                            
                            case "ESTABLECIMIENTO":

                                switch (peticion.getRequestAction()) {

                                    case "GET_ESTABLECIMIENTOS":
                                        
                                        synchronized (this) {
                                            ArrayList<String> establList = conexion.getEstablecimientos();
                                            ArrayList<Object> datos_establ_query = new ArrayList<Object>();
                                            datos_establ_query.add(establList);
                                            Message respuesta_establ_query = new Message("ESTABLECIMIENTO", "GET_ESTABLECIMIENTOS", datos_establ_query);
                                            out.writeObject(respuesta_establ_query);
                                        }

                                    break;
                                        
                                    case "GET_ESTABLECIMIENTOS_USUARIO":
                                        
                                        String usuario = (String) peticion.getData().get(0);
                                        
                                        synchronized (this) {
                                            
                                            ArrayList<String> establList = conexion.getEstablecimientos(usuario);
                                            ArrayList<Object> datos_establ_query = new ArrayList<Object>();
                                            datos_establ_query.add(establList);
                                            Message respuesta_establ_query = new Message("ESTABLECIMIENTO", "GET_ESTABLECIMIENTOS", datos_establ_query);
                                            out.writeObject(respuesta_establ_query);
                                        }

                                    break;    

                                    case "INSERTAR":
                                        
                                        synchronized (this) {

                                            boolean resultadoInsertarEstabl = conexion.insertarEstabl((String) peticion.getData().get(0), (String) peticion.getData().get(1), (String) peticion.getData().get(2));

                                            //Ejecución sin errores
                                            if (resultadoInsertarEstabl) {
                                                ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                datos_insert.add(true);
                                                Message respuesta_insert = new Message("ESTABLECIMIENTO", "INSERTAR", datos_insert);
                                                out.writeObject(respuesta_insert);

                                                Server.añadirNuevoCliente(); //Se incrementa el contador con los nuevos clientes registrados del día

                                            } else {
                                                ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                datos_insert.add(false);
                                                Message respuesta_insert = new Message("ESTABLECIMIENTO", "INSERTAR", datos_insert);
                                                out.writeObject(respuesta_insert);
                                            }
                                        }

                                    break;

                                }

                            break;
                            
                            case "CATEGORIA":

                                switch (peticion.getRequestAction()) {

                                    case "GET_CATEGORIAS":
                                        
                                        synchronized (this) {
                                            String establecimiento = (String) peticion.getData().get(0);
                                            ArrayList<Categoria> categorias = conexion.getCategorias(establecimiento);

                                            System.out.println(categorias);
                                            ArrayList<Object> datos_categories_query = new ArrayList<Object>();

                                            for (Categoria c : categorias) {
                                                datos_categories_query.add(c.getNombre());
                                            }

                                            Message respuesta_categories_query = new Message("CATEGORIA", "GET_CATEGORIAS", datos_categories_query);
                                            out.writeObject(respuesta_categories_query);
                                        }

                                    break;

                                    case "INSERTAR":
                                        
                                        synchronized (this) {

                                            String nombreCategoria = (String) peticion.getData().get(0);
                                            String descripcionCategoria = (String) peticion.getData().get(1);
                                            Establecimiento establCategoria = conexion.getEstablecimiento((String) peticion.getData().get(2));
                                            System.out.println(establCategoria);

                                            boolean resultadoInsertarCategoria = conexion.insertarCategoria(nombreCategoria, descripcionCategoria, establCategoria);

                                            //Ejecución sin errores
                                            if (resultadoInsertarCategoria) {
                                                ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                datos_insert.add(true);
                                                Message respuesta_insert = new Message("CATEGORIA","INSERTAR", datos_insert);
                                                out.writeObject(respuesta_insert);

                                            } else {
                                                ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                datos_insert.add(false);
                                                Message respuesta_insert = new Message("CATEGORIA","INSERTAR", datos_insert);
                                                out.writeObject(respuesta_insert);
                                            }
                                        }

                                    break;

                                }

                            break;
                            
                            case "PRODUCTO":
                                
                                switch(peticion.getRequestAction()){
                                    
                                    case "INSERTAR":
                                        
                                        synchronized (this) {

                                            String nombreProducto = (String) peticion.getData().get(0);
                                            String descripcionProducto = (String) peticion.getData().get(1);
                                            Establecimiento establProducto = conexion.getEstablecimiento((String) peticion.getData().get(2));
                                            double precio = (double) peticion.getData().get(3);
                                            int stock = (int) peticion.getData().get(4);
                                            ArrayList<String> categorias = (ArrayList<String>) peticion.getData().get(5);
                                            //System.out.println(establCategoria);

                                            boolean resultadoInsertarProducto = conexion.insertarProducto(nombreProducto, descripcionProducto, precio,stock,null,categorias,establProducto);

                                            //Ejecución sin errores
                                            if (resultadoInsertarProducto) {
                                                ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                datos_insert.add(true);
                                                Message respuesta_insert = new Message("CATEGORIA","INSERTAR", datos_insert);
                                                out.writeObject(respuesta_insert);

                                            } else {
                                                ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                datos_insert.add(false);
                                                Message respuesta_insert = new Message("CATEGORIA","INSERTAR", datos_insert);
                                                out.writeObject(respuesta_insert);
                                            }
                                        }
                                        
                                    break;
                                    
                                }
                                
                            break;
                            
                            case "PEDIDO":
                                break;

                        }

                    break;
                    
                    case EXIT:
                        System.out.println("Adios");
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
