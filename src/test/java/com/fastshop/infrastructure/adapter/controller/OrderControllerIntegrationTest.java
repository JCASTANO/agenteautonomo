package com.fastshop.infrastructure.adapter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fastshop.application.dto.CreateOrderRequest;
import com.fastshop.application.dto.OrderItemRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
                123,
                List.of(
                        new OrderItemRequest(456, 2),
                        new OrderItemRequest(789, 1)
                ),
                "CREDIT_CARD"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> entity = new HttpEntity<>(request, headers);

        // When
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/pedidos",
                entity,
                String.class
        );

        // Then
        assertEquals(201, response.getStatusCode().value());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("\"orderId\":1"));
        assertTrue(body.contains("\"customerId\":123"));
        assertTrue(body.contains("\"status\":\"PAGADO\""));
    }

    @Test
    void shouldGetOrderSuccessfully() throws Exception {
        // Given - Create an order first
        CreateOrderRequest request = new CreateOrderRequest(
                123,
                List.of(new OrderItemRequest(456, 1)),
                "CREDIT_CARD"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateOrderRequest> entity = new HttpEntity<>(request, headers);

        restTemplate.postForEntity("http://localhost:" + port + "/pedidos", entity, String.class);

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/orders/1",
                String.class
        );

        // Then
        assertEquals(200, response.getStatusCode().value());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("\"orderId\":1"));
        assertTrue(body.contains("\"customerId\":123"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentOrder() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/orders/999",
                String.class
        );

        assertEquals(404, response.getStatusCode().value());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("Orden no encontrado"));
    }
}