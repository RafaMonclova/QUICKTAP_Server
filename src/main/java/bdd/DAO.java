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
import java.util.Set;

/**
 *
 * @author rafam
 */
public interface DAO {
    
    public long getTotalClientes();
    public String getUltimoCliente();

    public boolean insertarUsuario(String name,String mail,String password,ArrayList<String> roles); //Administrador y cliente
    public boolean insertarUsuario(String name,String mail,String password,ArrayList<String> roles,ArrayList<String> establecimientos); //Propietario y trabajador
    public Usuario getUsuario(String correo,String passw);
    public Usuario getUsuario(String nombre);
    public Establecimiento getEstablecimiento(String nombre);
    
    public ArrayList<String> getRoles();
    public ArrayList<String> getEstablecimientos();
    public ArrayList<String> getEstablecimientos(String usuario);
    
    //Establecimientos cercanos
    public ArrayList<Object> getEstablecimientos(double latitud, double longitud, double radio);
    
    public ArrayList<Usuario> getUsuarios(String rol);
    public ArrayList<Categoria> getCategorias(String establecimiento);
    
    public Producto getProducto(String nombreProducto,String establecimientoProducto);
    public ArrayList<Producto> getProductos(String establecimiento);
    public ArrayList<Producto> getProductos(String establecimiento,String categoria);
    
    public ArrayList<LineaPedido> getLineaPedidos(String establecimiento);
    
    public Pedido getPedido(int id);
    
    public ArrayList<Usuario> getTrabajadores(String establecimiento);
    
    public Set convertToRoleSet(ArrayList<String> roles);
    
    public boolean insertarEstabl(String nombre, String direccion, String latitud, String longitud);
    public Set convertEstabl(ArrayList<String> establecimientos);
    public Set convertCategorias(ArrayList<String> categorias);
    
    public boolean actualizarUsuario(String nombreIdentifica, 
            String nombre, String correo, String passw, ArrayList<String> roles); //Administrador y cliente
    public boolean actualizarUsuario(String nombreIdentifica, 
            String nombre, String correo, String passw, ArrayList<String> roles, ArrayList<String> establecimientos); //Propietario y trabajador
    
    //Datos personales
    public boolean actualizarUsuario(String nombreIdentifica, 
            String nombre, String correo, String passw);
    
    //Roles y establecimientos
    public boolean actualizarUsuario(String nombreIdentifica, 
            ArrayList<String> roles,ArrayList<String> establecimientos);
    
    public boolean actualizarProducto(String nombreProducto,String nombreEstablecimiento, String nuevoNombre,String nuevaDescrip,
            double nuevoPrecio, int nuevoStock,byte[] nuevaImagen, ArrayList<String> nuevasCategorias);
    
    public boolean actualizarTrabajador(String nombreTrabajador, String nuevoNombre,String nuevoCorreo,
            String nuevaPassw, ArrayList<String> nuevosEstablecimientos);
    
    public boolean borrarProducto(String nombreProducto, String establProducto);
    public boolean borrarUsuario(String nombreUsuario);
    
    //public double getCajaHoy(String establecimiento); 
    public double getCaja(String establecimiento, String fecha);
    public ArrayList<Usuario> getUsuarios(Set<Establecimiento> establecimientos); //Devuelve una lista con los usuarios que forman parte de los establecimientos dados
    public long getNumPedidos(String estado);
    
    public boolean insertarCategoria(String nombre, String descripcion, Establecimiento establecimiento);
    public boolean insertarProducto(String nombre, String descripcion, double precio,int stockInicial, byte[] imagen,ArrayList<String> categorias,Establecimiento establecimiento);

    //PEDIDOS
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
