package com.project.entity;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message="An order must belong to a user")
    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name="order_date",nullable = false)
    private LocalDateTime orderDate;

    @NotNull(message="Order status is required")
    @Column(nullable = false)
    private String status;

    @NotNull(message="Total amount is required")
    @Column(name="total_amount",nullable = false)
    private BigDecimal totalAmount;

    @PrePersist
    protected void onCreate(){
        this.orderDate= LocalDateTime.now();
        if(this.status==null){
            this.status="PENDING";
        }
    }
}
