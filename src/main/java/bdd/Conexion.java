/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bdd;

import entidades.Establecimiento;
import entidades.Rol;
import entidades.Usuario;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.hibernate.HibernateException;
import org.hibernate.PropertyValueException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author rafam
 */
public class Conexion implements DAO {

    // Create an EntityManagerFactory when you start the application
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence
            .createEntityManagerFactory("ConexionBD");

    @Override
    public Usuario getUsuario(String correo, String passw) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        // the lowercase c refers to the object
        // :custID is a parameterized query thats value is set below
        String query = "SELECT c FROM Usuario c WHERE c.correo = :correobuscar and c.passw = :passwbuscar";

        // Issue the query and get a matching Customer
        TypedQuery<Usuario> tq = em.createQuery(query, Usuario.class);
        tq.setParameter("correobuscar", correo);
        tq.setParameter("passwbuscar", passw);

        Usuario usuario = null;
        try {
            // Get matching customer object and output
            usuario = tq.getSingleResult();
            //System.out.println(usuario.getNombre());
        } catch (NoResultException ex) {
            //ex.printStackTrace();
        } finally {
            em.close();
        }

        return usuario;
    }

    @Override
    public boolean insertarUsuario(String nombre, String correo, String passw, ArrayList<String> roles) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;
        
        boolean exito = false;
        
        try {

            Usuario usuario = new Usuario(nombre, correo, passw);
            
            usuario.setRols(convertRole(roles));
            
            tx = em.getTransaction(); // Obtener una instancia de EntityTransaction
            tx.begin(); // Iniciar una transacción
            em.persist(usuario); // Persistir el objeto usuario en el EntityManager
            tx.commit(); // Confirmar la transacción
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback(); // Revertir la transacción en caso de error
                exito = false;
                e.printStackTrace();
            }
            // Manejar la excepción adecuadamente
        } finally {
            em.close(); // Cerrar el EntityManager
        }
        
        return exito;
    }
    
    @Override
    public boolean insertarUsuario(String nombre, String correo, String passw, ArrayList<String> roles,ArrayList<String> establecimientos) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;
        
        boolean exito = false;
        
        try {

            Usuario usuario = new Usuario(nombre, correo, passw);
            
            usuario.setRols(convertRole(roles));
            usuario.setEstablecimientos(convertEstabl(establecimientos));
            
            
            tx = em.getTransaction(); // Obtener una instancia de EntityTransaction
            tx.begin(); // Iniciar una transacción
            em.persist(usuario); // Persistir el objeto usuario en el EntityManager
            tx.commit(); // Confirmar la transacción
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback(); // Revertir la transacción en caso de error
                exito = false;
                e.printStackTrace();
            }
            // Manejar la excepción adecuadamente
        } finally {
            em.close(); // Cerrar el EntityManager
        }
        
        return exito;
    }

    @Override
    public ArrayList<String> getRoles() {

        ArrayList<String> listaRoles = new ArrayList<String>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        // the lowercase c refers to the object
        // :custID is a parameterized query thats value is set below
        String query = "SELECT c FROM Rol c";

        // Issue the query and get a matching Customer
        TypedQuery<Rol> tq = em.createQuery(query, Rol.class);

        List<Rol> roles = null;
        try {
            // Get matching customer object and output
            roles = tq.getResultList();

            for (Rol rol : roles) {

                listaRoles.add(rol.getNombre());

            }

            //System.out.println(usuario.getNombre());
        } catch (NoResultException ex) {
            //ex.printStackTrace();
        } finally {
            em.close();
        }

        return listaRoles;

    }
    
    @Override
    public ArrayList<String> getEstablecimientos() {

        ArrayList<String> listaEstabl = new ArrayList<String>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        
        String query = "SELECT c FROM Establecimiento c";

        TypedQuery<Establecimiento> tq = em.createQuery(query, Establecimiento.class);

        List<Establecimiento> establecimientos = null;
        try {
            
            establecimientos = tq.getResultList();

            for (Establecimiento e : establecimientos) {

                listaEstabl.add(e.getNombre());

            }

            //System.out.println(usuario.getNombre());
        } catch (NoResultException ex) {
            //ex.printStackTrace();
        } finally {
            em.close();
        }

        return listaEstabl;

    }

    public Rol getRol(String nombreRol) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        // the lowercase c refers to the object
        // :custID is a parameterized query thats value is set below
        String query = "SELECT c FROM Rol c WHERE c.nombre = :nombrebuscar";

        // Issue the query and get a matching Customer
        TypedQuery<Rol> tq = em.createQuery(query, Rol.class);
        tq.setParameter("nombrebuscar", nombreRol);

        Rol rol = null;
        try {
            // Get matching customer object and output
            rol = tq.getSingleResult();
            //System.out.println(usuario.getNombre());
        } catch (NoResultException ex) {
            //ex.printStackTrace();
        } finally {
            em.close();
        }

        return rol;

    }

    @Override
    public Set convertRole(ArrayList<String> roles) {

        Set setRoles = new HashSet();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        for (String r : roles) {
            String query = "SELECT c FROM Rol c where c.nombre = :nombrerol";

            TypedQuery<Rol> tq = em.createQuery(query, Rol.class);
            tq.setParameter("nombrerol", r);
            
            Rol rol = null;
            try {
                
                rol = tq.getSingleResult();

                setRoles.add(rol);

            } catch (NoResultException ex) {
                //ex.printStackTrace();
            } finally {
                em.close();
            }
        }

        return setRoles;

    }
    
    @Override
    public Set convertEstabl(ArrayList<String> establecimientos) {

        Set setEstabl = new HashSet();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        for (String r : establecimientos) {
            String query = "SELECT c FROM Establecimiento c where c.nombre = :nombreestabl";

            TypedQuery<Establecimiento> tq = em.createQuery(query, Establecimiento.class);
            tq.setParameter("nombreestabl", r);
            
            Establecimiento establecimiento = null;
            try {
                
                establecimiento = tq.getSingleResult();

                setEstabl.add(establecimiento);

            } catch (NoResultException ex) {
                //ex.printStackTrace();
            } finally {
                em.close();
            }
        }

        return setEstabl;

    }

}
