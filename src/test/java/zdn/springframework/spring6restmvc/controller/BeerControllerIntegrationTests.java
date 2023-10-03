package zdn.springframework.spring6restmvc.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import zdn.springframework.spring6restmvc.entities.Beer;
import zdn.springframework.spring6restmvc.model.BeerDTO;
import zdn.springframework.spring6restmvc.repositories.BeerRepository;

import java.util.List;
import java.util.UUID;

@SpringBootTest
class BeerControllerIntegrationTests {

    @Autowired
    BeerController beerController;
    @Autowired
    BeerRepository beerRepository;

    @Test
    void testListBeers() {
        List<BeerDTO> dtos = beerController.listBeers();
        Assertions.assertEquals(dtos.size(),3);
    }

    @Rollback
    @Transactional
    @Test
    void testEmptyList() {
beerRepository.deleteAll();
        List<BeerDTO> dtos = beerController.listBeers();
Assertions.assertEquals(dtos.size(),0);
    }

    @Test
    void testGetByID() {
        Beer beer = beerRepository.findAll().get(0);
        BeerDTO dto = beerController.getBeerById(beer.getId());
        Assertions.assertNotNull(dto);
    }

    @Test
    void testBeerIdNotFound() {
        Assertions.assertThrows(NotFoundException.class, () -> {
            beerController.getBeerById(UUID.randomUUID());
        });
    }
}