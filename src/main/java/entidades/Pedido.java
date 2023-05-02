package entidades;
// Generated 10-abr-2023 18:23:04 by Hibernate Tools 4.3.1


import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
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

@Entity
@Table(name = "pedido")
public class Pedido  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Usuario clientePide;
    
    @ManyToOne
    @JoinColumn(name = "id_trabajador")
    private Usuario trabajadorPrepara;
    
    @ManyToMany
    @JoinTable(name = "participa_pedido",
        joinColumns = @JoinColumn(name = "id_pedido"),
        inverseJoinColumns = @JoinColumn(name = "id_cliente"))
    private Set<Usuario> participantes = new HashSet();
    
    @ManyToOne
    @JoinColumn(name = "id_establecimiento")
    private Establecimiento establecimiento;
     

    public Pedido() {
    }

    public Pedido(Date fecha, Usuario clientePide, Usuario trabajadorPrepara) {
        this.fecha = fecha;
        this.clientePide = clientePide;
        this.trabajadorPrepara = trabajadorPrepara;
    }
   
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


}


