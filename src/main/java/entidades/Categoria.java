package entidades;
// Generated 10-abr-2023 18:23:04 by Hibernate Tools 4.3.1


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Clase para mapear la tabla categoría
 * @author rafam
 */
@Entity
@Table(name = "categoria")
public class Categoria  implements Serializable {

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
    
    //Relación many to one con establecimiento
    @ManyToOne
    @JoinColumn(name = "id_establecimiento")
    private Establecimiento establecimiento;
    
    //Relación many to many con producto
    @ManyToMany(mappedBy = "categorias")
    private Set<Producto> productos;
     

    //Constructores
    public Categoria() {
    }

	
    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
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

    public Establecimiento getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(Establecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }

    public Set<Producto> getProductos() {
        return productos;
    }

    public void setProductos(Set<Producto> productos) {
        this.productos = productos;
    }

    


}


