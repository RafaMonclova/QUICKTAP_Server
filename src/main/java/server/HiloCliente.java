/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import bdd.Conexion;
import entidades.Categoria;
import entidades.Establecimiento;
import entidades.LineaPedido;
import entidades.LineaPedidoId;
import entidades.Pedido;
import entidades.Producto;
import entidades.Rol;
import entidades.Usuario;
import java.io.BufferedReader;
import java.io.File;
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
import utilidades.ObservadorSesion;

public class HiloCliente implements Runnable, ObservadorSesion {

    private static final int WAITING = 0; //Estado inicial. Acepta peticiones de logeo o salida
    private static final int RUNNING = 2; //Estado donde el usuario puede enviar peticiones al servidor para comunicarse con la BDD.
    private static final int EXIT = 3; //Estado final. Se ha cerrado la conexión del cliente en el servidor.

    private int estado;

    private Socket socketCliente;
    private Usuario usuario;
    private Establecimiento establecimientoActual;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private SesionCliente sesion;

    Conexion conexion = new Conexion();

    public HiloCliente(Socket clientSocket) {
        this.socketCliente = clientSocket;

        this.usuario = null;
        this.establecimientoActual = null;
        sesion = null;

        estado = WAITING;

    }

    public Socket getSocket() {
        return socketCliente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Establecimiento getEstablecimientoActual() {
        return establecimientoActual;
    }

    public void setEstablecimientoActual(Establecimiento establecimientoActual) {
        this.establecimientoActual = establecimientoActual;
    }

    @Override
    public void run() {
        try {

            in = new ObjectInputStream(socketCliente.getInputStream());
            out = new ObjectOutputStream(socketCliente.getOutputStream());

            while (estado != EXIT) {
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

                //LOGIN
                //LOGIN
                //rafa@gmail.com,1234
                switch (estado) {

                    case WAITING:

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

                                                //notificarClientes();
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

                                    case "LOGIN_ANDROID":

                                        String correoMovil = (String) peticion.getData().get(0);
                                        String passwMovil = (String) peticion.getData().get(1);

                                        synchronized (this) {
                                            Usuario usuario = conexion.getUsuario(correoMovil, passwMovil);

                                            System.out.println(usuario);

                                            //Si no es null, significa que se encontró un usuario con esa combinación de correo y contraseña
                                            if (usuario != null) {

                                                ArrayList<String> listaRoles = new ArrayList<>();
                                                for (Rol r : usuario.getRols()) {
                                                    listaRoles.add(r.getNombre());
                                                }

                                                if (!listaRoles.contains("cliente")) {
                                                    System.out.println("entra1");
                                                    ArrayList<Object> datos = new ArrayList<Object>();
                                                    datos.add(false);
                                                    Message respuesta = new Message("LOGIN", "LOGIN_ANDROID", datos);
                                                    out.writeObject(respuesta);
                                                } else {
                                                    System.out.println("entra2");
                                                    ArrayList<Object> datos = new ArrayList<Object>();
                                                    datos.add(true);
                                                    datos.add(usuario.getNombre());

                                                    datos.add(listaRoles);

                                                    Message respuesta = new Message("LOGIN", "LOGIN_ANDROID", datos);
                                                    out.writeObject(respuesta);

                                                    //Se asigna al cliente el usuario con el que logea
                                                    setUsuario(usuario);

                                                    //notificarAmigos();
                                                    //enviarAmigos();
                                                    //notificarClientes();
                                                    estado = RUNNING;
                                                    System.out.println("ok");
                                                }

                                            } //Si no, es que los datos no son correctos y no se encontró ningún usuario
                                            else {
                                                ArrayList<Object> datos = new ArrayList<Object>();
                                                datos.add(false);
                                                Message respuesta = new Message("LOGIN", "LOGIN_ANDROID", datos);
                                                out.writeObject(respuesta);
                                            }
                                        }

                                        break;

                                    case "LOGIN_ANDROID_TRABAJADORES":

                                        String correoMovilTrabaj = (String) peticion.getData().get(0);
                                        String passwMovilTrabaj = (String) peticion.getData().get(1);

                                        synchronized (this) {
                                            Usuario usuario = conexion.getUsuario(correoMovilTrabaj, passwMovilTrabaj);

                                            System.out.println(usuario);

                                            //Si no es null, significa que se encontró un usuario con esa combinación de correo y contraseña
                                            if (usuario != null) {

                                                ArrayList<String> listaRoles = new ArrayList<>();
                                                for (Rol r : usuario.getRols()) {
                                                    listaRoles.add(r.getNombre());
                                                }

                                                if (!listaRoles.contains("trabajador")) {
                                                    System.out.println("entra1");
                                                    ArrayList<Object> datos = new ArrayList<Object>();
                                                    datos.add(false);
                                                    Message respuesta = new Message("LOGIN", "LOGIN_ANDROID_TRABAJADORES", datos);
                                                    out.writeObject(respuesta);
                                                } else {
                                                    System.out.println("entra2");
                                                    ArrayList<Object> datos = new ArrayList<Object>();
                                                    datos.add(true);
                                                    datos.add(usuario.getNombre());

                                                    datos.add(listaRoles);

                                                    Message respuesta = new Message("LOGIN", "LOGIN_ANDROID_TRABAJADORES", datos);
                                                    out.writeObject(respuesta);

                                                    //Se asigna al cliente el usuario con el que logea
                                                    setUsuario(usuario);

                                                    notificarAmigos();
                                                    //enviarAmigos();
                                                    //notificarClientes();

                                                    estado = RUNNING;
                                                    System.out.println("ok");
                                                }

                                            } //Si no, es que los datos no son correctos y no se encontró ningún usuario
                                            else {
                                                ArrayList<Object> datos = new ArrayList<Object>();
                                                datos.add(false);
                                                Message respuesta = new Message("LOGIN", "LOGIN_ANDROID", datos);
                                                out.writeObject(respuesta);
                                            }
                                        }

                                        break;

                                    case "REGISTRO":
                                        
                                        synchronized (this) {
                                            //ROLES DEL USUARIO
                                            ArrayList<String> roles = (ArrayList<String>) peticion.getData().get(3);

                                            boolean resultadoInsertarUsuario = conexion.insertarUsuario((String) peticion.getData().get(0), (String) peticion.getData().get(1), (String) peticion.getData().get(2), (ArrayList<String>) peticion.getData().get(3));

                                            //Ejecución sin errores
                                            if (resultadoInsertarUsuario) {
                                                System.out.println("insertado ok");
                                                ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                datos_insert.add(true);
                                                datos_insert.add(peticion.getData().get(0) + "");
                                                Message respuesta_insert = new Message("LOGIN", "REGISTRO", datos_insert);
                                                out.writeObject(respuesta_insert);
                                            } else {
                                                System.out.println("insertado mal");
                                                ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                datos_insert.add(false);
                                                Message respuesta_insert = new Message("LOGIN", "REGISTRO", datos_insert);
                                                out.writeObject(respuesta_insert);
                                            }

                                        }

                                        break;

                                    //LOGIN
                                    //SALIR  
                                    //pepe    
                                    case "SALIR":

                                        System.out.println("Cliente desconectado: " + socketCliente.getInetAddress().getHostAddress());
                                        Server.eliminarHiloCliente(this);
                                        Server.mostrarClientesConectados();

                                        estado = EXIT;

                                        break;

                                }

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

                                            int numExcepciones = conexion.getExcepciones().size(); //Leer del fichero de errores
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
                                                String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                                cajaHoy += conexion.getCaja(e.getNombre(), fechaHoy);
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

                                    case "GET_CAJAS":

                                        synchronized (this) {
                                            Usuario usuario = conexion.getUsuario((String) peticion.getData().get(0));

                                            ArrayList<Object> datos_caja_query = new ArrayList<Object>();

                                            for (Establecimiento e : usuario.getEstablecimientos()) {
                                                ArrayList<Object> datosEstabl = new ArrayList<>();

                                                String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                                double cajaHoy = conexion.getCaja(e.getNombre(), fechaHoy);
                                                datosEstabl.add(e.getNombre());
                                                datosEstabl.add(cajaHoy);

                                                datos_caja_query.add(datosEstabl);
                                            }

                                            Message respuesta_caja_query = new Message("SERVIDOR", "GET_CAJAS", datos_caja_query);
                                            out.writeObject(respuesta_caja_query);
                                        }
                                        break;

                                    case "GET_TRABAJADORES_CONECTADOS":

                                        synchronized (this) {
                                            Usuario usuario = conexion.getUsuario((String) peticion.getData().get(0));

                                            ArrayList<Object> datos_trabajadores_query = new ArrayList<Object>();

                                            //Comprueba qué usuarios de los establecimientos están conectados al servidor
                                            for (Usuario u : conexion.getUsuarios(usuario.getEstablecimientos())) {
                                                ArrayList<Object> datosUsuarios = new ArrayList<>();
                                                for (HiloCliente h : Server.getListaClientes()) {

                                                    if (u.getNombre().equals(h.getUsuario().getNombre())) {
                                                        System.out.println("usuario: " + u.getNombre());
                                                        datosUsuarios.add(u.getNombre());
                                                        ArrayList<String> rolesUsuario = new ArrayList<>();
                                                        for (Rol r : u.getRols()) {
                                                            rolesUsuario.add(r.getNombre());
                                                        }

                                                        datosUsuarios.add(rolesUsuario);
                                                        //Se añade el usuario a la lista de datos de la respuesta
                                                        datos_trabajadores_query.add(datosUsuarios);

                                                    }

                                                }

                                            }

                                            Message respuesta_trabajadores_query = new Message("SERVIDOR", "GET_TRABAJADORES_CONECTADOS", datos_trabajadores_query);
                                            out.writeObject(respuesta_trabajadores_query);
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

                                                estado = WAITING;

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
                                                datos_actualizar.add((String) peticion.getData().get(1)); //Nuevo usuario
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
                                                    System.out.println("insertado ok");
                                                    ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                    datos_insert.add(true);
                                                    Message respuesta_insert = new Message("USUARIO", "INSERTAR", datos_insert);
                                                    out.writeObject(respuesta_insert);
                                                } else {
                                                    System.out.println("insertado mal");
                                                    ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                    datos_insert.add(false);
                                                    Message respuesta_insert = new Message("USUARIO", "INSERTAR", datos_insert);
                                                    out.writeObject(respuesta_insert);
                                                }
                                            }
                                        }

                                        break;

                                    case "ACTUALIZAR_DATOS_TRABAJADOR":

                                        String trabajador_a_actualizar = (String) peticion.getData().get(0);

                                        String nuevoNombre = (String) peticion.getData().get(1);
                                        String nuevoCorreo = (String) peticion.getData().get(2);
                                        String nuevaPassw = (String) peticion.getData().get(3);

                                        ArrayList<String> nuevosEstablecimientos = (ArrayList<String>) peticion.getData().get(4);

                                        synchronized (this) {

                                            boolean resultadoActualizarTrabajador = conexion.actualizarTrabajador(trabajador_a_actualizar, nuevoNombre, nuevoCorreo, nuevaPassw, nuevosEstablecimientos);

                                            //Ejecución sin errores
                                            if (resultadoActualizarTrabajador) {
                                                ArrayList<Object> datos_actualizar = new ArrayList<Object>();
                                                datos_actualizar.add(true);
                                                Message respuesta_actualizar = new Message("USUARIO", "ACTUALIZAR_DATOS_TRABAJADOR", datos_actualizar);
                                                out.writeObject(respuesta_actualizar);
                                            } else {
                                                ArrayList<Object> datos_actualizar = new ArrayList<Object>();
                                                datos_actualizar.add(false);
                                                Message respuesta_actualizar = new Message("USUARIO", "ACTUALIZAR_DATOS_TRABAJADOR", datos_actualizar);
                                                out.writeObject(respuesta_actualizar);
                                            }

                                        }

                                        break;

                                    case "BORRAR":
                                        
                                        synchronized (this) {

                                            String usuarioBorrar = (String) peticion.getData().get(0);

                                            boolean resultadoBorrarUsuario = conexion.borrarUsuario(usuarioBorrar);
                                            //Ejecución sin productoBorrar
                                            if (resultadoBorrarUsuario) {
                                                ArrayList<Object> datos_delete = new ArrayList<Object>();
                                                datos_delete.add(true);
                                                Message respuesta_delete = new Message("USUARIO", "BORRAR", datos_delete);
                                                out.writeObject(respuesta_delete);

                                            } else {
                                                ArrayList<Object> datos_delete = new ArrayList<Object>();
                                                datos_delete.add(false);
                                                Message respuesta_insert = new Message("USUARIO", "BORRAR", datos_delete);
                                                out.writeObject(respuesta_insert);
                                            }
                                        }

                                        break;

                                    case "AÑADIR_AMIGO":
                                        
                                        synchronized (this) {

                                            String amigo = (String) peticion.getData().get(0);
                                            int exitoAmigo = conexion.añadirAmigo(usuario, amigo, "Pendiente", "Envio");

                                            ArrayList<Object> data = new ArrayList<>();

                                            data.add(exitoAmigo);

                                            Message messageAñadirAmigo = new Message("USUARIO", "AÑADIR_AMIGO", data);
                                            out.writeObject(messageAñadirAmigo);

                                            if (exitoAmigo == 0) {
                                                //Notifica al solicitado
                                                notificarCliente(conexion.getUsuario(amigo));
                                            }

                                        }

                                        break;

                                    case "SOLICITUD_AMISTAD":
                                        
                                        synchronized (this) {

                                            String remitente = (String) peticion.getData().get(0);
                                            boolean aceptarRechazar = (boolean) peticion.getData().get(2);

                                            boolean solicitudExito = conexion.cambiarEstadoSolicitud(conexion.getUsuario(remitente), usuario, aceptarRechazar);

                                            if (!aceptarRechazar) {
                                                conexion.borrarAmistad(conexion.getUsuario(remitente), usuario);
                                            }

                                            //Ejecución sin errores. 
                                            if (solicitudExito) {

                                                ArrayList<Object> data = new ArrayList<>();

                                                data.add(true); //Exito en la ejecución
                                                data.add(aceptarRechazar); //True significa que acepta, y false que rechaza

                                                Message messageAñadirAmigo = new Message("USUARIO", "SOLICITUD_AMISTAD", data);
                                                out.writeObject(messageAñadirAmigo);

                                            } else {

                                                ArrayList<Object> data = new ArrayList<>();

                                                data.add(false);
                                                data.add(aceptarRechazar); //True significa que acepta, y false que rechaza

                                                Message messageAñadirAmigo = new Message("USUARIO", "SOLICITUD_AMISTAD", data);
                                                out.writeObject(messageAñadirAmigo);

                                            }

                                            notificarCliente(conexion.getUsuario(remitente));
                                            enviarAmigos();

                                        }

                                        break;

                                    case "GET_AMIGOS":
                                        
                                        try {
                                        enviarAmigos();
                                    } catch (NullPointerException ex) {

                                    }

                                    break;

                                    case "GET_SOLICITUDES_ENVIADAS":
                                        
                                        synchronized (this) {

                                            ArrayList<Object> data = conexion.getSolicitudesEnviadas(usuario.getNombre());

                                            Message message = new Message("USUARIO", "GET_SOLICITUDES_ENVIADAS", data);

                                            out.writeObject(message);

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

                                    case "GET_ESTABLECIMIENTOS_CERCANOS":
                                        
                                        synchronized (this) {
                                            double latitud = (double) peticion.getData().get(0);
                                            double longitud = (double) peticion.getData().get(1);
                                            double radio = (double) peticion.getData().get(2);
                                            ArrayList<Object> establList = conexion.getEstablecimientos(latitud, longitud, radio);

                                            System.out.println(establList.size());

                                            Message respuesta_establ_query = new Message("ESTABLECIMIENTO", "GET_ESTABLECIMIENTOS_CERCANOS", establList);
                                            out.writeObject(respuesta_establ_query);
                                        }

                                        break;

                                    case "GET_ESTABLECIMIENTOS_USUARIO":

                                        String usuario = (String) peticion.getData().get(0);

                                        synchronized (this) {

                                            ArrayList<String> establList = conexion.getEstablecimientos(usuario);
                                            System.out.println(establList);
                                            ArrayList<Object> datos_establ_query = new ArrayList<Object>();
                                            datos_establ_query.add(establList);
                                            Message respuesta_establ_query = new Message("ESTABLECIMIENTO", "GET_ESTABLECIMIENTOS_USUARIO", datos_establ_query);
                                            out.writeObject(respuesta_establ_query);
                                        }

                                        break;

                                    case "INSERTAR":
                                        
                                        synchronized (this) {

                                            boolean resultadoInsertarEstabl = conexion.insertarEstabl((String) peticion.getData().get(0), (String) peticion.getData().get(1), (String) peticion.getData().get(2), (String) peticion.getData().get(3));

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

                                    case "GET_TRABAJADORES":
                                    
                                        synchronized (this) {

                                            //Obtiene todos los trabajadores del establecimiento recibido
                                            String establecimiento = (String) peticion.getData().get(0);
                                            ArrayList<Usuario> trabajadores = conexion.getTrabajadores(establecimiento);

                                            //Lista de "Usuario" (trabajador)
                                            ArrayList<Object> datos_trabajadores_query = new ArrayList<>();

                                            //Recorre la lista de trabajadores de la base de datos
                                            //Por cada una, se crea una lista para añadir sus campos, y se añade a la lista inicial que se envía
                                            //al cliente.
                                            for (Usuario u : trabajadores) {

                                                ArrayList<Object> campos = new ArrayList<>();
                                                campos.add(u.getNombre());
                                                campos.add(u.getCorreo());
                                                campos.add(u.getPassw());

                                                datos_trabajadores_query.add(campos);

                                            }

                                            Message respuesta_trabajadores_query = new Message("ESTABLECIMIENTO", "GET_TRABAJADORES", datos_trabajadores_query);
                                            out.writeObject(respuesta_trabajadores_query);
                                            System.out.println(datos_trabajadores_query);
                                        }

                                        break;

                                    case "SET_ESTABLECIMIENTO":
                                        
                                        synchronized (this) {
                                            String establecimiento = (String) peticion.getData().get(0);
                                            Establecimiento establ = conexion.getEstablecimiento(establecimiento);
                                            this.establecimientoActual = establ;
                                            System.out.println("ESTABL: " + establ.getNombre());

                                            ArrayList<Object> datosEstabl = new ArrayList<>();
                                            datosEstabl.add(establecimientoActual.getLatitud());
                                            datosEstabl.add(establecimientoActual.getLongitud());

                                            //Envía las coordenadas al cliente
                                            Message messageCoords = new Message("ESTABLECIMIENTO", "SET_ESTABLECIMIENTO", datosEstabl);
                                            out.writeObject(messageCoords);

                                            //Si es cliente, notifica a sus amigos de que se ha conectado
                                            notificarAmigos();

                                            //Si es trabajador, recibe los pedidos de su establecimiento que estén en Preparando
                                            notificarPedidosTrabajadoresBDD(establecimientoActual);

                                        }

                                        break;

                                }

                                break;

                            case "CATEGORIA":

                                switch (peticion.getRequestAction()) {

                                    case "GET_CATEGORIAS":
                                        
                                        synchronized (this) {
                                            String establecimiento = (String) peticion.getData().get(0);

                                            /*
                                            if(this.establecimientoActual == null){
                                                this.establecimientoActual = conexion.getEstablecimiento(establecimiento);
                                                notificarAmigos();
                                            }
                                             */
                                            ArrayList<Categoria> categorias = conexion.getCategorias(establecimiento);

                                            System.out.println(categorias);
                                            ArrayList<Object> datos_categories_query = new ArrayList<Object>();

                                            for (Categoria c : categorias) {
                                                datos_categories_query.add(c.getNombre());
                                            }

                                            System.out.println(datos_categories_query);
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
                                                Message respuesta_insert = new Message("CATEGORIA", "INSERTAR", datos_insert);
                                                out.writeObject(respuesta_insert);

                                            } else {
                                                ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                datos_insert.add(false);
                                                Message respuesta_insert = new Message("CATEGORIA", "INSERTAR", datos_insert);
                                                out.writeObject(respuesta_insert);
                                            }
                                        }

                                        break;

                                }

                                break;

                            case "PRODUCTO":

                                switch (peticion.getRequestAction()) {

                                    case "INSERTAR":
                                        
                                        synchronized (this) {

                                            String nombreProducto = (String) peticion.getData().get(0);
                                            String descripcionProducto = (String) peticion.getData().get(1);
                                            Establecimiento establProducto = conexion.getEstablecimiento((String) peticion.getData().get(2));
                                            double precio = (double) peticion.getData().get(3);
                                            int stock = (int) peticion.getData().get(4);
                                            ArrayList<String> categorias = (ArrayList<String>) peticion.getData().get(5);
                                            byte[] imagen = (byte[]) peticion.getData().get(6);

                                            //System.out.println(establCategoria);
                                            boolean resultadoInsertarProducto = conexion.insertarProducto(nombreProducto, descripcionProducto, precio, stock, imagen, categorias, establProducto);

                                            //Ejecución sin errores
                                            if (resultadoInsertarProducto) {
                                                ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                datos_insert.add(true);
                                                Message respuesta_insert = new Message("PRODUCTO", "INSERTAR", datos_insert);
                                                out.writeObject(respuesta_insert);

                                            } else {
                                                ArrayList<Object> datos_insert = new ArrayList<Object>();
                                                datos_insert.add(false);
                                                Message respuesta_insert = new Message("PRODUCTO", "INSERTAR", datos_insert);
                                                out.writeObject(respuesta_insert);
                                            }
                                        }

                                        break;

                                    case "GET_PRODUCTOS":
                                    
                                        synchronized (this) {

                                            //Obtiene los productos del establecimiento recibido
                                            String establecimiento = (String) peticion.getData().get(0);
                                            ArrayList<Producto> productList = conexion.getProductos(establecimiento);

                                            //Lista de "productos"
                                            ArrayList<Object> datos_prod_query = new ArrayList<Object>();

                                            //Recorre la lista de productos de la base de datos
                                            //Por cada producto, se crea una lista para añadir sus campos, y se añade a la lista inicial que se envía
                                            //al cliente.
                                            for (Producto p : productList) {

                                                ArrayList<Object> camposProducto = new ArrayList<>();
                                                camposProducto.add(p.getNombre());
                                                camposProducto.add(p.getDescripcion());
                                                camposProducto.add(p.getPrecio());
                                                camposProducto.add(p.getStock());
                                                camposProducto.add(p.getImagen());

                                                datos_prod_query.add(camposProducto);

                                            }

                                            Message respuesta_prop_query = new Message("PRODUCTO", "GET_PRODUCTOS", datos_prod_query);
                                            out.writeObject(respuesta_prop_query);
                                        }

                                        break;

                                    case "GET_PRODUCTOS_CATEGORIA":
                                    
                                        synchronized (this) {

                                            //Obtiene los productos del establecimiento y categoría recibidos
                                            String establecimiento = (String) peticion.getData().get(0);
                                            String categoria = (String) peticion.getData().get(1);
                                            ArrayList<Producto> productList = conexion.getProductos(establecimiento, categoria);

                                            //Lista de "productos"
                                            ArrayList<Object> datos_prod_query = new ArrayList<Object>();

                                            //Recorre la lista de productos de la base de datos
                                            //Por cada producto, se crea una lista para añadir sus campos, y se añade a la lista inicial que se envía
                                            //al cliente.
                                            for (Producto p : productList) {

                                                ArrayList<Object> camposProducto = new ArrayList<>();
                                                camposProducto.add(p.getNombre());
                                                camposProducto.add(p.getDescripcion());
                                                camposProducto.add(p.getPrecio());
                                                camposProducto.add(p.getStock());
                                                camposProducto.add(p.getImagen());

                                                datos_prod_query.add(camposProducto);

                                            }

                                            Message respuesta_prop_query = new Message("PRODUCTO", "GET_PRODUCTOS_CATEGORIA", datos_prod_query);
                                            out.writeObject(respuesta_prop_query);

                                        }

                                        break;

                                    case "GET_DATOS_PRODUCTO":

                                        String nombreProducto = (String) peticion.getData().get(0);
                                        String establecimientoProducto = (String) peticion.getData().get(1);

                                        synchronized (this) {

                                            Producto producto = conexion.getProducto(nombreProducto, establecimientoProducto);

                                            System.out.println(producto.getNombre());
                                            System.out.println(producto.getDescripcion());
                                            System.out.println(producto.getPrecio());
                                            System.out.println(producto.getStock());

                                            ArrayList<Object> datos_product_query = new ArrayList<>();

                                            datos_product_query.add(producto.getNombre());
                                            datos_product_query.add(producto.getDescripcion());
                                            datos_product_query.add(producto.getPrecio());
                                            datos_product_query.add(producto.getStock());
                                            datos_product_query.add(producto.getImagen());

                                            ArrayList<String> categoriasProducto = new ArrayList<>();

                                            for (Categoria c : producto.getCategorias()) {

                                                categoriasProducto.add(c.getNombre());

                                            }
                                            System.out.println(categoriasProducto);

                                            datos_product_query.add(categoriasProducto);

                                            System.out.println("Tamaño:" + datos_product_query.size());

                                            Message respuesta_product_query = new Message("PRODUCTO", "GET_DATOS_PRODUCTO", datos_product_query);
                                            out.writeObject(respuesta_product_query);

                                        }

                                        break;

                                    case "ACTUALIZAR_DATOS_PRODUCTO":

                                        String producto_a_actualizar = (String) peticion.getData().get(0);
                                        String establProducto = (String) peticion.getData().get(1);

                                        String nuevoNombre = (String) peticion.getData().get(2);
                                        String nuevaDescrip = (String) peticion.getData().get(3);
                                        String campoPrecio = (String) peticion.getData().get(4);
                                        double nuevoPrecio = Double.parseDouble(campoPrecio);
                                        String campoStock = (String) peticion.getData().get(5);
                                        int nuevoStock = Integer.parseInt(campoStock);
                                        byte[] nuevaImagen = (byte[]) peticion.getData().get(6);
                                        ArrayList<String> nuevasCategorias = (ArrayList<String>) peticion.getData().get(7);

                                        synchronized (this) {

                                            boolean resultadoActualizarProducto = conexion.actualizarProducto(producto_a_actualizar, establProducto, nuevoNombre, nuevaDescrip, nuevoPrecio, nuevoStock, nuevaImagen, nuevasCategorias);

                                            //Ejecución sin errores
                                            if (resultadoActualizarProducto) {
                                                ArrayList<Object> datos_actualizar = new ArrayList<Object>();
                                                datos_actualizar.add(true);
                                                Message respuesta_actualizar = new Message("PRODUCTO", "ACTUALIZAR_DATOS_PRODUCTO", datos_actualizar);
                                                out.writeObject(respuesta_actualizar);
                                            } else {
                                                ArrayList<Object> datos_actualizar = new ArrayList<Object>();
                                                datos_actualizar.add(false);
                                                Message respuesta_actualizar = new Message("PRODUCTO", "ACTUALIZAR_DATOS_PRODUCTO", datos_actualizar);
                                                out.writeObject(respuesta_actualizar);
                                            }

                                        }

                                        break;

                                    case "BORRAR":
                                        
                                        synchronized (this) {

                                            String productoBorrar = (String) peticion.getData().get(0);
                                            String establProductoBorrar = (String) peticion.getData().get(1);

                                            boolean resultadoBorrarProducto = conexion.borrarProducto(productoBorrar, establProductoBorrar);
                                            //Ejecución sin productoBorrar
                                            if (resultadoBorrarProducto) {
                                                ArrayList<Object> datos_delete = new ArrayList<Object>();
                                                datos_delete.add(true);
                                                Message respuesta_delete = new Message("PRODUCTO", "BORRAR", datos_delete);
                                                out.writeObject(respuesta_delete);

                                            } else {
                                                ArrayList<Object> datos_delete = new ArrayList<Object>();
                                                datos_delete.add(false);
                                                Message respuesta_insert = new Message("PRODUCTO", "BORRAR", datos_delete);
                                                out.writeObject(respuesta_insert);
                                            }
                                        }

                                        break;

                                }

                                break;

                            case "PEDIDO":

                                switch (peticion.getRequestAction()) {

                                    case "COMPROBACION_SESION":
                                        
                                        synchronized (this) {

                                            boolean existe = false;

                                            for (SesionCliente sesion : Server.getSesiones()) {

                                                //Si ya existe una sesión donde el usuario es el anfitrión, envía true
                                                if (sesion.getClienteAnfitrion().getNombre().equals(usuario.getNombre())) {

                                                    existe = true;
                                                    this.sesion = sesion;
                                                    break;

                                                }
                                            }

                                            ArrayList<Object> data = new ArrayList<>();
                                            data.add(existe);
                                            Message message = new Message("PEDIDO", "COMPROBACION_SESION", data);
                                            out.writeObject(message);

                                        }

                                        break;

                                    case "GET_PEDIDOS":
                                    
                                        synchronized (this) {

                                            //Obtiene todas las líneas de pedido del establecimiento recibido
                                            String establecimiento = (String) peticion.getData().get(0);
                                            ArrayList<LineaPedido> lineasPedidos = conexion.getLineaPedidos(establecimiento);

                                            //Lista de "LineaPedido"
                                            ArrayList<Object> datos_pedidos_query = new ArrayList<>();

                                            //Recorre la lista de líneas de pedidos de la base de datos
                                            //Por cada una, se crea una lista para añadir sus campos, y se añade a la lista inicial que se envía
                                            //al cliente.
                                            for (LineaPedido p : lineasPedidos) {

                                                ArrayList<Object> camposLinea = new ArrayList<>();
                                                camposLinea.add(p.getPedido().getId());
                                                camposLinea.add(p.getCliente().getNombre());
                                                camposLinea.add(p.getProducto().getNombre());
                                                camposLinea.add(p.getCantidad());
                                                camposLinea.add(p.getEstado());

                                                datos_pedidos_query.add(camposLinea);

                                            }

                                            Message respuesta_pedidos_query = new Message("PEDIDO", "GET_PEDIDOS", datos_pedidos_query);
                                            out.writeObject(respuesta_pedidos_query);
                                            System.out.println(datos_pedidos_query);
                                        }

                                        break;

                                    case "ENVIAR_LINEASPEDIDOS":
                                    
                                        synchronized (this) {

                                            ArrayList<Object> datosRecibidos = peticion.getData();
                                            ArrayList<LineaPedido> lineasPedido = new ArrayList<>();

                                            for (Object o : datosRecibidos) {

                                                ArrayList<Object> camposLineaPedido = (ArrayList<Object>) o;

                                                int idPedido = (int) camposLineaPedido.get(0);
                                                Pedido pedido = conexion.getPedido(idPedido);

                                                String nombreProducto = (String) camposLineaPedido.get(1);
                                                String nombreEstabl = (String) camposLineaPedido.get(2);
                                                Producto producto = conexion.getProducto(nombreProducto, nombreEstabl);

                                                String nombreUsuario = (String) camposLineaPedido.get(3);
                                                Usuario usuario = conexion.getUsuario(nombreUsuario);

                                                int cantidad = (int) camposLineaPedido.get(4);

                                                LineaPedido lineaPedido = new LineaPedido(pedido, producto, usuario, cantidad, "Iniciado");

                                                lineasPedido.add(lineaPedido);

                                            }
                                            sesion.agregarLineasPedido(lineasPedido);
                                            //sesion.setLineasPedido(lineasPedido);

//                                            
                                        }

                                        break;

                                    case "NUEVO_PEDIDO":
                                    
                                        synchronized (this) {

                                            Usuario usuarioAnfitrion = conexion.getUsuario((String) peticion.getData().get(0));
                                            Establecimiento establecimientoSesion = conexion.getEstablecimiento((String) peticion.getData().get(1));

                                            String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                            Pedido pedido = conexion.crearPedido(fecha, usuarioAnfitrion, establecimientoSesion);

                                            //Ejecución sin errores
                                            if (pedido != null) {

                                                //Se crea el objeto para la sesión, y se añade a la colección del servidor
                                                SesionCliente sesion = new SesionCliente(usuarioAnfitrion, pedido);
                                                sesion.añadirInvitado(this);
                                                this.sesion = sesion;

                                                System.out.println("Pedido: " + sesion.getPedidoSesion().getId());
                                                System.out.println("Participante: " + sesion.getClienteAnfitrion());
                                                //conexion.insertarParticipante(sesion.getPedidoSesion(), sesion.getClienteAnfitrion());
                                                conexion.insertarParticipantes(sesion.getPedidoSesion(), sesion.getUsuariosInvitados());

                                                Server.agregarSesion(sesion);

                                                ArrayList<Object> data = new ArrayList<>();
                                                data.add(this.sesion.getPedidoSesion().getId());
                                                data.add(this.sesion.getClienteAnfitrion().getNombre());

                                                Message respuesta_nueva_sesion = new Message("PEDIDO", "NUEVO_PEDIDO", data);
                                                out.writeObject(respuesta_nueva_sesion);
                                            } else {
                                                System.out.println("Error al crear la sesión");
                                            }
                                        }

                                        break;

                                    case "UNIRSE_PEDIDO":
                                    
                                        synchronized (this) {

                                            Usuario usuarioAnfitrion = conexion.getUsuario((String) peticion.getData().get(0));
                                            Establecimiento establecimientoSesion = conexion.getEstablecimiento((String) peticion.getData().get(1));
                                            int codigoSesion = Integer.parseInt((String) peticion.getData().get(2));

                                            for (SesionCliente sesion : Server.getSesiones()) {

                                                if (sesion.getCodigoSesion() == codigoSesion) {

                                                    sesion.añadirInvitado(this);
                                                    this.sesion = sesion;

                                                    break;

                                                }

                                            }

                                            //Ejecución sin errores
                                            if (sesion != null) {

                                                ArrayList<Object> data = new ArrayList<>();
                                                data.add(sesion.getPedidoSesion().getId());
                                                data.add(sesion.getClienteAnfitrion().getNombre());
                                                data.add(sesion.convertirLista());

                                                Message respuesta_nueva_sesion = new Message("PEDIDO", "UNIRSE_PEDIDO", data);
                                                out.writeObject(respuesta_nueva_sesion);

                                            } else {

                                                ArrayList<Object> data = new ArrayList<>();
                                                data.add(0);

                                                Message respuesta_nueva_sesion = new Message("PEDIDO", "UNIRSE_PEDIDO", data);
                                                out.writeObject(respuesta_nueva_sesion);

                                            }

                                        }

                                        break;

                                    case "INVITAR":
                                        
                                        synchronized (this) {

                                            String usuarioInvitado = (String) peticion.getData().get(0);
                                            String establecimiento = (String) peticion.getData().get(1);

                                            ArrayList<Object> data = new ArrayList<>();

                                            for (HiloCliente cliente : Server.getListaClientes()) {

                                                //Busca al usuario que se quiere invitar y esté conectado
                                                if (cliente.getUsuario() != null) {
                                                    if (cliente.getUsuario().getNombre().equals(usuarioInvitado)) {

                                                        //Se comprueba que el usuario no pertenezca ya a la sesión
                                                        if (sesion.apareceCliente(usuarioInvitado) && sesion.apareceCliente(usuario.getNombre())) {

                                                            //cliente.enviarInvitacion(usuario.getNombre(),establecimiento,false);
                                                            data.add(1); //ya pertenece a la sesion
                                                            break;

                                                        } else {

                                                            //Si el establecimiento del invitado es distinto al del cliente, recibe un error
                                                            if (!cliente.getEstablecimientoActual().getNombre().equals(establecimientoActual.getNombre())) {
                                                                data.add(2); //establecimientos distintos
                                                                break;
                                                            } else {
                                                                cliente.enviarInvitacion(usuario.getNombre(), establecimiento, true);
                                                                data.add(0); //bien
                                                                break;
                                                            }

                                                        }

                                                    }
                                                }

                                            }

                                            Message message = new Message("PEDIDO", "INVITAR", data);
                                            out.writeObject(message);

                                        }

                                        break;

                                    case "RESPUESTA_INVITACION":
                                        
                                        synchronized (this) {

                                            String remitente = (String) peticion.getData().get(0);

                                            for (SesionCliente sesion : Server.getSesiones()) {

                                                if (sesion.getClienteAnfitrion().getNombre().equals(remitente)) {

                                                    sesion.añadirInvitado(this);
                                                    this.sesion = sesion;

                                                    System.out.println("Pedido: " + sesion.getPedidoSesion().getId());
                                                    System.out.println("Participante: " + usuario.getNombre());
                                                    //Añade el participante en la BDD
                                                    //conexion.insertarParticipante(sesion.getPedidoSesion(), usuario);
                                                    conexion.insertarParticipantes(sesion.getPedidoSesion(), sesion.getUsuariosInvitados());

                                                }

                                            }

                                            //Ejecución sin errores
                                            if (sesion != null) {

                                                ArrayList<Object> data = new ArrayList<>();
                                                data.add(sesion.getPedidoSesion().getId());
                                                data.add(sesion.getClienteAnfitrion().getNombre());
                                                data.add(sesion.convertirLista());

                                                Message respuesta_nueva_sesion = new Message("PEDIDO", "UNIRSE_PEDIDO", data);
                                                out.writeObject(respuesta_nueva_sesion);

                                            } else {

                                                ArrayList<Object> data = new ArrayList<>();
                                                data.add(0);

                                                Message respuesta_nueva_sesion = new Message("PEDIDO", "UNIRSE_PEDIDO", data);
                                                out.writeObject(respuesta_nueva_sesion);

                                            }

                                        }

                                        break;

                                    case "GET_LISTA_PEDIDOS":

                                        synchronized (this) {
                                            int idPedidoActual = (int) peticion.getData().get(0);
                                            SesionCliente sesionActual = null;
                                            for (SesionCliente sesion : Server.getSesiones()) {
                                                if (sesion.getPedidoSesion().getId() == idPedidoActual) {
                                                    sesionActual = sesion;
                                                    break;
                                                }
                                            }

                                            ArrayList<Object> datos = new ArrayList<>();
                                            datos = sesionActual.convertirLista();

                                            Message respuesta_pedidos_query = new Message("PEDIDO", "GET_LISTA_PEDIDOS", datos);
                                            out.writeObject(respuesta_pedidos_query);
                                            System.out.println(datos);
                                        }

                                        break;

                                    case "ACTUALIZAR_PEDIDO":

                                        ArrayList<Integer> nuevasCantidades = new ArrayList<>();

                                        synchronized (this) {
                                            for (Object o : peticion.getData()) {

                                                nuevasCantidades.add((Integer) o);

                                            }

                                            sesion.actualizarCantidades(nuevasCantidades);
                                        }

                                        break;

                                    case "PAGAR":
                                        
                                        synchronized (this) {

                                            ArrayList<LineaPedido> lineasAPagar = new ArrayList<>();

                                            for (LineaPedido lp : sesion.getLineasDePedido()) {

                                                if (lp.getEstado().equals("Iniciado")) {
                                                    lineasAPagar.add(lp); //Se añade la linea a la lista para el pago e inserción en la BDD
                                                    lp.setEstado("Pagando"); //Se cambia el estado de la linea a Pagando

                                                }

                                            }

                                            boolean exitoInsertarLp = conexion.insertarLineasPedido(lineasAPagar);

                                            //Ejecución sin errores
                                            if (exitoInsertarLp) {
                                                System.out.println("entra exito lp");
                                                //Las lineas con estado Pagando, son las que acaban de insertarse en la BDD
                                                for (LineaPedido lp : sesion.getLineasDePedido()) {
                                                    if (lp.getEstado().equals("Pagando")) {
                                                        lp.setEstado("Preparando"); //Cambia su estado a Preparando
                                                        //ACTUALIZAR ESTA LINEA PEDIDO EN LA BASE DE DATOS
                                                        conexion.actualizarLineaPedido(lp);
                                                    }

                                                }

                                                //Se notifica a los clientes de la sesión con la lista actualizada
                                                sesion.notificarPedidos();

                                                try {
                                                    //Se notifica a los trabajadores con las nuevas lineas de pedido en estado Preparando
                                                    notificarTrabajadores(establecimientoActual, conexion.getLineasEstablecimiento(establecimientoActual, "DISPONIBLES"));
                                                } catch (NullPointerException ex) {

                                                }

                                            }

                                        }

                                        break;

                                    case "BORRAR_LINEA_PEDIDO":

                                        ArrayList<Object> camposLineaPedido = (ArrayList<Object>) peticion.getData().get(0);

                                        synchronized (this) {
                                            int idPedido = (int) camposLineaPedido.get(0);
                                            Pedido pedido = conexion.getPedido(idPedido);

                                            String nombreProducto = (String) camposLineaPedido.get(1);
                                            String nombreEstabl = (String) camposLineaPedido.get(2);
                                            Producto producto = conexion.getProducto(nombreProducto, nombreEstabl);

                                            String nombreUsuario = (String) camposLineaPedido.get(3);
                                            Usuario usuario = conexion.getUsuario(nombreUsuario);

                                            int cantidad = (int) camposLineaPedido.get(4);

                                            LineaPedido lineaPedido = new LineaPedido(pedido, producto, usuario, cantidad, "Iniciado");
                                            lineaPedido.setId(new LineaPedidoId(idPedido, sesion.getPedidoSesion().getId()));

                                            sesion.eliminarLineaPedido(lineaPedido);
                                        }

                                        break;

                                    case "CAMBIAR_ESTADO":

                                        int idLinea = (int) peticion.getData().get(0);
                                        int idPedido = (int) peticion.getData().get(1);
                                        String estadoNuevo = (String) peticion.getData().get(2);

                                        //Se controla que la linea de pedido aparezca en una sesión.
                                        //Si aparece, notifica a los clientes y a la base de datos con la modificación del estado
                                        boolean entra = false;
                                        for (SesionCliente sesion : Server.getSesiones()) {

                                            if (sesion.getPedidoSesion().getId() == idPedido) {

                                                for (LineaPedido lineaPedido : sesion.getLineasDePedido()) {

                                                    if (lineaPedido.getId().getIdLineaPedido() == idLinea) {

                                                        lineaPedido.setEstado(estadoNuevo);
                                                        if (estadoNuevo.equals("Recoger")) {
                                                            lineaPedido.setCodigoRecogida(sesion.generarNumeroAleatorio());
                                                            System.out.println("Codigo: " + lineaPedido.getCodigoRecogida());
                                                        }
                                                        //ACTUALIZA ESTA LINEA PEDIDO EN LA BASE DE DATOS
                                                        conexion.actualizarLineaPedido(lineaPedido);
                                                        entra = true;
                                                        break;

                                                    }

                                                }
                                                sesion.notificarPedidos();

                                            }

                                        }

                                        //Si la linea de pedido no pertenece a ninguna sesión existente, solo actualiza la base de datos
                                        if (!entra) {
                                            LineaPedido lp = conexion.getLineaPedido(idLinea, idPedido);
                                            lp.setEstado(estadoNuevo);
                                            conexion.actualizarLineaPedido(lp);
                                        }
                                        
                                        try {
                                            //Se notifica a los trabajadores  con las lineas actualizadas
                                            notificarTrabajadores(establecimientoActual, conexion.getLineasEstablecimiento(establecimientoActual, "DISPONIBLES"));
                                        } catch (NullPointerException ex) {

                                        }

                                        break;

                                    case "DEJAR_PEDIDO":

                                        //Si es anfitrión, además de elimiar el hilo de la lista de invitados, se notifica a los invitados
                                        //del cierre de la sesión, y se elimina del servidor
                                        if (sesion.getClienteAnfitrion().getNombre().equals(usuario.getNombre())) {
                                            System.out.println("entra anfitrion");
                                            sesion.notificarCierre();
                                            sesion.eliminarInvitado(this);

                                            for (LineaPedido lp : sesion.getLineasDePedido()) {

                                                lp.setEstado("Cancelado");
                                                conexion.actualizarLineaPedido(lp);

                                            }

                                            try {
                                                //Se notifica a los trabajadores  con las lineas actualizadas
                                                notificarTrabajadores(establecimientoActual, conexion.getLineasEstablecimiento(establecimientoActual, "DISPONIBLES"));
                                            } catch (NullPointerException ex) {

                                            }

                                            Server.eliminarSesion(sesion);

                                        } //Si no, solamente se elimina al invitado
                                        else {
                                            sesion.notificarCierre(this);
                                            sesion.eliminarInvitado(this);
                                        }

                                        break;

                                    case "RECOGIDA":

                                        //Se busca la sesión con el pedido recibido, y dentro, se busca la linea de pedido con el id recibido
                                        String codigo = (String) peticion.getData().get(0);
                                        int codigoRecogida = Integer.parseInt(codigo);
                                        int idPedidoSesion = (int) peticion.getData().get(1);

                                        for (SesionCliente sesion : Server.getSesiones()) {

                                            if (sesion.getPedidoSesion().getId() == idPedidoSesion) {

                                                for (LineaPedido lp : sesion.getLineasDePedido()) {

                                                    if (lp.getCodigoRecogida() == codigoRecogida) {

                                                        lp.setEstado("Confirmado");
                                                        conexion.actualizarLineaPedido(lp);

                                                    }

                                                }
                                                
                                                //Notifica a los clientes
                                                sesion.notificarPedidos();
                                            }

                                        }

                                        try {
                                            //Se notifica a los trabajadores  con las lineas actualizadas
                                            notificarTrabajadores(establecimientoActual, conexion.getLineasEstablecimiento(establecimientoActual, "DISPONIBLES"));
                                        } catch (NullPointerException ex) {

                                        }

                                        break;

                                }

                                break;

                        }

                        break;

                }

            }

            socketCliente.close();
        } catch (IOException e) {
            if (usuario != null) {
                System.out.println("Cliente desconectado inesperadamente: " + usuario.getNombre() + "@" + socketCliente.getInetAddress().getHostAddress());
            } else {
                System.out.println("Cliente desconectado inesperadamente: " + socketCliente.getInetAddress().getHostAddress());
            }
            //Server.eliminarHiloCliente(Thread.currentThread());
            Server.eliminarHiloCliente(this);
            Server.mostrarClientesConectados();

            //Si está en una sesión, se elimina de la lista de invitados. Se actualizan los participantes en la BDD
            if (sesion != null) {
                sesion.eliminarInvitado(this);
                conexion.insertarParticipantes(sesion.getPedidoSesion(), sesion.getUsuariosInvitados());
            }

            try {
                notificarAmigos(); //Notifica a sus amigos de la desconexión
            } catch (NullPointerException ex) {
                //Puede ser que no tenga amigos conectados o su rol no es cliente
            }

            //e.printStackTrace();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void notificarCliente(Usuario remitente) {

        for (HiloCliente cliente : Server.getListaClientes()) {

            if (cliente.usuario != null && cliente.usuario.getNombre().equals(remitente.getNombre())) {

                cliente.enviarAmigos();
                break;

            }

        }

    }

    //Notifica a los amigos del cliente de que se ha desconectado, para que recarguen sus lista de amigos
    public void notificarAmigos() {

        ArrayList<Object> amigos = conexion.getAmigos(usuario.getNombre());

        for (Object o : amigos) {

            ArrayList<Object> camposUsuario = (ArrayList<Object>) o;

            String nombre = (String) camposUsuario.get(0);

            //Comprueba en la lista de clientes, si alguno es su amigo. Si lo es, le notifica
            for (HiloCliente cliente : Server.getListaClientes()) {

                try {

                    if (cliente.getUsuario().getNombre().equals(nombre)) {

                        notificarCliente(cliente.getUsuario());
                        break;

                    }

                } catch (NullPointerException ex) {

                }

            }

        }

    }

    public void enviarAmigos() {

        ArrayList<Object> solicitudes = conexion.getSolicitudesPendientes(usuario.getNombre());
        ArrayList<Object> amigos = conexion.getAmigos(usuario.getNombre());

        for (Object o : amigos) {

            ArrayList<Object> camposUsuario = (ArrayList<Object>) o;

            String nombre = (String) camposUsuario.get(0);
            //String estado = (String) camposUsuario.get(1);

            for (HiloCliente cliente : Server.getListaClientes()) {

                try {

                    System.out.println("Cliente lista: " + cliente.getUsuario().getNombre());
                    System.out.println("Amigo: " + nombre);

                    if (cliente.getUsuario().getNombre().equals(nombre)) {
                        camposUsuario.add("Conectado");
                        break;
                    }
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }

            }

        }

        ArrayList<Object> listaCombinada = new ArrayList<>();
        listaCombinada.addAll(solicitudes);
        listaCombinada.addAll(amigos);

        try {

            Message message = new Message("USUARIO", "GET_AMIGOS", listaCombinada);

            out.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void enviarInvitacion(String remitente, String establecimiento, boolean exito) {

        ArrayList<Object> data = new ArrayList<>();
        data.add(remitente);
        data.add(establecimiento);
        data.add(exito);

        Message mensaje = new Message("PEDIDO", "RESPUESTA_INVITACION", data);
        try {
            out.writeObject(mensaje);
        } catch (IOException ex) {
            Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void actualizarPedidos(ArrayList<Object> lineasPedido) {

        Message mensaje = new Message("PEDIDO", "NOTIFICAR_PEDIDOS", lineasPedido);
        try {
            out.writeObject(mensaje);
        } catch (IOException ex) {
            Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //Notifica a los trabajadores del establecimiento dado con las lineas de pedido nuevas
    public void notificarTrabajadores(Establecimiento establecimiento, ArrayList<Object> lineasPedido) {

        for (HiloCliente cliente : Server.getListaClientes()) {

            ArrayList<String> listaRoles = new ArrayList<>();
            for (Rol r : cliente.getUsuario().getRols()) {
                listaRoles.add(r.getNombre());
            }

            //Comprueba que su rol sea trabajador y su establecimiento sea el del pedido
            if (listaRoles.contains("trabajador")
                    && cliente.getEstablecimientoActual().getNombre().equals(establecimiento.getNombre())) {

                System.out.println("Trabajador: " + cliente.getUsuario().getNombre());
                System.out.println("Pedidos: " + lineasPedido.size());
                cliente.enviarPedidosNuevos(lineasPedido);

            }

        }

    }

    public void enviarPedidosNuevos(ArrayList<Object> lineasPedido) {

        Message mensaje = new Message("PEDIDO", "GET_PEDIDOS_NUEVOS", lineasPedido);
        try {
            out.writeObject(mensaje);
            System.out.println("Enviado");
        } catch (IOException ex) {
            Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void notificarPedidosTrabajadoresBDD(Establecimiento establecimiento) {

        for (HiloCliente cliente : Server.getListaClientes()) {

            if (cliente.getUsuario() != null) {
                ArrayList<String> listaRoles = new ArrayList<>();
                for (Rol r : cliente.getUsuario().getRols()) {
                    listaRoles.add(r.getNombre());
                }

                //Comprueba que su rol sea trabajador y su establecimiento sea el del pedido
                if (listaRoles.contains("trabajador")
                        && cliente.getEstablecimientoActual().getNombre().equals(establecimiento.getNombre())) {

                    System.out.println("Trabajador:" + cliente.getUsuario().getNombre());

                    ArrayList<Object> lineas = conexion.getLineasEstablecimiento(establecimiento, "DISPONIBLES");
                    System.out.println("TOTAL LINEAS:" + lineas.size());
                    System.out.println(lineas);
                    cliente.enviarPedidosNuevos(lineas);

                }
            }

        }

    }

    public void salirSesion() {

        Message mensaje = new Message("PEDIDO", "SALIR_SESION", new ArrayList<>());
        try {
            out.writeObject(mensaje);
            System.out.println("Enviado");
        } catch (IOException ex) {
            Logger.getLogger(HiloCliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
