package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.OrderItems;
import com.innowise.orderservice.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {
  void deleteByOrderId(Long id);

  Long order(Orders order);
}
