package zdn.springframework.spring6restmvc.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import zdn.springframework.spring6restmvc.entities.Customer;
import zdn.springframework.spring6restmvc.model.CustomerDTO;
import zdn.springframework.spring6restmvc.repositories.CustomerRepository;

import java.util.List;
import java.util.UUID;

@SpringBootTest
class CustomerControllerIntegrationTests {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerController customerController;

    @Test
    void listCustomers() {
        List<CustomerDTO> dtos = customerController.listCustomers();
        Assertions.assertEquals(dtos.size(),3);
    }

    @Rollback
    @Transactional
    @Test
    void listCustomersEmptyList() {
        customerRepository.deleteAll();
        List<CustomerDTO> dtos = customerController.listCustomers();
        Assertions.assertEquals(dtos.size(),0);
    }

    @Test
    void getCustomerById() {
        Customer customer = customerRepository.findAll().get(0);
        CustomerDTO customerDTO = customerController.getCustomerById(customer.getId());
        Assertions.assertNotNull(customerDTO);
    }

    @Test
    void getCustomerByIdNotFound() {
        Assertions.assertThrows(NotFoundException.class,()->{
            customerController.getCustomerById(UUID.randomUUID());
        });
    }
}