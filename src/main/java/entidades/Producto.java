package entidades;
// Generated 10-abr-2023 18:23:04 by Hibernate Tools 4.3.1


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Clase para mapear la tabla producto
 * @author rafam
 */
@Entity
@Table(name = "producto")
public class Producto  implements Serializable {

    //Clave primaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    //Columna nombre
    @Column(name = "nombre")
    private String nombre;
    
    //Columna descripción
    @Column(name = "descripcion")
    private String descripcion;
    
    //Columna precio
    @Column(name = "precio")
    private double precio;
    
    //Columna imagen
    @Column(name = "imagen")
    private byte[] imagen;
    
    //Columna stock
    @Column(name = "stock")
    private int stock;
    
    //Relación many to many con categoría
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "producto_categoria",
            joinColumns = @JoinColumn(name = "id_producto"),
            inverseJoinColumns = @JoinColumn(name = "id_categoria"))
    private Set<Categoria> categorias = new HashSet();
    
    //Relación many to one con establecimiento
    @ManyToOne
    @JoinColumn(name = "id_establecimiento")
    private Establecimiento establecimiento;

    //Constructores
    public Producto() {
    }

	
    public Producto(String nombre, String descripcion, double precio, byte[] imagen, int stock) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagen = imagen;
        this.stock = stock;
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
    public String getDescripcion() {
        return this.descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public double getPrecio() {
        return this.precio;
    }
    
    public void setPrecio(double precio) {
        this.precio = precio;
    }
    public byte[] getImagen() {
        return this.imagen;
    }
    
    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }
    public int getStock() {
        return this.stock;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
    }

    public Set<Categoria> getCategorias() {
        return this.categorias;
    }
    
    public void setCategorias(Set categorias) {
        this.categorias = categorias;
    }

    public Establecimiento getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(Establecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }

    public String toString(){
        return nombre;
    }

}


