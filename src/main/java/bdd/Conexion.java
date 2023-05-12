/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bdd;

import entidades.Categoria;
import entidades.Establecimiento;
import entidades.LineaPedido;
import entidades.Pedido;
import entidades.Producto;
import entidades.Rol;
import entidades.Usuario;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
import javax.transaction.Transactional;

/**
 *
 * @author rafam
 */
public class Conexion implements DAO {

    // Crea un objeto EntityManagerFactory para realizar las conexiones a la base de datos
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence
            .createEntityManagerFactory("ConexionBD");

    private List<Exception> excepciones = new ArrayList<>();

    public String getHora() {

        return ZonedDateTime.now()
                .format(DateTimeFormatter.ofPattern("hh:mm:ss"));

    }

    public List<Exception> getExcepciones() {
        return excepciones;
    }

    public void addExcepcion(Exception e) {
        excepciones.add(e);

        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new FileWriter("error_log.log", true));

            pw.println("[" + getHora() + "]" + e.getMessage());

        } catch (IOException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw.close();
        }
    }

    @Override
    public long getTotalClientes() {

        long numUsuarios = 0;

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String jpql = "SELECT COUNT(u) FROM Usuario u";

        Query query = (Query) em.createQuery(jpql);

        numUsuarios = (long) query.getSingleResult();

        return numUsuarios;

    }

    //Devuelve la fecha del último registro añadido en la tabla establecimiento
    @Override
    public String getUltimoCliente() {

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT e FROM Establecimiento e ORDER BY e.id DESC";

        TypedQuery<Establecimiento> tq = em.createQuery(query, Establecimiento.class);
        tq.setMaxResults(1); //Devuelve el primer registro, que es el último id

        Establecimiento establ = null;
        try {

            establ = tq.getSingleResult();
            //System.out.println(usuario.getNombre());
        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return establ.getNombre();

    }

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
            //System.out.println(usuario.getNombre());
        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return usuario;
    }

    @Override
    public Usuario getUsuario(String nombre) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT c FROM Usuario c WHERE c.nombre = :nombrebuscar";

        TypedQuery<Usuario> tq = em.createQuery(query, Usuario.class);
        tq.setParameter("nombrebuscar", nombre);

        Usuario usuario = null;
        try {

            usuario = tq.getSingleResult();
            //System.out.println(usuario.getNombre());
        } catch (NoResultException ex) {
            addExcepcion(ex);
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
                e.printStackTrace();
                addExcepcion(e);
            }
        } finally {
            em.close();
        }

        return exito;
    }

    @Override
    public boolean insertarEstabl(String nombre, String direccion, String coords) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            Establecimiento establecimiento = new Establecimiento(nombre, direccion, coords);

            tx = em.getTransaction();
            tx.begin();
            em.persist(establecimiento);
            tx.commit();
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                e.printStackTrace();
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
    }

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
                e.printStackTrace();
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
    }

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
                ex.printStackTrace();
                addExcepcion(ex);
            } finally {
                em.close();
            }
        }

        return setRoles;

    }

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
    
    @Override
    public Set<Categoria> convertCategorias(ArrayList<String> categorias) {

        Set<Categoria> setCateg = new HashSet();

        for (String c : categorias) {

            EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

            String query = "SELECT c FROM Categoria c where c.nombre = :nombrecateg";

            TypedQuery<Categoria> tq = em.createQuery(query, Categoria.class);
            tq.setParameter("nombrecateg", c);

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

            System.out.println("Usuario actualizado.");
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                e.printStackTrace();
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
    }

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

            System.out.println("Usuario actualizado.");
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                e.printStackTrace();
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
    }
    
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

            System.out.println("Usuario actualizado.");
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                e.printStackTrace();
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
        
    }
    
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

            System.out.println("Usuario actualizado.");
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                e.printStackTrace();
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
    }

//    @Override
//    public double getCajaHoy(String establecimiento) {
//
//        double cajaHoy = 0;
//
//        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
//
//        String jpql = "SELECT SUM(lp.cantidad * p.precio) AS precio_total\n"
//                + "FROM LineaPedido lp\n"
//                + "JOIN lp.producto p\n"
//                + "JOIN lp.pedido pe\n"
//                + "JOIN pe.establecimiento e\n"
//                + "WHERE pe.fecha = CURRENT_DATE and e.nombre = :nombreestabl";
//
//        Query query = (Query) em.createQuery(jpql);
//        query.setParameter("nombreestabl", establecimiento);
//        try {
//            cajaHoy = (double) query.getSingleResult();
//        }catch (NoResultException | NullPointerException ex) {
//            addExcepcion(ex);
//        } finally {
//            em.close();
//        }
//        
//        
//
//        return cajaHoy;
//
//    }
    
    @Override
    public double getCaja(String establecimiento,String fecha) {

        double caja = 0;
        
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaDate = null;
        try {
            fechaDate = formato.parse(fecha);
            System.out.println(fecha);
        } catch (ParseException e) {
            e.printStackTrace();
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
        }catch (NoResultException | NullPointerException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }
        
        

        return caja;

    }

    @Override
    public ArrayList<Usuario> getUsuarios(Set<Establecimiento> establecimientos) {

        ArrayList<Usuario> listaUsuarios = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT DISTINCT u " +
                        "FROM Usuario u " +
                        "JOIN u.establecimientos e " +
                        "WHERE e IN :establecimientos";
        
        

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

    @Override
    public long getNumPedidos(String estado) {

        long totalPedidos = 0;

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String jpql = "";

        switch (estado) {

            case "TODOS":
                jpql = "SELECT COUNT(p) FROM Pedido p";
                break;
            case "FINALIZADOS":
                jpql = "SELECT COUNT(p) FROM Pedido p WHERE NOT EXISTS (\n"
                        + "  SELECT lp FROM LineaPedido lp WHERE lp.pedido = p AND lp.estado <> 'finalizado'\n"
                        + ")";
                break;
            case "PENDIENTES":
                jpql = "SELECT COUNT(p) FROM Pedido p WHERE NOT EXISTS (\n"
                        + "  SELECT lp FROM LineaPedido lp WHERE lp.pedido = p AND lp.estado <> 'pendiente'\n"
                        + ")";
                
                break;

        }

        Query query = (Query) em.createQuery(jpql);

        totalPedidos = (long) query.getSingleResult();

        return totalPedidos;

    }

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

    @Override
    public boolean insertarCategoria(String nombre, String descripcion, Establecimiento establecimiento) {
        
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            Categoria categoria = new Categoria(nombre, descripcion);
            categoria.setEstablecimiento(establecimiento);
            //establecimiento.getCategorias().add(categoria);
            

            tx = em.getTransaction();
            tx.begin();
            em.persist(categoria);
            //em.merge(establecimiento);
            tx.commit();
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                e.printStackTrace();
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
        
    }

    @Override
    public Establecimiento getEstablecimiento(String nombre) {
        
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT c FROM Establecimiento c WHERE c.nombre = :nombrebuscar";

        TypedQuery<Establecimiento> tq = em.createQuery(query, Establecimiento.class);
        tq.setParameter("nombrebuscar", nombre);

        Establecimiento establecimiento = null;
        try {

            establecimiento = tq.getSingleResult();
            //System.out.println(usuario.getNombre());
        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return establecimiento;
        
    }

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

    @Override
    public boolean insertarProducto(String nombre, String descripcion, double precio, int stockInicial, byte[] imagen, ArrayList<String> categorias, Establecimiento establecimiento) {
    
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        boolean exito = false;

        try {

            Producto producto = new Producto(nombre, descripcion,precio,imagen,stockInicial);
            producto.setEstablecimiento(establecimiento);
            producto.setCategorias(convertCategorias(categorias));

            //Comprueba si existe un producto con ese nombre en el establecimiento seleccionado
            //Si existe, no lo inserta
//            if(getProducto(nombre,establecimiento.getNombre()) != null){
//                
//                exito = false;
//                
//            }
//            else{
                tx = em.getTransaction();
                tx.begin();
                em.persist(producto);
                //em.merge(establecimiento);
                tx.commit();
                exito = true;
            //}
            
            
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                e.printStackTrace();
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
    
    }

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
    
    @Override
    public ArrayList<Producto> getProductos(String establecimiento,String categoria) {
        
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
            //System.out.println(usuario.getNombre());
        } catch (NoResultException ex) {
            addExcepcion(ex);
        } finally {
            em.close();
        }

        return producto;
        
    }

    @Override
    public boolean actualizarProducto(String nombreProducto,String nombreEstablecimiento, String nuevoNombre,String nuevaDescrip,
            double nuevoPrecio, int nuevoStock,byte[] nuevaImagen, ArrayList<String> nuevasCategorias) {
        
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

            System.out.println("Producto actualizado.");
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                e.printStackTrace();
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
        
    }

    @Transactional
    @Override
    public ArrayList<LineaPedido> getLineaPedidos(String establecimiento) {
        
        ArrayList<LineaPedido> listaLineasPedidos = new ArrayList<>();

        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        //String query = "SELECT lp FROM LineaPedido lp WHERE p.establecimiento.nombre = :nombreEstabl";
        /*
        String jpql = "SELECT lp FROM LineaPedido lp\n"
                + "JOIN lp.producto p\n"
                + "JOIN lp.pedido pe\n"
                + "JOIN pe.establecimiento e\n"
                + "WHERE lp.estado = :estado and e.nombre = :nombreestabl";
        */
        
        String jpql = "SELECT lp FROM LineaPedido lp " +
              "JOIN lp.pedido p " +
              "JOIN p.establecimiento e " +
              "WHERE e.nombre = :nombreestabl and lp.estado = :estado";
        
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
    
    @Override
    public ArrayList<Usuario> getTrabajadores(String establecimiento) {
        
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
    
    @Override
    public boolean actualizarTrabajador(String nombreTrabajador, String nuevoNombre,String nuevoCorreo,
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

            System.out.println("Usuario actualizado.");
            exito = true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                e.printStackTrace();
                addExcepcion(e);
            }

        } finally {
            em.close();
        }

        return exito;
        
    }

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
            
            System.out.println("Producto borrado.");
            exito = true;
            
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                e.printStackTrace();
                addExcepcion(e);
            }
        } finally {
            em.close();
        }
        
        return exito;
        
    }

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
            
            System.out.println("Usuario borrado.");
            exito = true;
            
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                exito = false;
                e.printStackTrace();
                addExcepcion(e);
            }
        } finally {
            em.close();
        }
        
        return exito;
        
    }

    @Override
    public Pedido crearPedido(String fecha,Usuario cliente, Establecimiento establ) {
        
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction tx = null;

        Pedido pedido = null;
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaDate = null;

        try {

            fechaDate = formato.parse(fecha);
            System.out.println(fecha);
        
            pedido = new Pedido(fechaDate, cliente, establ);

            tx = em.getTransaction();
            tx.begin();
            em.persist(pedido);
            tx.commit();
            
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
                e.printStackTrace();
                addExcepcion(e);
            }
        } finally {
            em.close();
        }

        return pedido;
        
    }

    

}
