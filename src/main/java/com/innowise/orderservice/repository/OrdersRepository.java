package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders,Long>, JpaSpecificationExecutor<Orders> {
  Page<Orders> findAll(Specification<Orders> spec, Pageable pageable);

  Page<Orders> findAll(Pageable pageable);

  Optional<List<Orders>> findByUserId(Long userId);
}

