package zdn.springframework.spring6restmvc.services;

import org.springframework.stereotype.Service;
import zdn.springframework.spring6restmvc.model.Beer;

import java.util.UUID;

public interface BeerService {

    Beer getBeerByID(UUID id);

}
