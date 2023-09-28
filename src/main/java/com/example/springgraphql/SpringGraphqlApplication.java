package com.example.springgraphql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    AtomicInteger atomicInteger = new AtomicInteger();
    List<Customer> customerList = List.of( new Customer(atomicInteger.incrementAndGet(), "A"), new Customer(atomicInteger.incrementAndGet(), "B"));;

    @QueryMapping
    public List<Customer> customers() {
        return customerList;
    }

    @QueryMapping
    public Flux<Customer> customersReactive() {
        return Flux.just(new Customer(1, "A"), new Customer(2, "B"));
    }

    @QueryMapping
    public Customer customerById(@Argument Integer id) {
        return customers().get(id - 1);
    }

    @MutationMapping
    //@SchemaMapping(typeName = "Mutation", field = "addCustomer")
    public Customer addCustomer(@Argument String name) {
        Customer newCustomer = new Customer(atomicInteger.incrementAndGet(), name);
        customerList.add(newCustomer);
        return newCustomer;
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

record Greeting(String greeting){}

@Controller
class GreetingController {

    @QueryMapping
    public Greeting greeting(){
      return new Greeting("Warm welcome!!");
    }

    @SubscriptionMapping
    public Flux<Greeting> greetings() {
        return Flux.
                fromStream( Stream.generate(() -> new Greeting("Warm welcome @ " + Instant.now() + "!")))
                .delayElements(Duration.ofSeconds(1))
                .take(10);
    }

}
