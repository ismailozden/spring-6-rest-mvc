package zdn.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import zdn.springframework.spring6restmvc.config.SpringSecurityConfig;
import zdn.springframework.spring6restmvc.model.BeerDTO;
import zdn.springframework.spring6restmvc.services.BeerService;
import zdn.springframework.spring6restmvc.services.BeerServiceImpl;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static zdn.springframework.spring6restmvc.controller.CustomerControllerTest.PASSWORD;
import static zdn.springframework.spring6restmvc.controller.CustomerControllerTest.USERNAME;

@WebMvcTest(BeerController.class)
@Import(SpringSecurityConfig.class)
class BeerControllerTest {

    private static final String BEER_PATH = "/api/v1/beer";
    private static final String BEER_PATH_ID = BEER_PATH+"/{beerId}";

    public static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor JWT_REQUEST_POST_PROCESSOR =
            jwt().jwt(builder -> {
                builder.claims(claims -> {
                            claims.put("scope","message-read");
                            //claims.put("scope","message-write");
                        })
                        .subject("oidc-client")
                        .notBefore(Instant.now().minusSeconds(5L));
            });

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BeerService beerService;
    
    @Autowired
    ObjectMapper objectMapper;

    BeerServiceImpl beerServiceImpl;
    ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
    ArgumentCaptor<BeerDTO> beerArgumentCaptor = ArgumentCaptor.forClass(BeerDTO.class);

    @BeforeEach
    void setUp() {
        beerServiceImpl = new BeerServiceImpl();
    }

    @Test
    void getBeerById() throws Exception {
        BeerDTO testBeer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);

        given(beerService.getBeerByID(testBeer.getId())).willReturn(Optional.of(testBeer));

        mockMvc.perform(get(BEER_PATH_ID, testBeer.getId())
                        .with(JWT_REQUEST_POST_PROCESSOR)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testBeer.getId().toString())))
                .andExpect(jsonPath("$.beerName", is(testBeer.getBeerName())));

    }

    @Test
    void testListBeers() throws Exception {
        given(beerService.listBeers(any(), any(), any(), any(), any()))
                .willReturn(beerServiceImpl.listBeers(null, null, false, 1, 25));
        mockMvc.perform(get(BEER_PATH)
                        .with(JWT_REQUEST_POST_PROCESSOR)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()", is(3)));
    }

    @Test
    void testCreateNewBear() throws Exception {
    BeerDTO beer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);

    beer.setVersion(null);
    beer.setId(null);

    given(beerService.saveNewBeer(any(BeerDTO.class)))
            .willReturn(beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(1));

    mockMvc.perform(post(BEER_PATH)
                    .with(JWT_REQUEST_POST_PROCESSOR)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(beer)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"));

    }

    @Test
    void testUpdateBeer() throws Exception {
        BeerDTO beer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);

        given(beerService.updateBeerById(any(), any())).willReturn(Optional.of(beer));

        mockMvc.perform(put(BEER_PATH_ID, beer.getId())
                        .with(JWT_REQUEST_POST_PROCESSOR)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isNoContent());

        verify(beerService).updateBeerById(any(UUID.class),any(BeerDTO.class));
    }

    @Test
    void testDeleteBeer() throws Exception {
    BeerDTO beer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);

    given(beerService.deleteBeerById(any())).willReturn(true);

    mockMvc.perform(delete(BEER_PATH_ID, beer.getId())
                    .with(JWT_REQUEST_POST_PROCESSOR)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(beerService).deleteBeerById(uuidArgumentCaptor.capture());

        Assertions.assertEquals(beer.getId(),uuidArgumentCaptor.getValue());
    }

    @Test
    void testPatchBeer() throws Exception {
        BeerDTO beer = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);
        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New name");

        given(beerService.patchBeerById(any(),any())).willReturn(Optional.of(beer));

        mockMvc.perform(patch(BEER_PATH_ID, beer.getId())
                        .with(JWT_REQUEST_POST_PROCESSOR)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isNoContent());

        verify(beerService).patchBeerById(uuidArgumentCaptor.capture(),beerArgumentCaptor.capture());

        Assertions.assertEquals(beer.getId(),uuidArgumentCaptor.getValue());
        Assertions.assertEquals(beerMap.get("beerName"),beerArgumentCaptor.getValue().getBeerName());

        // TODO: 9/29/2023 Investigate .content(objectMapper.writeValueAsString(beerMap)))
        // Assertions.assertEquals("New name", beerServiceImpl.getBeerByID(beer.getId()).getBeerName());
        //beerServiceImpl.listBeers().get(1).setBeerName("HEBELE");
        //beerServiceImpl.patchBeerById(beer.getId(),beerServiceImpl.listBeers().get(1));
        //System.out.println(beerServiceImpl.listBeers().get(0));
        //System.out.println(beerServiceImpl.listBeers().get(1));
        //System.out.println(beerServiceImpl.listBeers().get(2));
        //Assertions.assertEquals(beerServiceImpl.getBeerByID(beer.getId()).getBeerName(),beerMap.get("beerName"));
    }

    @Test
    void getBeerByIdNotFound() throws Exception {
        given(beerService.getBeerByID(any(UUID.class))).willReturn(Optional.empty());

        mockMvc.perform(get(BEER_PATH_ID, UUID.randomUUID())
                        .with(JWT_REQUEST_POST_PROCESSOR))
                .andExpect(status().isNotFound());

    }

    @Test
    void testCreateBeerNullBeerDto() throws Exception {
        BeerDTO beerDTO = BeerDTO.builder().build();

        given(beerService.saveNewBeer(any(BeerDTO.class)))
                .willReturn(beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(1));

       MvcResult mvcResult = mockMvc.perform(post(BEER_PATH)
                       .with(JWT_REQUEST_POST_PROCESSOR)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.length()", is(6))).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());
    }

    @Test
    void testUpdateBeerNullBeerDto() throws Exception {
        BeerDTO beerDTO = beerServiceImpl.listBeers(null, null, false, 1, 25).getContent().get(0);
        BeerDTO beerNewDTO = BeerDTO.builder().build();

        given(beerService.updateBeerById(any(), any())).willReturn(Optional.of(beerDTO));

        MvcResult mvcResult = mockMvc.perform(put(BEER_PATH_ID, beerDTO.getId())
                        .with(JWT_REQUEST_POST_PROCESSOR)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerNewDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(6))).andReturn();

        System.out.println(mvcResult.getResponse().getContentAsString());

    }
}