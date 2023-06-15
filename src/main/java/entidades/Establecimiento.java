package entidades;
// Generated 10-abr-2023 18:23:04 by Hibernate Tools 4.3.1


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Clase para mapear la tabla establecimiento
 * @author rafam
 */
@Entity
@Table(name = "establecimiento")
public class Establecimiento  implements Serializable {

    //Clave primaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    //Columna nombre
    @Column(name = "nombre")
    private String nombre;
    
    //Columna dirección
    @Column(name = "direccion")
    private String direccion;
    
    //Columna latitud
    @Column(name = "latitud")
    private double latitud;
    
    //Columna longitud
    @Column(name = "longitud")
    private double longitud;
    
    //Relación one to many con producto
    @OneToMany(mappedBy = "establecimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Producto> productos = new HashSet<>();
    
    //Relación one to many con categoría
    @OneToMany(mappedBy = "establecimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Categoria> categorias = new HashSet<>();
    
    //Relación one to many con pedido
    @OneToMany(mappedBy = "establecimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Pedido> pedidos = new HashSet();

    //Relación many to many con usuario
    @ManyToMany(mappedBy = "establecimientos")
    private Set<Usuario> usuarios;

    //Constructores
    public Establecimiento() {
    }

	
    public Establecimiento(String nombre, String direccion, double latitud, double longitud) {
        this.nombre = nombre;
        this.direccion = direccion;
        //this.localidad = localidad;
        this.latitud = latitud;
        this.longitud = longitud;
    }
   
    //Getters y setters
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    public String getNombre() {
        return this.nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getDireccion() {
        return this.direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public double getLatitud() {
        return this.latitud;
    }
    
    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }
    public double getLongitud() {
        return this.longitud;
    }
    
    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public Set<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(Set<Categoria> categorias) {
        this.categorias = categorias;
    }
    
    public Set getProductos() {
        return this.productos;
    }
    
    public void setProductos(Set productos) {
        this.productos = productos;
    }
    public Set getPedidos() {
        return this.pedidos;
    }
    
    public void setPedidos(Set pedidos) {
        this.pedidos = pedidos;
    }
    public Set getUsuarios() {
        return this.usuarios;
    }
    
    public void setUsuarios(Set usuarios) {
        this.usuarios = usuarios;
    }




}


