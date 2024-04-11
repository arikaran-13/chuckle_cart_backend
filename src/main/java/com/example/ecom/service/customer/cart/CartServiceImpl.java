package com.example.ecom.service.customer.cart;

import com.example.ecom.dto.AddProductToCartDto;
import com.example.ecom.dto.OrderDto;
import com.example.ecom.entity.admin.Coupon;
import com.example.ecom.entity.admin.Product;
import com.example.ecom.entity.customer.CartItem;
import com.example.ecom.entity.customer.Order;
import com.example.ecom.entity.customer.User;
import com.example.ecom.enums.OrderStatus;
import com.example.ecom.exception.ValidationException;
import com.example.ecom.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService{
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final AdminProductRepository productRepository;
    private final CouponRepository couponRepository;


    public ResponseEntity<?> addProductToCart(AddProductToCartDto addProductToCartDto){
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductToCartDto.getUserId(), OrderStatus.Pending);
        Optional<CartItem> optCartItem = cartItemRepository.findByProductIdAndOrderIdAndUserId(addProductToCartDto.getProductId(),activeOrder.getId(),addProductToCartDto.getUserId());

        if(optCartItem.isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } else {
            Optional<Product> optProduct = productRepository.findById(addProductToCartDto.getProductId());
            Optional<User> optUser = userRepository.findById(addProductToCartDto.getUserId());

            if(optProduct.isPresent() && optUser.isPresent()){
                CartItem cartItem = new CartItem();
                cartItem.setUser(optUser.get());
                cartItem.setProduct(optProduct.get());
                cartItem.setQuantity(1L);
                cartItem.setPrice(optProduct.get().getPrice());
                cartItem.setOrder(activeOrder);

                CartItem updatedCart = cartItemRepository.save(cartItem);
                var amt = activeOrder.getTotalAmount() + cartItem.getPrice();
                activeOrder.setTotalAmount(amt);
                activeOrder.setAmount(amt);

                activeOrder.getCartItems().add(cartItem);
                orderRepository.save(activeOrder); // Saving the updated order

                return ResponseEntity.status(HttpStatus.CREATED).body(cartItem);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or product not found");
            }
        }
    }

    @Override
    public OrderDto getCartByUserId(Long id) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(id , OrderStatus.Pending);
       var cartItemDto= activeOrder.getCartItems().stream().map(CartItem::getCartDto).collect(Collectors.toList());
        OrderDto orderDto = new OrderDto();
        orderDto.setAmount(activeOrder.getAmount());
        orderDto.setId(activeOrder.getId());
        orderDto.setOrderStatus(activeOrder.getOrderStatus());
        orderDto.setDiscount(activeOrder.getDiscount());
        orderDto.setTotalAmount(activeOrder.getTotalAmount());
        orderDto.setCartItems(cartItemDto);
        if(activeOrder.getCoupon() != null){
            orderDto.setCouponName(activeOrder.getCoupon().getName());
        }
        return orderDto;
    }

    public OrderDto applyCoupon(Long userId,String couponCode){
        var activeOrder = orderRepository.findByUserIdAndOrderStatus(userId,OrderStatus.Pending);
        Coupon coupon = couponRepository.findByCode(couponCode).orElseThrow(()-> new ValidationException("Coupon Not found"));

        if(isCouponExpired(coupon)){
            throw new ValidationException("Coupon Expired : "+ couponCode);
        }
        var discountedAmt = ( coupon.getDiscount() / 100.0 ) * activeOrder.getTotalAmount();
        var netAmount = activeOrder.getTotalAmount() - discountedAmt;

        activeOrder.setAmount((long)netAmount);
        activeOrder.setDiscount((long)discountedAmt);
        activeOrder.setCoupon(coupon);

        Order updatedOrder = orderRepository.save(activeOrder);

        return updatedOrder.getOrderDto();
    }

    private boolean isCouponExpired(Coupon coupon) {
        var currentDate = new Date();
        return coupon.getExpirationDate()!= null && currentDate.after(coupon.getExpirationDate());
    }

}
