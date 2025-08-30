package com.example.carservice.controller;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
public class CarController {

    @QueryMapping
    public Mono<String> ping() {
        return Mono.just("pong - car service is running!");
    }

    @QueryMapping
    public Mono<CarDto> car(@Argument String vin) {
        // Simulate async lookup with reactive type
        CarDto car = new CarDto(vin, "Tesla Model S", "Red", 2024, true);
        return Mono.just(car);
    }

    @QueryMapping
    public Flux<CarDto> cars(@Argument Integer limit) {
        // Simulate returning multiple cars
        List<CarDto> mockCars = List.of(
                new CarDto("VIN001", "Tesla Model S", "Red", 2024, true),
                new CarDto("VIN002", "BMW i8", "Blue", 2023, true),
                new CarDto("VIN003", "Ford Mustang", "Black", 2022, false)
        );

        int actualLimit = (limit != null) ? limit : 10;
        return Flux.fromIterable(mockCars)
                .take(actualLimit);
    }

    /**
     * DTO representing a Car for GraphQL responses
     */
    public static record CarDto(
            String vin,
            String model,
            String color,
            Integer year,
            Boolean isElectric
    ) {}
}