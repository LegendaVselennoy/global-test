package org.example.beer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.BeerController;
import org.example.dto.BeerDTO;
import org.example.dto.BeerStyle;
import org.example.entity.Beer;
import org.example.exception.NotfoundException;
import org.example.mapper.BeerMapper;
import org.example.repository.BeerRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class BeerControllerIT {

    public static final String BEER_PATH = "/beer/";

    @Autowired
    BeerController beerController;
    @Autowired
    BeerRepository beerRepository;
    @Autowired
    BeerMapper beerMapper;
    @Autowired
    WebApplicationContext wac;
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;


    @Test
    void testDeleteByNotFound() {
        assertThrows(NotfoundException.class, () -> {
            beerController.deleteById(UUID.randomUUID());
        });
    }

    @Rollback
    @Transactional
    @Test
    void deleteByIdFound() {
        Beer beer = beerRepository.findAll().get(0);

        ResponseEntity<BeerDTO> responseEntity = beerController.deleteById(beer.getBeerId());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(beerRepository.findById(beer.getBeerId())).isEmpty();

    }

    @Test
    void testUpdateNotFound() {
        assertThrows(NotfoundException.class, () -> {
            beerController.updateBeer(UUID.randomUUID(), BeerDTO.builder().build());
        });
    }

    @Rollback
    @Transactional
    @Test
    void updateExistingBeer() {
        Beer beer = beerRepository.findAll().get(0);
        BeerDTO beerDTO = beerMapper.beerToBeerDTO(beer);
        beerDTO.setBeerId(null);
        beerDTO.setVersion(null);
        final String beerName = "UPDATED";
        beerDTO.setBeerName(beerName);

        ResponseEntity<BeerDTO> responseEntity = beerController.updateBeer(beer.getBeerId(), beerDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(204));

        Beer updateBeer = beerRepository.findById(beer.getBeerId()).get();
        assertThat(updateBeer.getBeerName()).isEqualTo(beerName);
    }

    @Test
    void testListBeers() {
        Page<BeerDTO> dtos = beerController.listBeers(null, null, null, 1, 25);
        assertThat(dtos.getContent().size()).isEqualTo(3);
    }

    @Test
    void testListBeersByStyleAndNameShowInventoryPage() throws Exception {
        mockMvc.perform(get(BEER_PATH)
                        .queryParam("beerName", "PALE_ALE")
                        .queryParam("beerStyle", BeerStyle.PALE_ALE.name())
                        .queryParam("showInventory", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(50)))
                .andExpect(jsonPath("$.[0].quantityOnHand").value(IsNull.notNullValue()));
    }

    @Test
    void testListBeersByStyleAndNameShowInventoryTrue() throws Exception {
        mockMvc.perform(get(BEER_PATH)
                        .queryParam("beerName", "PALE_ALE")
                        .queryParam("beerStyle", BeerStyle.PALE_ALE.name())
                        .queryParam("showInventory", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(310)))
                .andExpect(jsonPath("$.[0].quantityOnHand").value(IsNull.notNullValue()));
    }

    @Test
    void testListBeersByStyleAndNameShowInventoryFalse() throws Exception {
        mockMvc.perform(get(BEER_PATH)
                        .queryParam("beerName", "PALE_ALE")
                        .queryParam("beerStyle", BeerStyle.PALE_ALE.name())
                        .queryParam("showInventory", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(310)))
                .andExpect(jsonPath("$.[0].quantityOnHand").value(IsNull.nullValue()));
    }

    @Test
    void testListBeersByName() throws Exception {
        mockMvc.perform(get(BEER_PATH)
                        .queryParam("beerName", "IPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(336)));
    }

    @Test
    void testPatchBeerBadName() throws Exception {

        Beer beer = beerRepository.findAll().get(0);

        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New Name Validation Valid");

        mockMvc.perform(patch(BEER_PATH + beer.getBeerId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(beerMap)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testBeerIdNotFound() {
        assertThrows(ChangeSetPersister.NotFoundException.class, () -> {
            beerController.getBeerId(UUID.randomUUID());
        });
    }

    @Test
    @Rollback
    @Transactional
    void saveNewBeerTest() {
        BeerDTO beerDTO = BeerDTO.builder()
                .beerName("New name")
                .build();
        ResponseEntity<BeerDTO> responseEntity = beerController.handlePost(beerDTO);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(201));
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID saveUUID = UUID.fromString(locationUUID[2]);

        Beer beer = beerRepository.findById(saveUUID).get();
        assertThat(beer).isNotNull();
    }

    @Test
    void testGetById() {

        Beer beer = beerRepository.findAll().get(0);

        BeerDTO dto = beerController.getBeerId(beer.getBeerId());

        assertThat(dto).isNotNull();
    }

    @Test
    @Rollback
    @Transactional
    void testEmptyList() {

        beerRepository.deleteAll();
        Page<BeerDTO> dtos = beerController.listBeers(null, null, null, 1, 25);

        assertThat(dtos.getContent().size()).isEqualTo(0);
    }

    @Test
    void testUpdateBeerBadVersion() throws Exception {
        Beer beer = beerRepository.findAll().get(0);
        BeerDTO beerDTO = beerMapper.beerToBeerDTO(beer);

        beerDTO.setBeerName("Updated Name");

        MvcResult result = mockMvc.perform(put(BEER_PATH + beer.getBeerId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(beerDTO)))
                .andExpect(status().isNoContent())
                .andReturn();

        beerDTO.setBeerName("Updated Name 2");

        System.out.println(result.getResponse().getContentAsString());

        MvcResult result2 = mockMvc.perform(put(BEER_PATH + beer.getBeerId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(beerDTO)))
                .andExpect(status().isNoContent())
                .andReturn();

        System.out.println(result2.getResponse().getStatus());

    }
}