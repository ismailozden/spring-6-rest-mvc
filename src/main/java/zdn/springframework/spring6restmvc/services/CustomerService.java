package zdn.springframework.spring6restmvc.services;

import zdn.springframework.spring6restmvc.model.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    List<Customer> listCustomers();

    Customer getCustomerByID(UUID id);
}
