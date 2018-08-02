package com.maurofokker.poc.cloud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

@SpringBootApplication
@EnableOAuth2Client
@RestController
public class SpringMicroservicesOauthClientApplication {

    @Autowired
    private OAuth2RestTemplate restTemplate;

    @Bean
    public OAuth2RestTemplate restTemplate(OAuth2ClientContext clientContext) {
        return new OAuth2RestTemplate(resource(), clientContext);
    }

    @Bean
        protected OAuth2ProtectedResourceDetails resource() {
        // contain information such clients ID, clients secret or grant type all the information
        // required to make appropriate oauth calls that will allow to access protected resources
        ResourceOwnerPasswordResourceDetails details = new ResourceOwnerPasswordResourceDetails();
        details.setAccessTokenUri("http://localhost:9090/oauth/token"); // go here to make the initial request for an access token
        details.setClientId("webapp"); // sets the client id configured in the authorization server
        details.setClientSecret("secret"); //
        details.setGrantType("password");
        return details;
    }

    // Principal class provide information about the user and is injected by Spring
    @RequestMapping("/protectedService")
    public String getInfoFromProtectedService(Principal principal) throws URISyntaxException {
        User user = (User) ((Authentication)principal).getPrincipal();
        System.out.println("username: "+user.getUsername());
        System.out.println("password: "+user.getPassword());
        // URI of the protected resource on the resource server that we need to access
        URI uri = new URI("http://localhost:7070/resource/endpoint");
        // create and fire the request to the protected resource
        RequestEntity<String> request = new RequestEntity<String>(HttpMethod.POST, uri);
        AccessTokenRequest accessTokenRequest = this.restTemplate.getOAuth2ClientContext().getAccessTokenRequest();
        accessTokenRequest.set("username", user.getUsername());
        accessTokenRequest.set("password", user.getPassword());
        return this.restTemplate.exchange(request, String.class).getBody();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringMicroservicesOauthClientApplication.class, args);
    }
}
