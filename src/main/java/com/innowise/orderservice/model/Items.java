package com.innowise.orderservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringExclude;

import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "items")
public class Items extends Auditable{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false)
  private BigDecimal price;

  @ToStringExclude
  @OneToMany(mappedBy = "item")
  private List<OrderItems> orderItems;
}
