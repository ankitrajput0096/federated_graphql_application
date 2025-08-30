package com.example.carservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;

@GraphQlTest
class CarControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void pingQuery() {
        graphQlTester.document("{ ping }")
                .execute()
                .path("ping")
                .entity(String.class)
                .satisfies(response -> response.contains("pong"));
    }

    @Test
    void carQuery() {
        graphQlTester.document("{ car(vin: \"TEST123\") { vin model } }")
                .execute()
                .path("car.vin")
                .entity(String.class)
                .isEqualTo("TEST123");
    }
}