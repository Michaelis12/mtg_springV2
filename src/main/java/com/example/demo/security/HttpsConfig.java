package com.example.demo.security;


import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.catalina.connector.Connector;


@Configuration
public class HttpsConfig {
	
	// Rediriger les requete du port 8080 vers un port sécurisé

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> containerCustomizer() {
        return factory -> factory.addAdditionalTomcatConnectors(httpConnector());
    }

    private org.apache.catalina.connector.Connector httpConnector() {
        var connector = new org.apache.catalina.connector.Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);  // HTTP port
        connector.setSecure(false);
        connector.setRedirectPort(8443);  // HTTPS port
        return connector;
    }
    
}
