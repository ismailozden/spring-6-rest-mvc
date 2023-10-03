package zdn.springframework.spring6restmvc.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import zdn.springframework.spring6restmvc.entities.Beer;
import zdn.springframework.spring6restmvc.mappers.BeerMapper;
import zdn.springframework.spring6restmvc.model.BeerDTO;
import zdn.springframework.spring6restmvc.repositories.BeerRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
class BeerControllerIntegrationTests {

    @Autowired
    BeerController beerController;
    @Autowired
    BeerRepository beerRepository;
    @Autowired
    BeerMapper beerMapper;

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

    @Rollback
    @Transactional
    @Test
    void testSaveNewBear() {
        BeerDTO beerDTO = BeerDTO.builder()
                .beerName("New Bear")
                .build();

        ResponseEntity<Void> responseEntity = beerController.handlePost(beerDTO);

        Assertions.assertEquals(responseEntity.getStatusCode(), HttpStatusCode.valueOf(201));
        Assertions.assertNotNull(responseEntity.getHeaders().getLocation());

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath()
                .split("/");
        UUID savedUUID = UUID.fromString(locationUUID[4]);

        Optional<Beer> beer = beerRepository.findById(savedUUID);
        Assertions.assertNotNull(beer);
    }

    @Test
    void updateExistingBeer() {

        Beer beer = beerRepository.findAll().get(0);
        BeerDTO beerDTO = beerMapper.beertoBeerDto(beer);
        beerDTO.setId(null);
        beerDTO.setVersion(null);
        final String beerName = "UPDATED_BEER_NAME";
        beerDTO.setBeerName(beerName);

        ResponseEntity<Void> responseEntity = beerController.updateById(beer.getId(), beerDTO);

        Assertions.assertEquals(responseEntity.getStatusCode(),HttpStatusCode.valueOf(204));

        Optional<Beer> updatedBeer = beerRepository.findById(beer.getId());

        if (updatedBeer.isPresent()) {
        Assertions.assertEquals(updatedBeer.get().getBeerName(),beerName);
        } else {
        Assertions.fail("There is no beer with this Id");
        }


    }
}