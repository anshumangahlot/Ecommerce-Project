package com.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {
    
    private Long id;

    private User user;

    private LocalDateTime orderDate;

    private String status;

    private BigDecimal totalAmount;

}
