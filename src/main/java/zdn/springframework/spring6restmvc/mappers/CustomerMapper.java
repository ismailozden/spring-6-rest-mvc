package zdn.springframework.spring6restmvc.mappers;

import org.mapstruct.Mapper;
import zdn.springframework.spring6restmvc.entities.Customer;
import zdn.springframework.spring6restmvc.model.CustomerDTO;

@Mapper
public interface CustomerMapper {

    Customer customerDtoToCustomer(CustomerDTO dto);

    CustomerDTO customerToCustomerDto(Customer customer);

}
