package zdn.springframework.spring6restmvc.repositories;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import zdn.springframework.spring6restmvc.bootstrap.BootstrapData;
import zdn.springframework.spring6restmvc.entities.Beer;
import zdn.springframework.spring6restmvc.model.BeerStyle;
import zdn.springframework.spring6restmvc.services.BeerCsvServiceImpl;

import java.math.BigDecimal;
import java.util.List;

@DataJpaTest
@Import({BootstrapData.class, BeerCsvServiceImpl.class})
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testSaveBeer() {

        Beer savedBeer = beerRepository.save(Beer.builder()
                .beerName("New Beer")
                        .beerStyle(BeerStyle.GOSE)
                        .upc("435636")
                        .price(new BigDecimal("12.99"))
                .build());

        beerRepository.flush();

        Assertions.assertNotNull(savedBeer);
        Assertions.assertNotNull(savedBeer.getId());

    }

    @Test
    void testSaveBeerConstrainViolation() {

        Assertions.assertThrows(ConstraintViolationException.class, () -> {
        beerRepository.save(Beer.builder()
                .beerName("fdlsjfaiotizuiouijfjdsfoijdsafsdagiouioutiosdfjiosdjfjasdiofjjasoijdfioja")
                .beerStyle(BeerStyle.GOSE)
                .upc("435636")
                .price(new BigDecimal("12.99"))
                .build());

        beerRepository.flush();
        });
    }

    @Test
    void testGetBeerListByName() {
        Page<Beer> list = beerRepository.findAllByBeerNameIsLikeIgnoreCase("%IPA%", null);
        Assertions.assertEquals(list.getContent().size(),336);
    }
}