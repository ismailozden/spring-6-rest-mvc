package zdn.springframework.spring6restmvc.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import zdn.springframework.spring6restmvc.entities.Beer;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testSaveBeer() {

        Beer savedBeer = beerRepository.save(Beer.builder()
                .beerName("New Beer")
                .build());

        Assertions.assertNotNull(savedBeer);
        Assertions.assertNotNull(savedBeer.getId());

    }
}