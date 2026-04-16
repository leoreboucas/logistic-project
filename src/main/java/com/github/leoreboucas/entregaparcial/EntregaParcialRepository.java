package com.github.leoreboucas.entregaparcial;

import com.github.leoreboucas.entregador.Entregador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EntregaParcialRepository extends JpaRepository<EntregaParcial, Long> {
    @Query("SELECT ep FROM EntregaParcial ep WHERE ep.order.trackingCode = :trackingCode AND ep.deliveryMan = :deliveryMan ORDER BY ep.createdAt DESC LIMIT 1")
    EntregaParcial findLatestByOrderTrackingCodeAndDeliveryMan(
            @Param("trackingCode") String trackingCode,
            @Param("deliveryMan") Entregador deliveryMan
    );

    List<EntregaParcial> findByDeliveryMan(Entregador deliveryMan);
}
