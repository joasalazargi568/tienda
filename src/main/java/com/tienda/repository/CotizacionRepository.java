package com.tienda.repository;

import com.tienda.model.Cotizacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CotizacionRepository extends JpaRepository<Cotizacion, Long> {

    @Query(
            value = "select c from Cotizacion c join fetch c.cliente where c.cliente.id = :clienteId",
            countQuery = "select count(c) from Cotizacion c where c.cliente.id = :clienteId"
    )
    Page<Cotizacion> findByClienteIdWithCliente(@Param("clienteId") Long clienteId, Pageable pageable);
}