package com.tienda.config;

import com.tienda.model.Cliente;
import com.tienda.model.Cotizacion;
import com.tienda.model.EstadoCotizacion;
import com.tienda.repository.ClienteRepository;
import com.tienda.repository.CotizacionRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

@Configuration
@Profile("dev")
public class TestDataRunner {

    @Bean
    CommandLineRunner testDb(ClienteRepository clienteRepo, CotizacionRepository cotRepo) {
        return args -> {
            Cliente c = Cliente.builder()
                    .nombres("Juan")
                    .apellidos("Pérez")
                    .email("juan.perez+test@email.com")
                    .telefono("3001234567")
                    .documento("CC123456789")
                    .build();

            c = clienteRepo.save(c);

            Cotizacion q = Cotizacion.builder()
                    .cliente(c)
                    .total(new BigDecimal("1500000.00"))
                    .estado(EstadoCotizacion.CREADA)
                    .build();

            q = cotRepo.save(q);

            System.out.println("✅ Insert OK -> Cliente ID: " + c.getId() + " | Cotizacion ID: " + q.getId());
        };
    }
}