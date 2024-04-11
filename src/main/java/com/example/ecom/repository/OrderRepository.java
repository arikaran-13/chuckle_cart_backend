package com.example.ecom.repository;

import com.example.ecom.entity.customer.Order;
import com.example.ecom.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

    Order findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus);
}
