package entidades;
// Generated 10-abr-2023 18:23:04 by Hibernate Tools 4.3.1

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "correo", unique = true)
    private String correo;

    @Column(name = "passw")
    private String passw;

    @ManyToMany
    @JoinTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol"))
    private Set<Rol> rols = new HashSet();
    
    @ManyToMany
    @JoinTable(
        name = "trabajador_establecimiento",
        joinColumns = @JoinColumn(name = "id_trabajador",columnDefinition = "SELECT ue.id_trabajador FROM trabajador_establecimiento ue " +
        "JOIN usuario_rol ur ON ue.id_trabajador = ur.id_usuario " +
        "WHERE ur.id_rol = 2 OR ur.id_rol = 3"),
        inverseJoinColumns = @JoinColumn(name = "id_establecimiento")
        // Consulta personalizada con condición basada en el rol del usuario
            
    )
    //@Where(clause = "rol_id = 2 OR rol_id = 3") // Condición para filtrar por rol "trabajador"
    private Set<Establecimiento> establecimientos = new HashSet();

    public Usuario() {
    }

    public Usuario(String nombre, String correo, String passw) {
        this.nombre = nombre;
        this.correo = correo;
        this.passw = passw;
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

    public String getCorreo() {
        return this.correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassw() {
        return this.passw;
    }

    public void setPassw(String passw) {
        this.passw = passw;
    }

    public Set getRols() {
        return this.rols;
    }

    public void setRols(Set rols) {
        this.rols = rols;
    }

    public Set<Establecimiento> getEstablecimientos() {
        return establecimientos;
    }

    public void setEstablecimientos(Set<Establecimiento> establecimientos) {
        this.establecimientos = establecimientos;
    }
    
    

}
