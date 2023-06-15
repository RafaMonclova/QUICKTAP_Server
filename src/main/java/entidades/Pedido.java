package entidades;
// Generated 10-abr-2023 18:23:04 by Hibernate Tools 4.3.1


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Clase para mapear la tabla pedido
 * @author rafam
 */
@Entity
@Table(name = "pedido")
public class Pedido  implements Serializable {

    //Clave primaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    //Columna fecha
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    
    //Relación many to one con usuario (cliente)
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Usuario clientePide;
    
    //Relación many to one con usuario (trabajador)
    @ManyToOne
    @JoinColumn(name = "id_trabajador")
    private Usuario trabajadorPrepara;
    
    //Relación many to many con usuario (clientes participantes)
    @ManyToMany
    @JoinTable(name = "participa_pedido",
        joinColumns = @JoinColumn(name = "id_pedido"),
        inverseJoinColumns = @JoinColumn(name = "id_cliente"))
    private Set<Usuario> participantes = new HashSet();
    
    //Relación many to one con establecimiento
    @ManyToOne
    @JoinColumn(name = "id_establecimiento")
    private Establecimiento establecimiento;
    
    //Relación one to many con linea_pedido
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LineaPedido> lineasPedido = new HashSet();
     
    
    //Constructores
    public Pedido() {
    }

    public Pedido(Date fecha, Usuario clientePide, Establecimiento establecimiento) {
        this.fecha = fecha;
        this.clientePide = clientePide;
        this.establecimiento = establecimiento;

    }
   
    //Getters y setters
    public int getId() {
        return this.id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public Date getFecha() {
        return this.fecha;
    }
    
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Usuario getClientePide() {
        return clientePide;
    }

    public void setClientePide(Usuario clientePide) {
        this.clientePide = clientePide;
    }

    public Usuario getTrabajadorPrepara() {
        return trabajadorPrepara;
    }

    public void setTrabajadorPrepara(Usuario trabajadorPrepara) {
        this.trabajadorPrepara = trabajadorPrepara;
    }

    public Set<Usuario> getParticipantes() {
        return participantes;
    }

    public void setParticipantes(Set<Usuario> participantes) {
        this.participantes = participantes;
    }
    
    
    public String toString(){
        return "Pedido "+id;
    }

    public Establecimiento getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(Establecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }

    public Set<LineaPedido> getLineasPedido() {
        return lineasPedido;
    }

    public void setLineasPedido(Set<LineaPedido> lineasPedido) {
        this.lineasPedido = lineasPedido;
    }
    
    
    
}


