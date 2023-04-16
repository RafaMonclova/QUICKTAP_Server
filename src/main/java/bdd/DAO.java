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
    
    public boolean insertarUsuario(String name,String mail,String password,ArrayList<String> roles);
    public boolean insertarUsuario(String name,String mail,String password,ArrayList<String> roles,ArrayList<String> establecimientos);
    public Usuario getUsuario(String correo,String passw);
    public ArrayList<String> getRoles();
    public ArrayList<String> getEstablecimientos();
    public Set convertRole(ArrayList<String> roles);
    
    public boolean insertarEstabl(String nombre, String direccion, String coords);
    public Set convertEstabl(ArrayList<String> establecimientos);
    
}
