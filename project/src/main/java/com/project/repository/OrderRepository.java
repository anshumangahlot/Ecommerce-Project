package com.project.repository;

import com.project.entity.Order;
import com.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order ,Long> {
    List<Order> findByUser(User user);
}
