package zdn.springframework.spring6restmvc.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import zdn.springframework.spring6restmvc.entities.Customer;
import zdn.springframework.spring6restmvc.mappers.CustomerMapper;
import zdn.springframework.spring6restmvc.model.CustomerDTO;
import zdn.springframework.spring6restmvc.repositories.CustomerRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
class CustomerControllerIntegrationTests {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerController customerController;

    @Autowired
    CustomerMapper customerMapper;

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
        Assertions.assertThrows(NotFoundException.class,()-> customerController.getCustomerById(UUID.randomUUID()));
    }

    @Rollback
    @Transactional
    @Test
    void testSaveNewCustomer() {
        CustomerDTO customerDTO = CustomerDTO.builder()
                .customerName("New Customer")
                .build();

        ResponseEntity<Void> responseEntity = customerController.handlePost(customerDTO);

        Assertions.assertEquals(responseEntity.getStatusCode(), HttpStatusCode.valueOf(201));
        Assertions.assertNotNull(responseEntity.getHeaders().getLocation());

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath()
                .split("/");
        UUID savedUUID = UUID.fromString(locationUUID[4]);

        Optional<Customer> customer = customerRepository.findById(savedUUID);
        Assertions.assertNotNull(customer);

        if (customer.isPresent()) {
            Assertions.assertEquals("New Customer", customer.get().getCustomerName());
        } else {
            Assertions.fail("The customer is not present.");        }
    }

    @Rollback
    @Transactional
    @Test
    void updateExistingCustomer() {

        Customer customer = customerRepository.findAll().get(0);
        CustomerDTO customerDTO = customerMapper.customerToCustomerDto(customer);
        customerDTO.setId(null);
        customerDTO.setVersion(null);
        final String customerName = "UPDATED_BEER_NAME";
        customerDTO.setCustomerName(customerName);

        ResponseEntity<Void> responseEntity = customerController.updateById(customer.getId(), customerDTO);

        Assertions.assertEquals(responseEntity.getStatusCode(),HttpStatusCode.valueOf(204));

        Optional<Customer> updatedCustomer = customerRepository.findById(customer.getId());

        if (updatedCustomer.isPresent()) {
            Assertions.assertEquals(updatedCustomer.get().getCustomerName(),customerName);
        } else {
            Assertions.fail("There is no customer with this Id");
        }
    }

    @Test
    void testUpdateNotFound() {
        Assertions.assertThrows(NotFoundException.class,()-> customerController.updateById(UUID.randomUUID(),CustomerDTO.builder().build()));
    }

    @Rollback
    @Transactional
    @Test
    void deleteByIdFound() {
        Customer customer = customerRepository.findAll().get(0);

        ResponseEntity<Void> responseEntity = customerController.deleteById(customer.getId());

        Assertions.assertEquals(responseEntity.getStatusCode(),HttpStatusCode.valueOf(204));
        Assertions.assertTrue(customerRepository.findById(customer.getId()).isEmpty());
    }

    @Test
    void testDeleteNotFound() {
        Assertions.assertThrows(NotFoundException.class,()-> customerController.deleteById(UUID.randomUUID()));
    }

    @Rollback
    @Transactional
    @Test
    void testPatch() {
        Customer customer = customerRepository.findAll().get(0);

        CustomerDTO customerDTO = customerMapper.customerToCustomerDto(customer);
        customerDTO.setId(null);
        customerDTO.setVersion(null);
        final String customerName = "UPDATED_BEER_NAME";
        customerDTO.setCustomerName(customerName);

        ResponseEntity<Void> responseEntity = customerController.patchById(customer.getId(), customerDTO);

        Assertions.assertEquals(responseEntity.getStatusCode(),HttpStatusCode.valueOf(204));

        Optional<Customer> updatedCustomer = customerRepository.findById(customer.getId());

        if (updatedCustomer.isPresent()) {
            Assertions.assertEquals(updatedCustomer.get().getCustomerName(),customerName);
        } else {
            Assertions.fail("There is no customer with this Id");
        }
    }

    @Test
    void testPatchNotFound() {
        Assertions.assertThrows(NotFoundException.class,()-> customerController.patchById(UUID.randomUUID(),CustomerDTO.builder().build()));
    }
}