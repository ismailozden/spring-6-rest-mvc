package zdn.springframework.spring6restmvc.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zdn.springframework.spring6restmvc.entities.Beer;

import java.util.List;
import java.util.UUID;

public interface BeerRepository extends JpaRepository<Beer, UUID> {

    List<Beer> findAllByBeerNameIsLikeIgnoreCase(String beerName);

}
