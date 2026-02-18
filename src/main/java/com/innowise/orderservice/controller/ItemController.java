package com.innowise.orderservice.controller;

import com.innowise.orderservice.controller.api.ItemControllerApi;
import com.innowise.orderservice.model.dto.request.ItemsRequestDto;
import com.innowise.orderservice.model.dto.response.ItemsResponseDto;
import com.innowise.orderservice.service.ItemsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/items")
public class ItemController implements ItemControllerApi {

    private final ItemsService itemsService;

    @Override
    @PreAuthorize("@authorisationService.hasAdminRole(authentication)")
    @PostMapping
    public ResponseEntity<ItemsResponseDto> createItem(@RequestBody @Valid ItemsRequestDto requestDto) {
      ItemsResponseDto createdItem = itemsService.createItem(requestDto);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @Override
    @PreAuthorize("@authorisationService.hasAdminRole(authentication)")
    @GetMapping("/{id}")
    public ResponseEntity<ItemsResponseDto> getItemById(@PathVariable("id") Long id) {
      ItemsResponseDto item = itemsService.getItemById(id);
      return ResponseEntity.ok().body(item);
    }

    @Override
    @PreAuthorize("@authorisationService.hasAdminRole(authentication)")
    @PatchMapping("/{id}")
    public ResponseEntity<ItemsResponseDto> updateItemById(@PathVariable("id") Long id,
                                                            @RequestBody @Valid ItemsRequestDto requestDto) {
      ItemsResponseDto item = itemsService.updateItemById(requestDto, id);
      return ResponseEntity.ok().body(item);
    }

    @Override
    @PreAuthorize("@authorisationService.hasAdminRole(authentication)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemById(@PathVariable("id") Long id) {
      itemsService.deleteItemById(id);
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
