/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidades;

/**
 *
 * @author rafam
 */
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.*;
import javax.persistence.*;

/**
 * Clase para mapear la tabla amistades
 * @author rafam
 */
@Entity
@Table(name = "amistades")
public class Amistad {

    //Clave primaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    //Relación many to one con usuario (usuario)
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    //Relación many to one con usuario (amigo)
    @ManyToOne
    @JoinColumn(name = "id_amigo")
    private Usuario amigo;

    //Columna estado
    @Column(name = "estado")
    private String estado;
    
    //Constructores
    public Amistad(){
        
    }

    public Amistad(Usuario usuario, Usuario amigo, String estado) {
        this.usuario = usuario;
        this.amigo = amigo;
        this.estado = estado;
    }

    //Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getAmigo() {
        return amigo;
    }

    public void setAmigo(Usuario amigo) {
        this.amigo = amigo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    
}
