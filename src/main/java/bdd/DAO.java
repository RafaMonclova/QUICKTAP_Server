/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package bdd;


import entidades.Usuario;
import java.util.ArrayList;
import java.util.Set;

/**
 *
 * @author rafam
 */
public interface DAO {
    
    public int getTotalClientes();

    public boolean insertarUsuario(String name,String mail,String password,ArrayList<String> roles); //Administrador y cliente
    public boolean insertarUsuario(String name,String mail,String password,ArrayList<String> roles,ArrayList<String> establecimientos); //Propietario y trabajador
    public Usuario getUsuario(String correo,String passw);
    public Usuario getUsuario(String nombre);
    
    public ArrayList<String> getRoles();
    public ArrayList<String> getEstablecimientos();
    
    public Set convertToRoleSet(ArrayList<String> roles);
    
    public boolean insertarEstabl(String nombre, String direccion, String coords);
    public Set convertEstabl(ArrayList<String> establecimientos);
    
    public boolean actualizarUsuario(String nombreIdentifica, 
            String nombre, String correo, String passw, ArrayList<String> roles); //Administrador y cliente
    public boolean actualizarUsuario(String nombreIdentifica, 
            String nombre, String correo, String passw, ArrayList<String> roles, ArrayList<String> establecimientos); //Propietario y trabajador
    
}
