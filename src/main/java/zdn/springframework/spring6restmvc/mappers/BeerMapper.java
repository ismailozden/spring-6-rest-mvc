package zdn.springframework.spring6restmvc.mappers;

import org.mapstruct.Mapper;
import zdn.springframework.spring6restmvc.entities.Beer;
import zdn.springframework.spring6restmvc.model.BeerDTO;

@Mapper
public interface BeerMapper {

    Beer beerDtoToBeer(BeerDTO dto);

    BeerDTO beertoBeerDto(Beer beer);
}
