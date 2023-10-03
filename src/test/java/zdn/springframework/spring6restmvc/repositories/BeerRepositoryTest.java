package zdn.springframework.spring6restmvc.repositories;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import zdn.springframework.spring6restmvc.entities.Beer;
import zdn.springframework.spring6restmvc.model.BeerStyle;

import java.math.BigDecimal;

@DataJpaTest
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
}