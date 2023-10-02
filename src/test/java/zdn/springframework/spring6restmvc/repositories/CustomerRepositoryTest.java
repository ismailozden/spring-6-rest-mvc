package zdn.springframework.spring6restmvc.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import zdn.springframework.spring6restmvc.entities.Customer;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;

    @Test
    void testSaveCustomer() {

        Customer saved = customerRepository.save(Customer.builder()
                .customerName("Tom Cr.").build());

        Assertions.assertNotNull(saved);
        Assertions.assertNotNull(saved.getId());

    }
}