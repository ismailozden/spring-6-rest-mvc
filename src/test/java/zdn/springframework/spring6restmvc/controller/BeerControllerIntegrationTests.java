package zdn.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
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
import zdn.springframework.spring6restmvc.model.BeerStyle;
import zdn.springframework.spring6restmvc.repositories.BeerRepository;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static zdn.springframework.spring6restmvc.controller.CustomerControllerTest.PASSWORD;
import static zdn.springframework.spring6restmvc.controller.CustomerControllerTest.USERNAME;

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
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity()).build();
    }

    @Test
    void testListBeers() {
        Page<BeerDTO> dtos = beerController.listBeers(null, null, false, 1, 1000);
        Assertions.assertEquals(dtos.getContent().size(),1000);
    }

    @Rollback
    @Transactional
    @Test
    void testEmptyList() {
beerRepository.deleteAll();
        Page<BeerDTO> dtos = beerController.listBeers(null,null, false, 1, 25);
Assertions.assertEquals(dtos.getContent().size(),0);
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
                       .with(httpBasic(USERNAME,PASSWORD))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.length()", is(1))).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());
    }

    @Test
    void testListBeerByName() throws Exception {

        mockMvc.perform(get(BEER_PATH)
                        .with(httpBasic(USERNAME,PASSWORD))
                .queryParam("beerName","IPA")
                        .queryParam("pageSize","336"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(336)));

    }

    @Test
    void testListBeerByStyle() throws Exception {

        mockMvc.perform(get(BEER_PATH)
                        .with(httpBasic(USERNAME,PASSWORD))
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("pageSize","548"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(548)));

    }

    @Test
    void tesListBeersByStyleAndNameShowInventoryTrue() throws Exception {
        mockMvc.perform(get(BEER_PATH)
                        .with(httpBasic(USERNAME,PASSWORD))
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "true")
                        .queryParam("pageSize","309"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(309)))
                .andExpect(jsonPath("$.content.[0].quantityOnHand").value(IsNull.notNullValue()));
    }

    @Test
    void tesListBeersByStyleAndNameShowInventoryFalse() throws Exception {
        mockMvc.perform(get(BEER_PATH)
                        .with(httpBasic(USERNAME,PASSWORD))
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "false")
                        .queryParam("pageSize", "800"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(310)))
                .andExpect(jsonPath("$.content.[0].quantityOnHand").value(IsNull.nullValue()));
    }

    @Test
    void tesListBeersByStyleAndName() throws Exception {
        mockMvc.perform(get(BEER_PATH)
                        .with(httpBasic(USERNAME,PASSWORD))
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("pageSize", "1800"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(310)));
    }

    @Test
    void tesListBeersByStyleAndNameShowInventoryTruePage2() throws Exception {
        mockMvc.perform(get(BEER_PATH)
                        .with(httpBasic(USERNAME,PASSWORD))
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "true")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(50)))
                .andExpect(jsonPath("$.content[0].quantityOnHand").value(IsNull.notNullValue())).andReturn();
    }
}