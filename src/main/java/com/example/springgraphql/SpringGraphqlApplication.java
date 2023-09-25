package com.example.springgraphql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class SpringGraphqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringGraphqlApplication.class, args);
    }

}

record Customer(Integer id, String name){}
record Profile(Integer id) {}

@Controller
class CustomerGraphqlController {

    @QueryMapping
    public List<Customer> customers() {
        return List.of( new Customer(1, "A"), new Customer(2, "B"));
    }

    @QueryMapping
    public Flux<Customer> customersReactive() {
        return Flux.just(new Customer(1, "A"), new Customer(2, "B"));
    }

    @QueryMapping
    public Customer customerById(@Argument Integer id) {
        return customers().get(id - 1);
    }

    /**
     * This is a preferred approach to assembling the data avoiding N + 1 problem
     * All the profiles are fetched in a single call and so is all the customers
     * @param customers
     * @return
     */

    @BatchMapping
    public Map<Customer, Profile> profile (List<Customer> customers){
        System.out.println("Fetching profile for customers " + customers.size());
        return customers
                .stream()
                .collect(Collectors.toMap( customer -> customer,
                                            customer -> new Profile(customer.id())));
    }

    /**
     * This implementation causes the N+1 problem
     * If the profile fetch is a network call there will be 1 call to fetch all the customers
     * and N calls to fetch profiles for those N customers.
     */

//    @SchemaMapping( typeName = "Customer")
//    public Profile profile(Customer customer){
//        System.out.println("Calling profile for customer " + customer.id());
//        return new Profile(customer.id());
//    }

}
