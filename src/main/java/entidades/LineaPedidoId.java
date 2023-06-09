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
    
    @Column(name = "id_lineaPedido")
    private int idLineaPedido;
    
    @Column(name = "id_pedido")
    private int idPedido;

    public int getIdLineaPedido() {
        return idLineaPedido;
    }

    public void setIdLineaPedido(int idLineaPedido) {
        this.idLineaPedido = idLineaPedido;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public LineaPedidoId(int idLineaPedido, int idPedido) {
        this.idLineaPedido = idLineaPedido;
        this.idPedido = idPedido;
    }
    
    public LineaPedidoId(){
        
    }

    // Métodos equals() y hashCode() para comparación y uso en colecciones
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LineaPedidoId that = (LineaPedidoId) o;

        if (idLineaPedido != that.idLineaPedido) return false;
        return idPedido == that.idPedido;
    }

    @Override
    public int hashCode() {
        int result = idLineaPedido;
        result = 31 * result + idPedido;
        return result;
    }
}
