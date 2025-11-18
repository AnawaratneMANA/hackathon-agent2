package com.hackathon.hackathon.agent2.proxy;

import com.hackathon.hackathon.agent2.domain.Inventory;
import com.hackathon.hackathon.agent2.dto.ItemListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findBySme_SmeCodeAndItem_ItemCode(String smeCode, String itemCode);

    @Query("SELECT new com.hackathon.hackathon.agent2.dto.ItemListDTO(" +
            "it.itemCode, it.itemName, inv.currentStock, inv.dailyConsumption, inv.leadTimeDays, " +
            "CAST(CASE WHEN inv.dailyConsumption > 0 THEN (inv.currentStock / inv.dailyConsumption) ELSE 0.0 END AS double), " +
            "COALESCE(v.vendorType, 'UNKNOWN'), " +
            "inv.safetyStock" +
            ") " +
            "FROM Inventory inv " +
            "JOIN inv.item it " +
            "JOIN inv.sme s " +
            "LEFT JOIN inv.vendor v " +
            "WHERE s.smeCode = :smeCode " +
            "ORDER BY it.itemCode")
    List<ItemListDTO> findItemListBySmeCode(@Param("smeCode") String smeCode);

}