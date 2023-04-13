package entidades;
// Generated 10-abr-2023 18:23:04 by Hibernate Tools 4.3.1


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "rol")
public class Rol  implements Serializable {

    @Id
    private int id;
    
    @Column(name = "nombre")
    private String nombre;
     //private Set usuarios = new HashSet(0);

    
    public Rol() {
    }

	
    public Rol(String nombre) {
        this.nombre = nombre;
    }
    
   
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

    public String toString(){
        return nombre;
    }



}


