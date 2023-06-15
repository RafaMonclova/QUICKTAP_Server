/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bdd;

import entidades.Amistad;
import entidades.Categoria;
import entidades.Establecimiento;
import entidades.LineaPedido;
import entidades.Pedido;
import entidades.Producto;
import entidades.Rol;
import entidades.Usuario;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.transaction.Transactional;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Clase que implementa los métodos del DAO
 * @author rafam
 */
public class Conexion implements DAO {

    //Crea un objeto EntityManagerFactory para realizar las conexiones a la base de datos
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence
            .createEntityManagerFactory("ConexionBD");

    //Listado de excepciones
    private ArrayList<String> excepciones = new ArrayList<>();

    /**
     * Obtiene la hora actual
     * @return 
     */
    public String getHora() {

        return ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("hh:mm:ss"));

    }

    /**
     * Lee las excepciones del fichero y devuelve el listado de excepciones
     * @return El listado de excepciones
     */
    public ArrayList<String> getExcepciones() {
        
        excepciones.clear();
        
        BufferedReader br = null;
        
        
        try {
            br = new BufferedReader(new FileReader("error_log.log"));
            String linea;
            
             while((linea = br.readLine()) != null){
                
                linea = br.readLine();
                excepciones.add(linea);  
                 
            }
             
            
        } catch (FileNotFoundException ex) {
            //Sin errores registrados
            
        } catch (IOException ex) {
        }
        
        finally{
            
            try {
                
                if(br != null){
                    
                    br.close();
                }
                
                
            } catch (IOException ex) {
            }
            
            
        }
        
        return excepciones;
    }

    /**
     * Añade una nueva excepción al listado de excepciones
     * @param e La excepción a añadir
     */
    public void addExcepcion(Exception e) {

        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new FileWriter("error_log.log", true));
            
            
            pw.println("[" + getHora() + "]" + e.getMessage());
            excepciones.add("[" + getHora() + "]" + e.getMessage());
            
            
            

        } catch (IOException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }
    }

    /**
     * Devuelve el total de establecimientos registrados en la BDD
     * @return El total de establecimientos registrados
     */
    @Override
    public long getTotalClientes() {

        long numUsuarios = 0;

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String jpql = "SELECT COUNT(e) FROM Establecimiento e";

        Query query = (Query) em.createQuery(jpql);

        numUsuarios = (long) query.getSingleResult();

        return numUsuarios;

    }

    /**
     * Devuelve la fecha del último registro añadido en la tabla establecimiento
     * @return La fecha del último registro de establecimiento
     */
    @Override
    public String getUltimoCliente() {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT e FROM Establecimiento e ORDER BY e.id DESC";

        TypedQuery<Establecimiento> tq = em.createQuery(query, Establecimiento.class);
        tq.setMaxResults(1); //Devuelve el primer registro, que es el último id

        Establecimiento establ = null;
        try {

            establ = tq.getSingleResult();
        } catch (NoResultException ex) {
            return "Sin establecimientos";
        } finally {
            em.close();
        }

        if (establ != null) {
            return establ.getNombre();
        } else {
            return "Sin establecimientos";
        }

    }

    /**
     * Devuelve el usuario con el correo y contraseña dados
     * @param correo El correo del usuario
     * @param passw La contraseña del usuario
     * @return Devuelve el objeto Usuario si existe
     */
    @Override
    public Usuario getUsuario(String correo, String passw) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT c FROM Usuario c WHERE c.correo = :correobuscar and c.passw = :passwbuscar";

        TypedQuery<Usuario> tq = em.createQuery(query, Usuario.class);
        tq.setParameter("correobuscar", correo);
        tq.setParameter("passwbuscar", passw);

        Usuario usuario = null;
        try {

            usuario = tq.getSingleResult();
        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return usuario;
    }

    /**
     * Devuelve el usuario con el nombre dado
     * @param nombre El nombre del usuario
     * @return Devuelve el objeto usuario si existe
     */
    @Override
    public Usuario getUsuario(String nombre) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT c FROM Usuario c WHERE c.nombre = :nombrebuscar";

        TypedQuery<Usuario> tq = em.createQuery(query, Usuario.class);
        tq.setParameter("nombrebuscar", nombre);

        Usuario usuario = null;
        try {

            usuario = tq.getSingleResult();
        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return usuario;
    }

    /**
     * Inserta un nuevo usuario en la BDD
     * @param nombre El nombre del usuario
     * @param correo El correo del usuario
     * @param passw La contraseña del usuario, encriptada
     * @param roles La lista de roles del usuario, en formato String
     * @return Devuelve un boolean que indica si el usuario se ha insertado correctamente
     */
    @Override
    public boolean insertarUsuario(String nombre, String correo, String passw, ArrayList<String> roles) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {
            Usuario usuario = new Usuario(nombre, correo, passw);

            usuario.setRols(convertToRoleSet(roles));

            tx = em.getTransaction();
            tx.begin();
            em.persist(usuario);
            tx.commit();
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }
        } finally {
            em.close();
        }

        return exito;
    }

    /**
     * Inserta un nuevo establecimiento en la BDD
     * @param nombre El nombre del establecimiento
     * @param direccion La dirección del establecimiento
     * @param latitud La latitud del establecimiento
     * @param longitud La longitud del establecimiento
     * @return Devuelve un boolean que indica si el establecimiento se ha insertado correctamente
     */
    @Override
    public boolean insertarEstabl(String nombre, String direccion, String latitud, String longitud) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            Establecimiento establecimiento = new Establecimiento(nombre, direccion, Double.parseDouble(latitud), Double.parseDouble(longitud));

            tx = em.getTransaction();
            tx.begin();
            em.persist(establecimiento);
            tx.commit();
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
    }

    /**
     * Inserta un nuevo usuario en la BDD
     * @param nombre El nombre del usuario
     * @param correo El correo del usuario
     * @param passw La contraseña del usuario
     * @param roles La lista de roles del usuario, en formato String
     * @param establecimientos La lista de establecimientos a los que pertenece el usuario, en formato String
     * @return Devuelve un boolean que indica si el usuario se ha insertado correctamente
     */
    @Override
    public boolean insertarUsuario(String nombre, String correo, String passw, ArrayList<String> roles, ArrayList<String> establecimientos) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            Usuario usuario = new Usuario(nombre, correo, passw);

            usuario.setRols(convertToRoleSet(roles));
            
            usuario.setEstablecimientos(convertEstabl(establecimientos));
            
            tx = em.getTransaction();
            tx.begin();
            em.persist(usuario);
            tx.commit();
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
    }

    /**
     * Devuelve los roles registrados
     * @return La lista de roles registrados
     */
    @Override
    public ArrayList<String> getRoles() {

        ArrayList<String> listaRoles = new ArrayList<String>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT c FROM Rol c";

        TypedQuery<Rol> tq = em.createQuery(query, Rol.class);

        List<Rol> roles = null;
        try {

            roles = tq.getResultList();

            for (Rol rol : roles) {

                listaRoles.add(rol.getNombre());

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaRoles;

    }

    /**
     * Devuelve la lista de establecimientos registrados
     * @return La lista de establecimientos registrados
     */
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

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaEstabl;

    }

    /**
     * Devuelve el rol con el nombre dado
     * @param nombreRol El nombre del rol
     * @return El objeto Rol
     */
    public Rol getRol(String nombreRol) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT c FROM Rol c WHERE c.nombre = :nombrebuscar";

        TypedQuery<Rol> tq = em.createQuery(query, Rol.class);
        tq.setParameter("nombrebuscar", nombreRol);

        Rol rol = null;
        try {

            rol = tq.getSingleResult();

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return rol;

    }

    /**
     * Convierte la lista de roles en formato String a un Set de Rol
     * @param roles La lista de String
     * @return El Set de Rol
     */
    @Override
    public Set<Rol> convertToRoleSet(ArrayList<String> roles) {

        Set<Rol> setRoles = new HashSet();

        for (String r : roles) {

            EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

            String query = "SELECT c FROM Rol c where c.nombre = :nombrerol";

            TypedQuery<Rol> tq = em.createQuery(query, Rol.class);
            tq.setParameter("nombrerol", r);

            Rol rol = null;
            try {

                rol = tq.getSingleResult();

                setRoles.add(rol);

            } catch (NoResultException ex) {
                addExcepcion(ex);
            } finally {
                em.close();
            }
        }

        return setRoles;

    }

    /**
     * Convierte la lista de establecimientos en formato String a un Set de Establecimiento
     * @param establecimientos La lista de String
     * @return El Set de Establecimiento
     */
    @Override
    public Set<Establecimiento> convertEstabl(ArrayList<String> establecimientos) {

        Set<Establecimiento> setEstabl = new HashSet();

        for (String r : establecimientos) {

            EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

            String query = "SELECT c FROM Establecimiento c where c.nombre = :nombreestabl";

            TypedQuery<Establecimiento> tq = em.createQuery(query, Establecimiento.class);
            tq.setParameter("nombreestabl", r);

            Establecimiento establecimiento = null;
            try {

                establecimiento = tq.getSingleResult();

                setEstabl.add(establecimiento);

            } catch (NoResultException ex) {
                addExcepcion(ex);
            } finally {
                em.close();
            }
        }

        return setEstabl;

    }

    /**
     * Convierte la lista de categorías en formato String a un Set de Categoría
     * @param categorias La lista de String
     * @return El Set de Categoría
     */
    @Override
    public Set<Categoria> convertCategorias(ArrayList<String> categoriasEstablecimientos) {

        Set<Categoria> setCateg = new HashSet();

        for (String c : categoriasEstablecimientos) {

            EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

            String[] campos = c.split("\\;");
            String nombreCategoria = campos[0];
            String nombreEstabl = campos[1];
            
            String query = "SELECT c FROM Categoria c where c.nombre = :nombrecateg and c.establecimiento.nombre = :establcategoria";

            TypedQuery<Categoria> tq = em.createQuery(query, Categoria.class);
            tq.setParameter("nombrecateg", nombreCategoria);
            tq.setParameter("establcategoria", nombreEstabl);

            Categoria categoria = null;
            try {

                categoria = tq.getSingleResult();

                setCateg.add(categoria);

            } catch (NoResultException ex) {
                addExcepcion(ex);
            } finally {
                em.close();
            }
        }

        return setCateg;

    }

    /**
     * Actualiza los datos de un usuario en la BDD
     * @param nombreIdentifica El nombre que identifica al usuario a modificar
     * @param nombre Nuevo nombre
     * @param correo Nuevo correo
     * @param passw Nueva contraseña
     * @param roles Nuevos roles
     * @return Devuelve un boolean que indica si se ha modificado correctamente
     */
    @Override
    public boolean actualizarUsuario(String nombreIdentifica, String nombre, String correo, String passw, ArrayList<String> roles) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            // Obtiene el usuario por nomnre (nombre único)
            TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u WHERE u.nombre = :nombre", Usuario.class);
            query.setParameter("nombre", nombreIdentifica);
            Usuario usuario = (Usuario) query.getSingleResult();

            usuario.setNombre(nombre);
            usuario.setCorreo(correo);
            usuario.setPassw(passw);

            Set<Rol> setRoles = convertToRoleSet(roles);

            usuario.setRols(setRoles);

            // Guarda los cambios en la base de datos
            em.merge(usuario);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
    }

    /**
     * Actualiza los datos de un usuario en la BDD
     * @param nombreIdentifica El nombre que identifica al usuario a modificar
     * @param nombre Nuevo nombre
     * @param correo Nuevo correo
     * @param passw Nueva contraseña
     * @param roles Nuevos roles
     * @param establecimientos Nuevos establecimientos
     * @return Devuelve un boolean que indica si se ha modificado correctamente
     */
    @Override
    public boolean actualizarUsuario(String nombreIdentifica, String nombre, String correo, String passw, ArrayList<String> roles, ArrayList<String> establecimientos) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            // Obtiene el usuario por nomnre (nombre único)
            TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u WHERE u.nombre = :nombre", Usuario.class);
            query.setParameter("nombre", nombreIdentifica);
            Usuario usuario = (Usuario) query.getSingleResult();

            usuario.setNombre(nombre);
            usuario.setCorreo(correo);
            usuario.setPassw(passw);
            usuario.setRols(convertToRoleSet(roles));
            usuario.setEstablecimientos(convertEstabl(establecimientos)); //Cambia sus establecimientos

            // Guarda los cambios en la base de datos
            em.merge(usuario);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
    }

    /**
     * Actualiza los datos del usuario en la BDD
     * @param nombreIdentifica El nombre que identifica al usuario a modificar
     * @param roles Nuevos roles
     * @param establecimientos Nuevos establecimientos
     * @return Devuelve un boolean que indica si se ha modificado correctamente
     */
    @Override
    public boolean actualizarUsuario(String nombreIdentifica, ArrayList<String> roles, ArrayList<String> establecimientos) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            // Obtiene el usuario por nomnre (nombre único)
            TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u WHERE u.nombre = :nombre", Usuario.class);
            query.setParameter("nombre", nombreIdentifica);
            Usuario usuario = (Usuario) query.getSingleResult();

            usuario.setRols(convertToRoleSet(roles));
            usuario.setEstablecimientos(convertEstabl(establecimientos)); //Cambia sus establecimientos

            // Guarda los cambios en la base de datos
            em.merge(usuario);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;

    }

    /**
     * Actualiza los datos personales del usuario dado en la BDD
     * @param nombreIdentifica El nombre que identifica al usuario a modificar
     * @param nombre Nuevo nombre
     * @param correo Nuevo correo
     * @param passw Nueva contraseña
     * @return Devuelve un boolean que indica si se ha modificado correctamente
     */
    @Override
    public boolean actualizarUsuario(String nombreIdentifica, String nombre, String correo, String passw) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            // Obtiene el usuario por nomnre (nombre único)
            TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u WHERE u.nombre = :nombre", Usuario.class);
            query.setParameter("nombre", nombreIdentifica);
            Usuario usuario = (Usuario) query.getSingleResult();

            usuario.setNombre(nombre);
            usuario.setCorreo(correo);
            usuario.setPassw(passw);

            // Guarda los cambios en la base de datos
            em.merge(usuario);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
    }

    /**
     * Obtiene la caja del establecimiento en la fecha dada
     * @param establecimiento El nombre del establecimiento a consultar
     * @param fecha La fecha sobre la que hacer la consulta
     * @return Devuelve la cantidad generada para ese establecimiento en esa fecha
     */
    @Override
    public double getCaja(String establecimiento, String fecha) {

        double caja = 0;

        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaDate = null;
        try {
            fechaDate = formato.parse(fecha);
        } catch (ParseException e) {
        }

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String jpql = "SELECT SUM(lp.cantidad * p.precio) AS precio_total\n"
                + "FROM LineaPedido lp\n"
                + "JOIN lp.producto p\n"
                + "JOIN lp.pedido pe\n"
                + "JOIN pe.establecimiento e\n"
                + "WHERE pe.fecha = :fecha and e.nombre = :nombreestabl";

        Query query = (Query) em.createQuery(jpql);
        query.setParameter("fecha", fechaDate);
        query.setParameter("nombreestabl", establecimiento);
        try {
            caja = (double) query.getSingleResult();
        } catch (NoResultException | NullPointerException ex) {
            //No hay caja generada en la fecha
        } finally {
            em.close();
        }

        return caja;

    }

    /**
     * Devuelve los usuarios que pertenecen a alguno de los establecimientos dados
     * @param establecimientos Los establecimientos donde consultar
     * @return Devuelve el listado de usuarios
     */
    @Override
    public ArrayList<Usuario> getUsuarios(Set<Establecimiento> establecimientos) {

        ArrayList<Usuario> listaUsuarios = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT DISTINCT u "
                + "FROM Usuario u "
                + "JOIN u.establecimientos e "
                + "WHERE e IN :establecimientos";

        TypedQuery<Usuario> tq = em.createQuery(query, Usuario.class);
        tq.setParameter("establecimientos", establecimientos);

        List<Usuario> usuarios = null;
        try {

            usuarios = tq.getResultList();
            listaUsuarios.addAll(usuarios);

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaUsuarios;

    }

    /**
     * Devuelve los usuarios con el rol dado
     * @param rol El rol para hacer la consulta
     * @return El listado de usuarios
     */
    @Override
    public ArrayList<Usuario> getUsuarios(String rol) {

        ArrayList<Usuario> listaUsuarios = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT DISTINCT u FROM Usuario u JOIN u.rols r WHERE r.nombre = :nombreRol";

        TypedQuery<Usuario> tq = em.createQuery(query, Usuario.class);
        tq.setParameter("nombreRol", rol);

        List<Usuario> usuarios = null;
        try {

            usuarios = tq.getResultList();

            for (Usuario u : usuarios) {

                listaUsuarios.add(u);

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaUsuarios;

    }

    /**
     * Inserta una nueva categoría en la BDD
     * @param nombre El nombre de la categoría
     * @param descripcion La descripción de la categoría
     * @param establecimiento El establecimiento al que se añade la categoría
     * @return Devuelve un boolean que indica si se ha insertado correctamente
     */
    @Override
    public boolean insertarCategoria(String nombre, String descripcion, Establecimiento establecimiento) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            Categoria categoria = new Categoria(nombre, descripcion);
            categoria.setEstablecimiento(establecimiento);

            tx = em.getTransaction();
            tx.begin();
            em.persist(categoria);
            tx.commit();
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;

    }

    /**
     * Devuelve el establecimiento con el nombre dado
     * @param nombre El nombre del establecimiento a buscar
     * @return El objeto Establecimiento
     */
    @Override
    public Establecimiento getEstablecimiento(String nombre) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT c FROM Establecimiento c WHERE c.nombre = :nombrebuscar";

        TypedQuery<Establecimiento> tq = em.createQuery(query, Establecimiento.class);
        tq.setParameter("nombrebuscar", nombre);

        Establecimiento establecimiento = null;
        try {

            establecimiento = tq.getSingleResult();
        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return establecimiento;

    }

    /**
     * Devuelve la lista de categorías de un establecimiento
     * @param establecimiento El establecimiento sobre el que hacer la consulta
     * @return El listado de categorías
     */
    @Override
    public ArrayList<Categoria> getCategorias(String establecimiento) {

        ArrayList<Categoria> listaCategorias = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT c FROM Categoria c WHERE c.establecimiento.nombre = :nombreEstabl";

        TypedQuery<Categoria> tq = em.createQuery(query, Categoria.class);
        tq.setParameter("nombreEstabl", establecimiento);

        List<Categoria> categorias = null;
        try {

            categorias = tq.getResultList();

            for (Categoria c : categorias) {

                listaCategorias.add(c);

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaCategorias;

    }

    /**
     * Inserta un nuevo producto en la BDD
     * @param nombre Nombre del producto
     * @param descripcion Descripción del producto
     * @param precio Precio del producto
     * @param stockInicial Stock del producto
     * @param imagen Imagen del producto
     * @param categorias Categorías del producto
     * @param establecimiento Establecimiento del producto
     * @return Devuelve un boolean que indica si se ha insertado correctamente
     */
    @Override
    public boolean insertarProducto(String nombre, String descripcion, double precio, int stockInicial, byte[] imagen, ArrayList<String> categorias, Establecimiento establecimiento) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            Producto producto = new Producto(nombre, descripcion, precio, imagen, stockInicial);
            producto.setEstablecimiento(establecimiento);
            
            
            producto.setCategorias(convertCategorias(categorias));

            
            tx = em.getTransaction();
            tx.begin();
            em.persist(producto);
            tx.commit();
            exito = true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
                e.printStackTrace();
            }

        } finally {
            em.close();
        }

        return exito;

    }

    /**
     * Devuelve los establecimientos del usuario dado
     * @param usuario El usuario sobre el que hacer la consulta
     * @return El listado de establecimientos
     */
    @Override
    public ArrayList<String> getEstablecimientos(String usuario) {

        ArrayList<String> listaEstabl = new ArrayList<String>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT e FROM Establecimiento e JOIN e.usuarios u WHERE u.nombre = :usuario";

        TypedQuery<Establecimiento> tq = em.createQuery(query, Establecimiento.class);
        tq.setParameter("usuario", usuario);

        List<Establecimiento> establecimientos = null;
        try {

            establecimientos = tq.getResultList();

            for (Establecimiento e : establecimientos) {

                listaEstabl.add(e.getNombre());

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaEstabl;

    }

    /**
     * Devuelve el listado de productos de un establecimiento dado
     * @param establecimiento El establecimiento sobre el que hacer la consulta
     * @return El listado de productos
     */
    @Override
    public ArrayList<Producto> getProductos(String establecimiento) {

        ArrayList<Producto> listaProductos = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT p FROM Producto p WHERE p.establecimiento.nombre = :nombreEstabl";

        TypedQuery<Producto> tq = em.createQuery(query, Producto.class);
        tq.setParameter("nombreEstabl", establecimiento);

        List<Producto> productos = null;
        try {

            productos = tq.getResultList();

            for (Producto p : productos) {

                listaProductos.add(p);

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaProductos;

    }

    /**
     * Devuelve el listado de productos de un establecimiento dado y una categoría en concreto
     * @param establecimiento El establecimiento sobre el que hacer la consulta
     * @param categoria La categoría sobre la que hacer la consulta
     * @return El listado de productos
     */
    @Override
    public ArrayList<Producto> getProductos(String establecimiento, String categoria) {

        ArrayList<Producto> listaProductos = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT p FROM Producto p JOIN p.categorias c WHERE p.establecimiento.nombre = :nombreEstabl "
                + "and c.nombre = :nombreCategoria";

        TypedQuery<Producto> tq = em.createQuery(query, Producto.class);
        tq.setParameter("nombreEstabl", establecimiento);
        tq.setParameter("nombreCategoria", categoria);

        List<Producto> productos = null;
        try {

            productos = tq.getResultList();

            for (Producto p : productos) {

                listaProductos.add(p);

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaProductos;

    }

    /**
     * Devuelve el producto con el nombre dado y del establecimiento dado
     * @param nombreProducto El nombre del producto
     * @param establecimientoProducto El establecimiento sobre el que hacer la consulta
     * @return El objeto Producto
     */
    @Override
    public Producto getProducto(String nombreProducto, String establecimientoProducto) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT p FROM Producto p WHERE p.nombre = :nombreproducto and p.establecimiento.nombre = :establproducto";

        TypedQuery<Producto> tq = em.createQuery(query, Producto.class);
        tq.setParameter("nombreproducto", nombreProducto);
        tq.setParameter("establproducto", establecimientoProducto);

        Producto producto = null;
        try {

            producto = tq.getSingleResult();
        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return producto;

    }

    /**
     * Actualiza los datos de un producto en la BDD
     * @param nombreProducto Nombre para identificar el producto a modificar
     * @param nombreEstablecimiento Nuevo establecimiento
     * @param nuevoNombre Nuevo nombre
     * @param nuevaDescrip Nueva descripción
     * @param nuevoPrecio Nuevo precio
     * @param nuevoStock Nuevo stock
     * @param nuevaImagen Nueva imagen
     * @param nuevasCategorias Nuevas categorías
     * @return Devuelve un boolean que indica si se ha modificado correctamente
     */
    @Override
    public boolean actualizarProducto(String nombreProducto, String nombreEstablecimiento, String nuevoNombre, String nuevaDescrip,
            double nuevoPrecio, int nuevoStock, byte[] nuevaImagen, ArrayList<String> nuevasCategorias) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            // Obtiene el producto por nombre y establecimiento (un establecimiento no puede tener varios productos con el mismo nombre)
            TypedQuery<Producto> query = em.createQuery("SELECT p FROM Producto p WHERE p.nombre = :nombreproducto and p.establecimiento.nombre = :nombreestabl", Producto.class);
            query.setParameter("nombreproducto", nombreProducto);
            query.setParameter("nombreestabl", nombreEstablecimiento);

            Producto producto = (Producto) query.getSingleResult();

            producto.setNombre(nuevoNombre);
            producto.setDescripcion(nuevaDescrip);
            producto.setPrecio(nuevoPrecio);
            producto.setStock(nuevoStock);
            producto.setImagen(nuevaImagen);
            producto.setCategorias(convertCategorias(nuevasCategorias));

            // Guarda los cambios en la base de datos
            em.merge(producto);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;

    }

    /**
     * Obtiene las líneas de pedido del establecimiento dado
     * @param establecimiento El establecimiento sobre el que hacer la consulta
     * @return El listado de líneas de pedido
     */
    @Transactional
    @Override
    public ArrayList<LineaPedido> getLineaPedidos(String establecimiento) {

        ArrayList<LineaPedido> listaLineasPedidos = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        
        String jpql = "SELECT lp FROM LineaPedido lp "
                + "JOIN lp.pedido p "
                + "JOIN p.establecimiento e "
                + "WHERE e.nombre = :nombreestabl and lp.estado = :estado";

        Query query = (Query) em.createQuery(jpql);
        query.setParameter("estado", "EN PREPARACIÓN");
        query.setParameter("nombreestabl", establecimiento);

        List<LineaPedido> lineasPedidos = null;
        try {

            lineasPedidos = query.getResultList();

            for (LineaPedido p : lineasPedidos) {

                listaLineasPedidos.add(p);

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaLineasPedidos;

    }

    /**
     * Devuelve el listado de trabajadores de un establecimiento
     * @param establecimiento El establecimiento sobre el que hacer la consulta
     * @return El listado de trabajadores
     */
    @Override
    public ArrayList<Usuario> getTrabajadores(String establecimiento) {

        ArrayList<Usuario> listaTrabajadores = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        //String query = "SELECT DISTINCT u FROM Usuario u JOIN u.establecimientos e WHERE e.nombre = :nombreestabl";

        String query = "SELECT DISTINCT u FROM Usuario u JOIN u.establecimientos e WHERE e.nombre = :nombreestabl AND EXISTS ( SELECT r FROM u.rols r WHERE r.nombre = 'trabajador' ) AND NOT EXISTS ( SELECT r FROM u.rols r WHERE r.nombre = 'propietario')";
                
        
        TypedQuery<Usuario> tq = em.createQuery(query, Usuario.class);
        tq.setParameter("nombreestabl", establecimiento);

        List<Usuario> usuarios = null;
        try {

            usuarios = tq.getResultList();

            for (Usuario u : usuarios) {

                listaTrabajadores.add(u);

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaTrabajadores;

    }
    
    /**
     * Devuelve el listado de trabajadores y propietarios de un establecimiento
     * @param establecimiento El establecimiento sobre el que hacer la consulta
     * @return El listado de trabajadores y propietarios
     */
    @Override
    public ArrayList<Usuario> getTrabajadoresPropietarios(String establecimiento) {

        ArrayList<Usuario> listaTrabajadores = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT DISTINCT u FROM Usuario u JOIN u.establecimientos e WHERE e.nombre = :nombreestabl";

        TypedQuery<Usuario> tq = em.createQuery(query, Usuario.class);
        tq.setParameter("nombreestabl", establecimiento);

        List<Usuario> usuarios = null;
        try {

            usuarios = tq.getResultList();

            for (Usuario u : usuarios) {
                
                listaTrabajadores.add(u);

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaTrabajadores;

    }

    /**
     * Actualiza los datos de un trabajador en la BDD
     * @param nombreTrabajador Nombre para identificar al trabajador a modificar
     * @param nuevoNombre Nuevo nombre
     * @param nuevoCorreo Nuevo correo
     * @param nuevaPassw Nueva contraseña
     * @param nuevosEstablecimientos Nuevos establecimientos
     * @return Devuelve un boolean que indica si se ha modificado correctamente
     */
    @Override
    public boolean actualizarTrabajador(String nombreTrabajador, String nuevoNombre, String nuevoCorreo,
            String nuevaPassw, ArrayList<String> nuevosEstablecimientos) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            // Obtiene el producto por nombre y establecimiento (un establecimiento no puede tener varios productos con el mismo nombre)
            TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u WHERE u.nombre = :nombreusuario", Usuario.class);
            query.setParameter("nombreusuario", nombreTrabajador);

            Usuario usuario = (Usuario) query.getSingleResult();

            usuario.setNombre(nuevoNombre);
            usuario.setCorreo(nuevoCorreo);
            usuario.setPassw(nuevaPassw);

            usuario.setEstablecimientos(convertEstabl(nuevosEstablecimientos));

            // Guarda los cambios en la base de datos
            em.merge(usuario);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;

    }

    /**
     * Borra un producto de la BDD
     * @param nombreProducto El nombre del producto
     * @param establProducto El establecimiento al que pertenece el producto
     * @return Devuelve un boolean que indica si se ha borrado correcamente
     */
    @Override
    public boolean borrarProducto(String nombreProducto, String establProducto) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        String query = "SELECT p FROM Producto p WHERE p.nombre = :nombreproducto and p.establecimiento.nombre = :establproducto";

        TypedQuery<Producto> tq = em.createQuery(query, Producto.class);
        tq.setParameter("nombreproducto", nombreProducto);
        tq.setParameter("establproducto", establProducto);

        Producto producto = null;
        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            producto = tq.getSingleResult();

            //Borra el producto
            em.remove(producto);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }
        } finally {
            em.close();
        }

        return exito;

    }

    /**
     * Borra un usuario de la BDD
     * @param nombreUsuario El nombre del usuario
     * @return Devuelve un boolean que indica si se ha borrado correcamente
     */
    @Override
    public boolean borrarUsuario(String nombreUsuario) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        String query = "SELECT u FROM Usuario u WHERE u.nombre = :nombreusuario";

        TypedQuery<Usuario> tq = em.createQuery(query, Usuario.class);
        tq.setParameter("nombreusuario", nombreUsuario);

        Usuario usuario = null;
        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            usuario = tq.getSingleResult();

            //Borra el producto
            em.remove(usuario);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }
        } finally {
            em.close();
        }

        return exito;

    }

    /**
     * Crea un nuevo pedido en la BDD
     * @param fecha La fecha de creación del pedido
     * @param cliente El cliente que inicia el pedido
     * @param establ El establecimiento donde se crea el pedido
     * @return El objeto Pedido creado
     */
    @Override
    public Pedido crearPedido(String fecha, Usuario cliente, Establecimiento establ) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        Pedido pedido = null;
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaDate = null;

        try {

            fechaDate = formato.parse(fecha);

            pedido = new Pedido(fechaDate, cliente, establ);

            tx = em.getTransaction();
            tx.begin();
            em.persist(pedido);
            tx.commit();

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                addExcepcion(e);
            }
        } finally {
            em.close();
        }

        return pedido;

    }

    /**
     * Devuelve un pedido a partir de su id
     * @param id El id del pedido
     * @return El objeto Pedido
     */
    @Override
    public Pedido getPedido(int id) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        Pedido pedido = em.find(Pedido.class, id);
        return pedido;

    }

    /**
     * Devuelve los establecimientos dentro de un radio de búsqueda dado
     * @param latitud La latitud del usuario
     * @param longitud La longitud del usuario
     * @param radioBusqueda El radio de búsqueda dado
     * @return Los establecimientos que se encuentran dentro del radio
     */
    @Override
    public ArrayList<Object> getEstablecimientos(double latitud, double longitud, double radioBusqueda) {

        ArrayList<Object> listaEstabl = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        
        //Obtiene el listado de establecimientos en el radio, ordenados de los más cercanos a los más lejanos
        String jpqlQuery = "SELECT r FROM Establecimiento r WHERE "
                + "(ACOS(SIN(RADIANS(r.latitud)) * SIN(RADIANS(:userLat)) + "
                + "COS(RADIANS(r.latitud)) * COS(RADIANS(:userLat)) * "
                + "COS(RADIANS(r.longitud) - RADIANS(:userLon))) * 6371000) <= :radioMaximo "
                + "ORDER BY "
                + "(ACOS(SIN(RADIANS(r.latitud)) * SIN(RADIANS(:userLat)) + "
                + "COS(RADIANS(r.latitud)) * COS(RADIANS(:userLat)) * "
                + "COS(RADIANS(r.longitud) - RADIANS(:userLon))) * 6371000) DESC";

        TypedQuery<Establecimiento> query = em.createQuery(jpqlQuery, Establecimiento.class);

        query.setParameter("userLat", latitud);
        query.setParameter("userLon", longitud);
        query.setParameter("radioMaximo", radioBusqueda);

        List<Establecimiento> establecimientos = null;
        try {

            establecimientos = query.getResultList();

            for (Establecimiento e : establecimientos) {

                ArrayList<Object> camposEstabl = new ArrayList<>();
                camposEstabl.add(e.getNombre());
                camposEstabl.add(e.getDireccion());
                camposEstabl.add(e.getLatitud());
                camposEstabl.add(e.getLongitud());

                listaEstabl.add(camposEstabl);

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaEstabl;

    }

    /**
     * Devuelve los amigos de un usuario
     * @param usuario El usuario sobre el que hacer la consulta
     * @return El listado de amigos
     */
    @Override
    public ArrayList<Object> getAmigos(String usuario) {

        ArrayList<Object> listaUsuarios = new ArrayList<>();

        Usuario u = getUsuario(usuario);

        for (Amistad amistad : u.getAmigos()) {
            ArrayList<Object> camposAmigo = new ArrayList<>();

            if (!amistad.getEstado().equals("Pendiente")) { //Excluye las solicitudes pendientes
                camposAmigo.add(amistad.getAmigo().getNombre());
                camposAmigo.add(amistad.getEstado());

                listaUsuarios.add(camposAmigo);
            }

        }

        return listaUsuarios;

    }

    /**
     * Envía una solicitud de amistad a un usuario
     * @param usuario El usuario que envía la solicitud
     * @param amigo El usuario que recibe la solicitud
     * @param estado El estado de la solicitud
     * @param tipoSolicitud El tipo de solicitud
     * @return Devuelve un código que indica el resultado de la operación. 0: ejecución correcta; 1: no existe el usuario amigo; 2: ya hay una solicitud en curso; 3: el usuario ya es tu amigo
     */
    @Override
    public int añadirAmigo(Usuario usuario, String amigo, String estado, String tipoSolicitud) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        int exito = 0;

        try {

            //Si es de tipo 'Envio', significa que el cliente envía la petición
            //Se comprueban varios casos al enviar la petición
            if (tipoSolicitud.equals("Envio")) {

                if (existeAmistad(usuario.getNombre(), amigo, "Pendiente")
                        || existeAmistad(amigo, usuario.getNombre(), "Pendiente")) {
                    exito = 2;
                } else if (existeAmistad(usuario.getNombre(), amigo, "Aceptado")
                        || existeAmistad(amigo, usuario.getNombre(), "Aceptado")) {
                    exito = 3;
                } else {
                    Usuario amigoU = getUsuario(amigo);
                    Amistad amistad = new Amistad(usuario, amigoU, estado);

                    tx = em.getTransaction();
                    tx.begin();
                    em.persist(amistad);
                    tx.commit();
                    exito = 0;
                }

            } //Si es 'Respuesta', significa que el cliente responde a una petición recibida de otro cliente
            //No se realiza la comprobación en este caso
            else if (tipoSolicitud.equals("Respuesta")) {

                Usuario amigoU = getUsuario(amigo);
                Amistad amistad = new Amistad(usuario, amigoU, estado);

                tx = em.getTransaction();
                tx.begin();
                em.persist(amistad);
                tx.commit();
                exito = 0;

            }

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();

                

                if (existeAmistad(usuario.getNombre(), amigo, "Pendiente")
                        || existeAmistad(amigo, usuario.getNombre(), "Pendiente")) {
                    exito = 2;
                } else if (existeAmistad(usuario.getNombre(), amigo, "Aceptado")
                        || existeAmistad(amigo, usuario.getNombre(), "Aceptado")) {
                    exito = 3;
                } else {
                    exito = 1;
                }

                addExcepcion(e);
            }
        } finally {
            em.close();
        }

        return exito;

    }

    /**
     * Comprueba si dos usuarios ya son amigos
     * @param usuario Primer usuario
     * @param amigo Segundo usuario
     * @param estado El estado de la amistad
     * @return Devuelve un boolean que indica si los usuarios ya son amigos
     */
    public boolean existeAmistad(String usuario, String amigo, String estado) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        boolean existe = false;

        String query = "SELECT a FROM Amistad a "
                + "WHERE a.usuario.nombre = :usuario AND a.amigo.nombre = :amigo and a.estado = :estado";

        TypedQuery<Amistad> tq = em.createQuery(query, Amistad.class);
        tq.setParameter("usuario", usuario);
        tq.setParameter("amigo", amigo);
        tq.setParameter("estado", estado);

        Amistad amistad = null;
        try {

            amistad = tq.getSingleResult();
            existe = true;
        } catch (NoResultException ex) {
            addExcepcion(ex);
            existe = false;
        } finally {
            em.close();
        }

        return existe;

    }

    /**
     * Devuelve un listado de solicitudes con estado pendiente de un usuario dado
     * @param usuario El usuario sobre el que hacer la consulta
     * @return El listado de solicitudes del usuario
     */
    @Override
    public ArrayList<Object> getSolicitudesPendientes(String usuario) {

        ArrayList<Object> listaSolicitudes = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT a FROM Amistad a WHERE a.amigo.nombre = :usuario AND a.estado = 'Pendiente'";

        TypedQuery<Amistad> tq = em.createQuery(query, Amistad.class);
        tq.setParameter("usuario", usuario);

        List<Amistad> amistades = null;
        try {

            amistades = tq.getResultList();

            for (Amistad a : amistades) {

                ArrayList<Object> camposAmistad = new ArrayList<>();
                camposAmistad.add(a.getUsuario().getNombre());
                camposAmistad.add(a.getEstado());

                listaSolicitudes.add(camposAmistad);

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaSolicitudes;

    }

    /**
     * Devuelve las solicitudes enviadas por el usuario dado
     * @param usuario El usuario sobre el que hacer la consulta
     * @return El listado de solicitudes enviadas
     */
    @Override
    public ArrayList<Object> getSolicitudesEnviadas(String usuario) {

        ArrayList<Object> listaSolicitudes = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT a FROM Amistad a WHERE a.usuario.nombre = :usuario AND a.estado = 'Pendiente'";

        TypedQuery<Amistad> tq = em.createQuery(query, Amistad.class);
        tq.setParameter("usuario", usuario);

        List<Amistad> amistades = null;
        try {

            amistades = tq.getResultList();

            for (Amistad a : amistades) {

                ArrayList<Object> camposAmistad = new ArrayList<>();
                camposAmistad.add(a.getAmigo().getNombre());
                camposAmistad.add(a.getEstado());

                listaSolicitudes.add(camposAmistad);

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaSolicitudes;

    }

    /**
     * Cambia el estado de la solicitud de amistad
     * @param remitente El usuario que envía la solicitud
     * @param usuario El usuario que la recibe
     * @param aceptarRechazar Indicador si acepta o rechaza
     * @return Devuelve un boolean que indica si se ha cambiado el estado de la solicitud correctamente
     */
    @Override
    public boolean cambiarEstadoSolicitud(Usuario remitente, Usuario usuario, boolean aceptarRechazar) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            String query = "SELECT a FROM Amistad a WHERE a.amigo.nombre = :yo AND a.usuario.nombre = :remitente";

            TypedQuery<Amistad> tq = em.createQuery(query, Amistad.class);
            tq.setParameter("yo", usuario.getNombre());
            tq.setParameter("remitente", remitente.getNombre());

            Amistad amistad = (Amistad) tq.getSingleResult();


            if (aceptarRechazar) {
                amistad.setEstado("Aceptado");
                //Añade el registro para completar la amistad
                añadirAmigo(usuario, remitente.getNombre(), "Aceptado", "Respuesta");
            }

            // Guarda los cambios en la base de datos
            em.merge(amistad);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;

    }

    /**
     * Borra una amistad de la BDD
     * @param usuario Primer usuario
     * @param amigo Segundo usuario
     */
    @Override
    public void borrarAmistad(Usuario usuario, Usuario amigo) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        String query = "SELECT a FROM Amistad a WHERE a.amigo.nombre = :yo AND a.usuario.nombre = :remitente";

        TypedQuery<Amistad> tq = em.createQuery(query, Amistad.class);
        tq.setParameter("yo", amigo.getNombre());
        tq.setParameter("remitente", usuario.getNombre());

        Amistad amistad = null;

        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            amistad = tq.getSingleResult();

            //Borra la amistad
            em.remove(amistad);
            // Confirma la transacción
            em.getTransaction().commit();


        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                addExcepcion(e);
            }
        } finally {
            em.close();
        }

    }

    /**
     * Inserta líneas de pedido en la BDD
     * @param lineasAPagar Las líneas a insertar
     * @return Devuelve un boolean que indica si se han insertado correctamente
     */
    @Override
    public boolean insertarLineasPedido(ArrayList<LineaPedido> lineasAPagar) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            tx = em.getTransaction();
            tx.begin();

            
            for (LineaPedido lineaPedido : lineasAPagar) {
                Pedido pedido = em.getReference(Pedido.class, lineaPedido.getPedido().getId());
                lineaPedido.setPedido(pedido);
                em.persist(lineaPedido);
            }

            tx.commit();

            exito = true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;

    }
    
    /**
     * Actualiza una línea de pedido existente en la BDD
     * @param lineaPedido La línea de pedido a actualizar
     * @return Devuelve un boolean que indica si se ha modificado correctamente
     */
    @Override
    public boolean actualizarLineaPedido(LineaPedido lineaPedido){
        
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            // Iniciar la transacción
            em.getTransaction().begin();


            // Guarda los cambios en la base de datos
            em.merge(lineaPedido);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
        
    }

    /**
     * Devuelve las líneas de pedido con el estado dado
     * @param estado El estado sobre el que consultar
     * @return La lista de líneas
     */
    @Override
    public ArrayList<Object> getLineasEstado(String estado) {
        
        ArrayList<Object> listaLineasPedido = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT lp FROM LineaPedido lp WHERE lp.estado = :estado";

        TypedQuery<LineaPedido> tq = em.createQuery(query, LineaPedido.class);
        tq.setParameter("estado", estado);

        List<LineaPedido> lineas = null;
        try {

            lineas = tq.getResultList();

            for (LineaPedido lp : lineas) {

                ArrayList<Object> camposLinea = new ArrayList<>();
                
                camposLinea.add(lp.getPedido().getId());
                camposLinea.add(lp.getCliente().getNombre());
                camposLinea.add(lp.getProducto().getNombre());
                camposLinea.add(lp.getCantidad());
                camposLinea.add(lp.getEstado());
                
                listaLineasPedido.add(camposLinea);

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaLineasPedido;
        
    }
    
    /**
     * Devuelve las líneas de un establecimiento en concreto, con el estado dado
     * @param establecimiento El establecimiento sobre el que hacer la consulta
     * @param estado El estado sobre el que hacer la consulta
     * @return La lista de líneas
     */
    @Override
    public ArrayList<Object> getLineasEstablecimiento(Establecimiento establecimiento, String estado) {
        
        ArrayList<Object> listaLineasPedido = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "";
        TypedQuery<LineaPedido> tq = null;
        
        if(estado.equals("TODAS")){
            query = "SELECT lp FROM LineaPedido lp WHERE lp.pedido.establecimiento = :establecimiento";
            tq = em.createQuery(query, LineaPedido.class);
            tq.setParameter("establecimiento", establecimiento);
        }
        else if(estado.equals("DISPONIBLES")){
            query = "SELECT lp FROM LineaPedido lp WHERE lp.pedido.establecimiento = :establecimiento and lp.estado <> 'Cancelado'";
            tq = em.createQuery(query, LineaPedido.class);
            tq.setParameter("establecimiento", establecimiento);
        }
        else{
            query = "SELECT lp FROM LineaPedido lp WHERE lp.estado = :estado and lp.pedido.establecimiento = :establecimiento";
            tq = em.createQuery(query, LineaPedido.class);
            tq.setParameter("estado", estado);
            tq.setParameter("establecimiento", establecimiento);
        }

        List<LineaPedido> lineas = null;
        try {

            lineas = tq.getResultList();

            for (LineaPedido lp : lineas) {

                ArrayList<Object> camposLinea = new ArrayList<>();
                
                camposLinea.add(lp.getId().getIdLineaPedido());
                camposLinea.add(lp.getPedido().getId());
                camposLinea.add(lp.getCliente().getNombre());
                camposLinea.add(lp.getProducto().getNombre());
                camposLinea.add(lp.getCantidad());
                camposLinea.add(lp.getEstado());
                
                listaLineasPedido.add(camposLinea);

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaLineasPedido;
        
    }
    
    /**
     * Inserta los participantes de un pedido en la BDD
     * @param pedido El pedido sobre el que insertar
     * @param participantes Los participantes a insertar
     * @return Devuelve un boolean que indica si se han insertado correctamente
     */
    @Override
    public boolean insertarParticipantes(Pedido pedido, ArrayList<Usuario> participantes) {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            tx = em.getTransaction();
            tx.begin();
            
            Set<Usuario> setParticipantes = new HashSet();
            
            for(Usuario u : participantes){
                setParticipantes.add(u);
                
            }
            
            pedido.setParticipantes(setParticipantes);
            em.merge(pedido);

            tx.commit();

            exito = true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }
        
        return exito;

    }

    /**
     * Devuelve una línea de pedido dada su clave compuesta (idLinea e idPedido)
     * @param idLinea El id de la línea
     * @param idPedido El id del pedido al que pertenece
     * @return El objeto LineaPedido
     */
    @Override
    public LineaPedido getLineaPedido(int idLinea, int idPedido) {
        
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT lp FROM LineaPedido lp WHERE lp.id.idLineaPedido = :idLinea and lp.pedido.id = :idPedido";

        TypedQuery<LineaPedido> tq = em.createQuery(query, LineaPedido.class);
        tq.setParameter("idLinea", idLinea);
        tq.setParameter("idPedido", idPedido);

        LineaPedido lineaPedido = null;
        try {

            lineaPedido = tq.getSingleResult();
        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return lineaPedido;
        
    }

    /**
     * Actualiza el stock de los productos dados restando la cantidad actual con la recibida
     * @param productosCantidades Mapa con cada producto y la cantidad a restar
     */
    @Override
    public void actualizarStock(HashMap<Producto,Integer> productosCantidades) {
        
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;
        
        try {
            
            tx = em.getTransaction();
            tx.begin();

            //Recorre el mapa de productos y cantidades
            for (Map.Entry<Producto, Integer> entry : productosCantidades.entrySet()) {
                Producto producto = entry.getKey();
                int nuevaCantidad = entry.getValue();

                //Actualiza el producto en la BDD
                Producto productoActualizado = em.find(Producto.class, producto.getId());
                productoActualizado.setStock(productoActualizado.getStock() - nuevaCantidad);

                //Guarda los cambios en la BDD
                em.merge(productoActualizado);
            }

            // Confirmar la transacción
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                addExcepcion(e);
            }

        } finally {
            em.close();
        }
        
    }

    /**
     * Borra una categoría de la BDD
     * @param nombreCategoria El nombre de la categoría
     * @param establCategoria El establecimiento donde borrarla
     * @return Devuelve un boolan que indica si se ha podido borrar correctamente
     */
    @Override
    public boolean borrarCategoria(String nombreCategoria, String establCategoria) {
        
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        String query = "SELECT c FROM Categoria c WHERE c.nombre = :nombrecategoria and c.establecimiento.nombre = :establcategoria";

        TypedQuery<Categoria> tq = em.createQuery(query, Categoria.class);
        tq.setParameter("nombrecategoria", nombreCategoria);
        tq.setParameter("establcategoria", establCategoria);

        Categoria categoria = null;
        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            categoria = tq.getSingleResult();

            //Borra la categoría
            em.remove(categoria);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }
        } finally {
            em.close();
        }

        return exito;
        
    }

    @Override
    public boolean actualizarCategoria(String nombreCategoria, String nombreEstablecimiento, String nuevoNombre, String nuevaDescrip) {
        
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            // Obtiene la categoría por nombre y establecimiento (un establecimiento no puede tener varias categorías con el mismo nombre)
            TypedQuery<Categoria> query = em.createQuery("SELECT c FROM Categoria c WHERE c.nombre = :nombrecategoria and c.establecimiento.nombre = :nombreestabl", Categoria.class);
            query.setParameter("nombrecategoria", nombreCategoria);
            query.setParameter("nombreestabl", nombreEstablecimiento);

            Categoria categoria = (Categoria) query.getSingleResult();

            categoria.setNombre(nuevoNombre);
            categoria.setDescripcion(nuevaDescrip);
            

            // Guarda los cambios en la base de datos
            em.merge(categoria);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
    }

    @Override
    public Categoria getCategoria(String nombreCategoria, String establecimientoCategoria) {
        
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT c FROM Categoria c WHERE c.nombre = :nombrecategoria and c.establecimiento.nombre = :establcategoria";

        TypedQuery<Categoria> tq = em.createQuery(query, Categoria.class);
        tq.setParameter("nombrecategoria", nombreCategoria);
        tq.setParameter("establcategoria", establecimientoCategoria);

        Categoria categoria = null;
        try {

            categoria = tq.getSingleResult();
        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return categoria;
    }

    @Override
    public boolean borrarEstablecimiento(String nombreEstablecimiento) {
        
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        String query = "SELECT e FROM Establecimiento e WHERE e.nombre = :nombreestabl";

        TypedQuery<Establecimiento> tq = em.createQuery(query, Establecimiento.class);
        tq.setParameter("nombreestabl", nombreEstablecimiento);

        Establecimiento establ = null;
        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            establ = tq.getSingleResult();

            //Borra la categoría
            em.remove(establ);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }
        } finally {
            em.close();
        }

        return exito;
        
    }

    @Override
    public boolean actualizarEstabl(String nombreEstabl, String nuevoNombre, String nuevaDireccion, String nuevaLatitud, String nuevaLongitud) {
        
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            // Iniciar la transacción
            em.getTransaction().begin();

            // Obtiene el establecimiento por nombre  (un establecimiento tiene nombre único)
            TypedQuery<Establecimiento> query = em.createQuery("SELECT e FROM Establecimiento e WHERE e.nombre = :nombreestabl", Establecimiento.class);
            query.setParameter("nombreestabl", nombreEstabl);

            Establecimiento establecimiento = (Establecimiento) query.getSingleResult();

            establecimiento.setNombre(nuevoNombre);
            establecimiento.setDireccion(nuevaDireccion);
            establecimiento.setLatitud(Double.parseDouble(nuevaLatitud));
            establecimiento.setLongitud(Double.parseDouble(nuevaLongitud));

            // Guarda los cambios en la base de datos
            em.merge(establecimiento);

            // Confirma la transacción
            em.getTransaction().commit();

            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
        
    }

    @Override
    public ArrayList<Establecimiento> getDatosEstablecimientos() {
        
        ArrayList<Establecimiento> listaEstabl = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT c FROM Establecimiento c";

        TypedQuery<Establecimiento> tq = em.createQuery(query, Establecimiento.class);

        List<Establecimiento> establecimientos = null;
        try {

            establecimientos = tq.getResultList();

            for (Establecimiento e : establecimientos) {

                listaEstabl.add(e);

            }

        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return listaEstabl;
    }

    

}
