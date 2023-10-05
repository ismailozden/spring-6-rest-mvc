package zdn.springframework.spring6restmvc.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import zdn.springframework.spring6restmvc.model.BeerDTO;
import zdn.springframework.spring6restmvc.model.BeerStyle;
import zdn.springframework.spring6restmvc.services.BeerService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BeerController {

    private static final String BEER_PATH = "/api/v1/beer";
    private static final String BEER_PATH_ID = BEER_PATH+"/{beerId}";

    private final BeerService beerService;

    @PostMapping(BEER_PATH)
    public ResponseEntity<Void> handlePost(@Validated @RequestBody BeerDTO beer){
        BeerDTO savedBeer = beerService.saveNewBeer(beer);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location","/api/v1/beer/" + savedBeer.getId().toString());

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @GetMapping(BEER_PATH)
    public List<BeerDTO> listBeers(@RequestParam(required = false) String beerName,
                                   @RequestParam(required = false)BeerStyle beerStyle){
        return beerService.listBeers(beerName, beerStyle);
    }

    @RequestMapping(value = BEER_PATH_ID, method = RequestMethod.GET)
    public BeerDTO getBeerById(@PathVariable("beerId") UUID beerId){

        log.debug("Get Beer by Id - in controller.");

        return beerService.getBeerByID(beerId).orElseThrow(NotFoundException::new);
    }

    @PutMapping(BEER_PATH_ID)
    public ResponseEntity<Void> updateById(@PathVariable("beerId") UUID beerId,@Validated @RequestBody BeerDTO beer){
       if (beerService.updateBeerById(beerId, beer).isEmpty()){
           throw new NotFoundException();
       }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(BEER_PATH_ID)
    public ResponseEntity<Void> deleteById(@PathVariable("beerId") UUID beerId){
        if (!beerService.deleteBeerById(beerId)){
            throw new NotFoundException();
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping(BEER_PATH_ID)
    public ResponseEntity<Void> patchById(@PathVariable("beerId") UUID beerId, @RequestBody BeerDTO beer){
        if (beerService.patchBeerById(beerId, beer).isEmpty()){
            throw new NotFoundException();
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
