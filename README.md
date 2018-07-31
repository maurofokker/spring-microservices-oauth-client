# Spring Microservice Oauth client

* part of [microservices demo](https://github.com/maurofokker/microservices-demo)
* [oauth configuration server demo](https://github.com/maurofokker/spring-microservices-oauth-server)

## Configuration

* Re use `WebSecurityConfig.java` (from oauth server demo)
  * keep configuration of the in memory authentication with users `user` and `admin`
  * override the configure HTTP security method and add configuration that requires all requests to be authorized
    ```java
    @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                        .anyRequest()
                            .authenticated()
                    .and()
                    .formLogin()
                    .and()
                    .httpBasic()
            ;
        }
    ```
* add `@EnableOAuth2Client` annotation
* inject bean `OAuth2RestTemplate` with `OAuth2ProtectedResourceDetails` and `DefaultOAuth2ClientContext`
  ```java
    @Autowired
    private OAuth2RestTemplate restTemplate;

    @Bean
    public OAuth2RestTemplate restTemplate() {
        return new OAuth2RestTemplate(resource(), new DefaultOAuth2ClientContext());
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
  ```
* hit the protected resource (in resource server) with the generated client
  ```java
    @RequestMapping("/protectedService")
    public String getInfoFromProtectedService(Principal principal) throws URISyntaxException {
        User user = (User) ((Authentication)principal).getPrincipal();
        // URI of the protected resource on the resource server that we need to access
        URI uri = new URI("http://localhost:9090/resource/endpoin");
        // create and fire the request to the protected resource
        RequestEntity<String> request = new RequestEntity<String>(HttpMethod.POST, uri);
        AccessTokenRequest accessTokenRequest = this.restTemplate.getOAuth2ClientContext().getAccessTokenRequest();
        accessTokenRequest.set("username", user.getUsername());
        accessTokenRequest.set("password", user.getPassword());
        return this.restTemplate.exchange(request, String.class).getBody();
    }
  ```
