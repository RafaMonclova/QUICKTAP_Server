/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import entidades.Usuario;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

/**
 *
 * @author rafam
 */
public class Prueba {
    
    // Create an EntityManagerFactory when you start the application
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence
            .createEntityManagerFactory("ConexionBD");
    
    public static void getCustomer(String correo, String passw) {
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
    		System.out.println(usuario.getNombre());
    	}
    	catch(NoResultException ex) {
    		ex.printStackTrace();
    	}
    	finally {
    		em.close();
    	}
    }
    
    public static void main(String[] args) {
        getCustomer("rafa@gmail.com","1234");
    }
    
}
