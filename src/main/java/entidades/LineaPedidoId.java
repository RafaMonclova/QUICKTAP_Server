/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entidades;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author rafam
 */
@Embeddable
public class LineaPedidoId implements Serializable {

    //private static final long serialVersionUID = 1L;

    @Column(name = "id_pedido")
    private int idPedido;

    @Column(name = "id_producto")
    private int idProducto;

    public LineaPedidoId(int idPedido, int idProducto) {
        this.idPedido = idPedido;
        this.idProducto = idProducto;
    }
    
    public LineaPedidoId(){
        
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LineaPedidoId)) return false;
        LineaPedidoId that = (LineaPedidoId) o;
        return Objects.equals(idPedido, that.idPedido) &&
                Objects.equals(idProducto, that.idProducto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPedido, idProducto);
    }
}
