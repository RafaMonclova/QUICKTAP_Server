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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Where;

/**
 * Clase para mapear la tabla usuario
 * @author rafam
 */
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    //Clave primaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //Columna nombre
    @Column(name = "nombre")
    private String nombre;

    //Columna correo
    @Column(name = "correo", unique = true)
    private String correo;

    //Columna contraseña
    @Column(name = "passw")
    private String passw;

    //Relación many to many con rol
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol"))
    private Set<Rol> rols = new HashSet();
    
    //Relación many to many con establecimiento. Solo se mapea si el rol es propietario o administrador
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "trabajador_establecimiento",
        joinColumns = @JoinColumn(name = "id_trabajador",columnDefinition = "SELECT ue.id_trabajador FROM trabajador_establecimiento ue " +
        "JOIN usuario_rol ur ON ue.id_trabajador = ur.id_usuario " +
        "WHERE ur.id_rol = 2 OR ur.id_rol = 3"),
        inverseJoinColumns = @JoinColumn(name = "id_establecimiento")
        //Consulta con condición basada en el rol del usuario
            
    )
    private Set<Establecimiento> establecimientos = new HashSet();
    
    //Relación one to many con amistades
    @OneToMany(mappedBy = "usuario",orphanRemoval = true,fetch = FetchType.EAGER)
    private Set<Amistad> amigos = new HashSet();
    
    //Constructores
    public Usuario() {
    }

    public Usuario(String nombre, String correo, String passw) {
        this.nombre = nombre;
        this.correo = correo;
        this.passw = passw;
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

    public Set<Rol> getRols() {
        return this.rols;
    }

    public void setRols(Set<Rol> rols) {
        this.rols = rols;
    }

    public Set<Establecimiento> getEstablecimientos() {
        return establecimientos;
    }

    public void setEstablecimientos(Set<Establecimiento> establecimientos) {
        this.establecimientos = establecimientos;
    }

    public Set<Amistad> getAmigos() {
        return amigos;
    }

    public void setAmigos(Set<Amistad> amigos) {
        this.amigos = amigos;
    }
    
    public void añadirAmigo(Amistad amistad){
        amigos.add(amistad);
    }
    
    public void borrarAmigo(Amistad amistad){
        amigos.remove(amistad);
    }
    
    public String toString(){
        return nombre;
    }

}
