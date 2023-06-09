/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidades;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "linea_pedido")
public class LineaPedido implements Serializable{
    
    @EmbeddedId
    private LineaPedidoId id;

    /*
    @Id
    @Column(name = "id_lineaPedido")
    private int id;*/
    
    //@ManyToOne
    //@JoinColumn(name = "id_pedido")
    //private Pedido pedido;
    
    @ManyToOne
    @MapsId("idPedido") // Mapeo de la parte de la clave compuesta correspondiente a id_pedido
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Usuario cliente;
    
    @Column(name = "cantidad")
    private int cantidad;
    
    @Column(name = "estado")
    private String estado;
    
    @Column(name = "codRecogida")
    private int codigoRecogida;

    public LineaPedido(){
        
    }
    
    public LineaPedido(Pedido pedido, Producto producto, Usuario cliente, int cantidad, String estado) {
        this.pedido = pedido;
        this.producto = producto;
        this.cliente = cliente;
        this.cantidad = cantidad;
        this.estado = estado;
    }

    public LineaPedidoId getId() {
        return id;
    }

    public void setId(LineaPedidoId id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getCodigoRecogida() {
        return codigoRecogida;
    }

    public void setCodigoRecogida(int codigoRecogida) {
        this.codigoRecogida = codigoRecogida;
    }
    
    
    
}
