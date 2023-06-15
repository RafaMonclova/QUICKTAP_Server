/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package bdd;


import entidades.Amistad;
import entidades.Categoria;
import entidades.Establecimiento;
import entidades.LineaPedido;
import entidades.Pedido;
import entidades.Producto;
import entidades.Usuario;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Interfaz con los métodos que realizan operaciones sobre la BDD
 * @author rafam
 */
public interface DAO {
    
    //SERVIDOR
    public long getTotalClientes();
    public String getUltimoCliente();
    public double getCaja(String establecimiento, String fecha);

    
    //USUARIO
    public boolean insertarUsuario(String name,String mail,String password,ArrayList<String> roles); //Administrador y cliente
    public boolean insertarUsuario(String name,String mail,String password,ArrayList<String> roles,ArrayList<String> establecimientos); //Propietario y trabajador
    public Usuario getUsuario(String correo,String passw);
    public Usuario getUsuario(String nombre);
    public ArrayList<Usuario> getUsuarios(String rol);
    public ArrayList<Usuario> getTrabajadores(String establecimiento);
    public ArrayList<Usuario> getTrabajadoresPropietarios(String establecimiento);
    public boolean actualizarTrabajador(String nombreTrabajador, String nuevoNombre,String nuevoCorreo,
            String nuevaPassw, ArrayList<String> nuevosEstablecimientos);
    public boolean actualizarUsuario(String nombreIdentifica, 
            String nombre, String correo, String passw, ArrayList<String> roles); //Administrador y cliente
    public boolean actualizarUsuario(String nombreIdentifica, 
            String nombre, String correo, String passw, ArrayList<String> roles, ArrayList<String> establecimientos); //Propietario y trabajador
    public boolean actualizarUsuario(String nombreIdentifica, 
            String nombre, String correo, String passw); //Datos personales
    public boolean actualizarUsuario(String nombreIdentifica, 
            ArrayList<String> roles,ArrayList<String> establecimientos); //Roles y establecimientos
    public boolean borrarUsuario(String nombreUsuario);
    public ArrayList<Usuario> getUsuarios(Set<Establecimiento> establecimientos); //Devuelve una lista con los usuarios que forman parte de los establecimientos dados
    
    
    //ESTABLECIMIENTO
    public Establecimiento getEstablecimiento(String nombre);
    public ArrayList<String> getEstablecimientos();
    public ArrayList<Establecimiento> getDatosEstablecimientos();
    public ArrayList<String> getEstablecimientos(String usuario);
    public ArrayList<Object> getEstablecimientos(double latitud, double longitud, double radio);
    public boolean insertarEstabl(String nombre, String direccion, String latitud, String longitud);
    public Set convertEstabl(ArrayList<String> establecimientos);
    public boolean actualizarEstabl(String nombreEstabl, String nuevoNombre,String nuevaDireccion, String nuevaLatitud, String nuevaLongitud);
    public boolean borrarEstablecimiento(String nombreEstablecimiento);
    
    
    //ROL
    public ArrayList<String> getRoles();
    public Set convertToRoleSet(ArrayList<String> roles);
    
    
    //CATEGORÍA    
    public ArrayList<Categoria> getCategorias(String establecimiento);
    public Set convertCategorias(ArrayList<String> categorias);
    public boolean insertarCategoria(String nombre, String descripcion, Establecimiento establecimiento);
    public boolean actualizarCategoria(String nombreCategoria,String nombreEstablecimiento, String nuevoNombre,String nuevaDescrip);
    public boolean borrarCategoria(String nombreCategoria, String establCategoria);
    public Categoria getCategoria(String nombreCategoria,String establecimientoCategoria);
    
    
    //PRODUCTO
    public Producto getProducto(String nombreProducto,String establecimientoProducto);
    public ArrayList<Producto> getProductos(String establecimiento);
    public ArrayList<Producto> getProductos(String establecimiento,String categoria);
    public boolean actualizarProducto(String nombreProducto,String nombreEstablecimiento, String nuevoNombre,String nuevaDescrip,
            double nuevoPrecio, int nuevoStock,byte[] nuevaImagen, ArrayList<String> nuevasCategorias);
    public void actualizarStock(HashMap<Producto,Integer> productosCantidades);
    public boolean borrarProducto(String nombreProducto, String establProducto);
    public boolean insertarProducto(String nombre, String descripcion, double precio,int stockInicial, byte[] imagen,ArrayList<String> categorias,Establecimiento establecimiento);
    
    
    //PEDIDO
    public ArrayList<LineaPedido> getLineaPedidos(String establecimiento);
    public Pedido getPedido(int id);
    public Pedido crearPedido(String fecha,Usuario cliente, Establecimiento establ);
    public boolean insertarLineasPedido(ArrayList<LineaPedido> lineasAPagar);
    public boolean actualizarLineaPedido(LineaPedido lineaPedido);
    public ArrayList<Object> getLineasEstado(String estado);
    public ArrayList<Object> getLineasEstablecimiento(Establecimiento establecimiento,String estado);
    public boolean insertarParticipantes(Pedido pedido, ArrayList<Usuario> participantes);
    public LineaPedido getLineaPedido(int idLinea, int idPedido);

    
    //AMIGOS
    public ArrayList<Object> getAmigos(String usuario);
    public ArrayList<Object> getSolicitudesPendientes(String usuario);
    public ArrayList<Object> getSolicitudesEnviadas(String usuario);
    public int añadirAmigo(Usuario usuario, String amigo, String estado,String tipoSolicitud);
    public boolean cambiarEstadoSolicitud(Usuario amigo, Usuario usuario, boolean aceptarRechazar);
    public void borrarAmistad(Usuario usuario, Usuario amigo);


}
