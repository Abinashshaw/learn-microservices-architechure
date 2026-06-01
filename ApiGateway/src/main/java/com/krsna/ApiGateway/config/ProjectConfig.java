package com.krsna.ApiGateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.time.Duration;

@Configuration
public class ProjectConfig {

    private final String userServiceId;

    private final String ratingServiceId;

    private final String hotelServiceId;

    public ProjectConfig(
            @Value("${user.service.id}") String userServiceId,
            @Value("${hotel.service.id}") String hotelServiceId,
            @Value("${rating.service.id}") String ratingServiceId
            ){
        this.userServiceId = userServiceId;
        this.hotelServiceId = hotelServiceId;
        this.ratingServiceId = ratingServiceId;
    }
    @Bean
    public RouteLocator myCustomRoutes(RouteLocatorBuilder builder){

        RouteLocator myRoutes = builder.routes().
                route( "user-route", r ->
                        r.path("/users/**")
                                 .filters(f -> f
//                                         .retry(retryConfig ->
//                                                 retryConfig.setRetries(3).setMethods(HttpMethod.GET, HttpMethod.POST)
//                                                .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, false).setJitter(0.5))
                                         .circuitBreaker(c -> c.setName("UserCircuitBreaker")
                                                 .setFallbackUri("forward:/fallback-call"))
                                         .rewritePath("/users/?(?<remaining>.*)", "/${remaining}"))
                                 .uri("lb://"+userServiceId))

                    .route("hotel-route", r ->
                        r.path("/hotels/**")
                                .filters(f -> f.circuitBreaker(c -> c.setName("HotelCircuitBreaker").setFallbackUri("forward:/fallback-call")).rewritePath("/hotels/?(?<remaining>.*)", "/${remaining}"))
                                .uri("lb://"+hotelServiceId))

                    .route("rating-route", r ->
                        r.path("/ratings/**")
                                .filters(f -> f.circuitBreaker(c -> c.setName("RatingCircuitBreaker").setFallbackUri("forward:/fallback-call")).rewritePath("/ratings/?(?<remaining>.*)", "/${remaining}"))
                                .uri("lb://"+ratingServiceId))
                .build();

        return myRoutes;
    }
}
