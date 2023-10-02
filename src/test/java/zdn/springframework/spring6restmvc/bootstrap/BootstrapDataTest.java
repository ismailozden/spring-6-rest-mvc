package zdn.springframework.spring6restmvc.bootstrap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import zdn.springframework.spring6restmvc.repositories.BeerRepository;
import zdn.springframework.spring6restmvc.repositories.CustomerRepository;
import zdn.springframework.spring6restmvc.services.BeerServiceImpl;
import zdn.springframework.spring6restmvc.services.CustomerServiceImpl;

@DataJpaTest
class BootstrapDataTest {

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    CustomerRepository customerRepository;

    BootstrapData bootstrapData;

    @BeforeEach
    void setUp() {
        bootstrapData = new BootstrapData(beerRepository, customerRepository);
    }

    @Test
    void run() {
        BeerServiceImpl beerService = new BeerServiceImpl();
        System.out.println(beerService.listBeers());

        CustomerServiceImpl customerService = new CustomerServiceImpl();
        System.out.println(customerService.listCustomers());
    }

    @Test
    void testRun() throws Exception {
        bootstrapData.run((String) null);

        Assertions.assertEquals(beerRepository.count(),3);
        Assertions.assertEquals(customerRepository.count(),3);

    }
}