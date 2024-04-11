package com.example.ecom.service.customer.cart;

import com.example.ecom.dto.AddProductToCartDto;
import com.example.ecom.dto.OrderDto;
import org.springframework.http.ResponseEntity;

public interface CartService {

     ResponseEntity<?> addProductToCart(AddProductToCartDto addProductToCartDto);

    OrderDto getCartByUserId(Long id);
    OrderDto applyCoupon(Long userId,String couponCode);

}
