package com.redhat;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import org.apache.camel.component.servlet.CamelHttpTransportServlet;


@Component

public final class CamelRoutes extends RouteBuilder {
    /**
     * Defines Apache Camel routes using REST DSL fluent API.
     */
   @Bean
   public ServletRegistrationBean servletRegistrationBean() {
       ServletRegistrationBean registration = new ServletRegistrationBean(new CamelHttpTransportServlet(), "/service/*");
       registration.setName("CamelServlet");
       return registration;
   }

    public void configure() {
        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.auto)
                .producerComponent("http4")
                .apiContextPath("/swagger")
                .apiContextRouteId("swagger")
                .contextPath("/service")
                .host("localhost:8064")
                .apiProperty("api.title", "Example REST api")
                .apiProperty("api.version", "1.0");
   rest().get("/eligibility/{custId}")

   .route().bean(TransformerBean.class,"validateTxn");

   }

}



