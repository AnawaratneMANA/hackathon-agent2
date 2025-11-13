package com.hackathon.hackathon.agent2.proxy;

import com.hackathon.hackathon.agent2.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findBySme_SmeCodeAndItem_ItemCode(String smeCode, String itemCode);
}