package com.github.leoreboucas.historicopedido;

import com.github.leoreboucas.pedido.Pedido;
import com.github.leoreboucas.pedido.PedidoStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_history")
public class HistoricoPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "id_order")
    private Pedido order;
    @Column(name = "previous_status")
    @Enumerated(EnumType.STRING)
    private PedidoStatus previousStatus;
    @Column(name = "new_status")
    @Enumerated(EnumType.STRING)
    private PedidoStatus newStatus;
    private String observation;
    @Column(name = "date_of_change")
    private LocalDateTime dateOfChange;
}
