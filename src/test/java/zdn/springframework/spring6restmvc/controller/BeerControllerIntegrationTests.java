package zdn.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import zdn.springframework.spring6restmvc.entities.Beer;
import zdn.springframework.spring6restmvc.mappers.BeerMapper;
import zdn.springframework.spring6restmvc.model.BeerDTO;
import zdn.springframework.spring6restmvc.repositories.BeerRepository;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class BeerControllerIntegrationTests {

    @Autowired
    BeerController beerController;
    @Autowired
    BeerRepository beerRepository;
    @Autowired
    BeerMapper beerMapper;
    @Autowired
    WebApplicationContext wac;
    @Autowired
    ObjectMapper objectMapper;
    private static final String BEER_PATH = "/api/v1/beer";
    private static final String BEER_PATH_ID = BEER_PATH+"/{beerId}";
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void testListBeers() {
        List<BeerDTO> dtos = beerController.listBeers();
        Assertions.assertEquals(dtos.size(),2413);
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
        Assertions.assertThrows(NotFoundException.class, () -> beerController.getBeerById(UUID.randomUUID()));
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

    @Rollback
    @Transactional
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

    @Test
    void testUpdateNotFound() {
        Assertions.assertThrows(NotFoundException.class,()-> beerController.updateById(UUID.randomUUID(),BeerDTO.builder().build()));
    }

    @Rollback
    @Transactional
    @Test
    void deleteByIdFound() {
        Beer beer = beerRepository.findAll().get(0);

        ResponseEntity<Void> responseEntity = beerController.deleteById(beer.getId());

        Assertions.assertEquals(responseEntity.getStatusCode(),HttpStatusCode.valueOf(204));
        Assertions.assertTrue(beerRepository.findById(beer.getId()).isEmpty());
    }

    @Test
    void testDeleteNotFound() {
        Assertions.assertThrows(NotFoundException.class,()-> beerController.deleteById(UUID.randomUUID()));
    }

    @Rollback
    @Transactional
    @Test
    void testPatch() {
        Beer beer = beerRepository.findAll().get(0);

        BeerDTO beerDTO = beerMapper.beertoBeerDto(beer);
        beerDTO.setId(null);
        beerDTO.setVersion(null);
        final String beerName = "UPDATED_BEER_NAME";
        beerDTO.setBeerName(beerName);

        ResponseEntity<Void> responseEntity = beerController.patchById(beer.getId(), beerDTO);

        Assertions.assertEquals(responseEntity.getStatusCode(),HttpStatusCode.valueOf(204));

        Optional<Beer> updatedBeer = beerRepository.findById(beer.getId());

        if (updatedBeer.isPresent()) {
            Assertions.assertEquals(updatedBeer.get().getBeerName(),beerName);
        } else {
            Assertions.fail("There is no beer with this Id");
        }
    }

    @Test
    void testPatchNotFound() {
        Assertions.assertThrows(NotFoundException.class,()-> beerController.patchById(UUID.randomUUID(),BeerDTO.builder().build()));
    }

    @Test
    void testPatchBeerBadName() throws Exception {
        Beer beer = beerRepository.findAll().get(0);
        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "876bzfbfgtuttuzit6766765675v567567cc67654647c47c464c64c45");

       MvcResult mvcResult = mockMvc.perform(patch(BEER_PATH_ID, beer.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.length()", is(1))).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());
    }

}