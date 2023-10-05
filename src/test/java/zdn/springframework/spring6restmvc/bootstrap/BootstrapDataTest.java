package zdn.springframework.spring6restmvc.bootstrap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import zdn.springframework.spring6restmvc.repositories.BeerRepository;
import zdn.springframework.spring6restmvc.repositories.CustomerRepository;
import zdn.springframework.spring6restmvc.services.BeerCsvService;
import zdn.springframework.spring6restmvc.services.BeerCsvServiceImpl;
import zdn.springframework.spring6restmvc.services.BeerServiceImpl;
import zdn.springframework.spring6restmvc.services.CustomerServiceImpl;

@DataJpaTest
@Import(BeerCsvServiceImpl.class)
class BootstrapDataTest {

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    BeerCsvService csvService;

    BootstrapData bootstrapData;

    @BeforeEach
    void setUp() {
        bootstrapData = new BootstrapData(beerRepository, customerRepository, csvService);
    }

    @Test
    void run() {
        BeerServiceImpl beerService = new BeerServiceImpl();
        System.out.println(beerService.listBeers(null, null, false, 1, 25));

        CustomerServiceImpl customerService = new CustomerServiceImpl();
        System.out.println(customerService.listCustomers());
    }

    @Test
    void testRun() throws Exception {
        bootstrapData.run((String) null);

        Assertions.assertEquals(beerRepository.count(),2413);
        Assertions.assertEquals(customerRepository.count(),3);

    }
}