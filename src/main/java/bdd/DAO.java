/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package bdd;


import entidades.Categoria;
import entidades.Establecimiento;
import entidades.Usuario;
import java.awt.Image;
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
    
    public ArrayList<Usuario> getUsuarios(String rol);
    public ArrayList<Categoria> getCategorias(String establecimiento);
    
    public Set convertToRoleSet(ArrayList<String> roles);
    
    public boolean insertarEstabl(String nombre, String direccion, String coords);
    public Set convertEstabl(ArrayList<String> establecimientos);
    public Set convertCategorias(ArrayList<String> categorias);
    
    public boolean actualizarUsuario(String nombreIdentifica, 
            String nombre, String correo, String passw, ArrayList<String> roles); //Administrador y cliente
    public boolean actualizarUsuario(String nombreIdentifica, 
            String nombre, String correo, String passw, ArrayList<String> roles, ArrayList<String> establecimientos); //Propietario y trabajador
    
    public boolean actualizarUsuario(String nombreIdentifica, 
            String nombre, String correo, String passw);
    
    public boolean actualizarUsuario(String nombreIdentifica, 
            ArrayList<String> roles,ArrayList<String> establecimientos);
    
    public double getCajaHoy(String establecimiento);
    public ArrayList<Usuario> getUsuarios(Set<Establecimiento> establecimientos); //Devuelve una lista con los usuarios que forman parte de los establecimientos dados
    public long getNumPedidos(String estado);
    
    public boolean insertarCategoria(String nombre, String descripcion, Establecimiento establecimiento);
    public boolean insertarProducto(String nombre, String descripcion, double precio,int stockInicial, byte[] imagen,ArrayList<String> categorias,Establecimiento establecimiento);
}
