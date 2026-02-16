package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders,Long>, JpaSpecificationExecutor<Orders> {

  Optional<List<Orders>> findByUserId(Long userId);
}

