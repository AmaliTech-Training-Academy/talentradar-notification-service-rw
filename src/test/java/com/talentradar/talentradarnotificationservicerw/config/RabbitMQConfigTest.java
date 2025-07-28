package com.talentradar.talentradarnotificationservicerw.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RabbitMQConfigTest {
    @Configuration
    static class TestConfig extends RabbitMQConfig {
        @Bean
        public ConnectionFactory connectionFactory() {
            return mock(ConnectionFactory.class);
        }
    }
    @Test
    void allBeansCreated() {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(TestConfig.class);
        assertNotNull(ctx.getBean("assessmentEventsQueue"));
        assertNotNull(ctx.getBean("assessmentEventsExchange"));
        assertNotNull(ctx.getBean("assessmentEventsBinding"));
        assertNotNull(ctx.getBean("feedbackSubmittedQueue"));
        assertNotNull(ctx.getBean("feedbackEventsExchange"));
        assertNotNull(ctx.getBean("userEventsBinding"));
        assertNotNull(ctx.getBean("assessmentEventsBinding"));
        assertNotNull(ctx.getBean("feedbackEventsBinding"));
        assertNotNull(ctx.getBean("feedbackSubmittedBinding"));
        assertNotNull(ctx.getBean("assessmentSubmittedBinding"));
        ctx.close();
    }
}