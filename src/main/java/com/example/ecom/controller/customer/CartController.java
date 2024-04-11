package com.example.ecom.controller.customer;

import com.example.ecom.dto.AddProductToCartDto;
import com.example.ecom.dto.OrderDto;
import com.example.ecom.exception.ValidationException;
import com.example.ecom.service.customer.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer")
@CrossOrigin("*")
public class CartController {

    private final CartService cartService;

    @PostMapping("/cart")
    public ResponseEntity<?>addProductToCart(@RequestBody AddProductToCartDto addProductToCartDto){
        return cartService.addProductToCart(addProductToCartDto);
    }


    @GetMapping("/cart/{userId}")
    public ResponseEntity<OrderDto> getCartByUserId(@PathVariable("userId") Long id){
        return ResponseEntity.status(HttpStatus.OK).body(cartService.getCartByUserId(id));
    }

    @GetMapping("/coupon/{id}/{code}")
    public ResponseEntity<?>applyCoupon(@PathVariable("id")Long id,@PathVariable("code")String code){
        try{
           OrderDto orderDto =  cartService.applyCoupon(id,code);
           return ResponseEntity.status(HttpStatus.OK).body(orderDto);
        }catch (ValidationException ex){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
