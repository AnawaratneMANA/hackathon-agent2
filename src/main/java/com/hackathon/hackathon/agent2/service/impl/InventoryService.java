package com.hackathon.hackathon.agent2.service.impl;
import com.hackathon.hackathon.agent2.domain.Inventory;
import com.hackathon.hackathon.agent2.domain.Item;
import com.hackathon.hackathon.agent2.domain.SME;
import com.hackathon.hackathon.agent2.domain.Vendor;
import com.hackathon.hackathon.agent2.dto.EOQResponseDTO;
import com.hackathon.hackathon.agent2.dto.InventoryRequestDTO;
import com.hackathon.hackathon.agent2.dto.InventoryStatusDTO;
import com.hackathon.hackathon.agent2.dto.ItemListDTO;
import com.hackathon.hackathon.agent2.proxy.InventoryRepository;
import com.hackathon.hackathon.agent2.proxy.ItemRepository;
import com.hackathon.hackathon.agent2.proxy.SMERepository;
import com.hackathon.hackathon.agent2.proxy.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final SMERepository smeRepository;
    private final VendorRepository vendorRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            ItemRepository itemRepository,
                            SMERepository smeRepository,
                            VendorRepository vendorRepository) {
        this.inventoryRepository = inventoryRepository;
        this.itemRepository = itemRepository;
        this.smeRepository = smeRepository;
        this.vendorRepository = vendorRepository;
    }

    /**
     * Get inventory status DTO for given SME code and item code.
     */
    @Transactional(readOnly = true)
    public InventoryStatusDTO getInventoryStatus(String smeCode, String itemCode) {
        Optional<Inventory> opt = inventoryRepository.findBySme_SmeCodeAndItem_ItemCode(smeCode, itemCode);
        Inventory inv = opt.orElseThrow(() -> new NoSuchElementException("Inventory not found for SME: " + smeCode + " item: " + itemCode));

        InventoryStatusDTO dto = new InventoryStatusDTO();
        dto.setItemId(inv.getItem().getItemCode());
        dto.setItemName(inv.getItem().getItemName());
        dto.setCurrentStock(inv.getCurrentStock() == null ? 0 : inv.getCurrentStock());
        dto.setDailyConsumption(inv.getDailyConsumption() == null ? 0.0 : inv.getDailyConsumption());
        dto.setLeadTimeDays(inv.getLeadTimeDays() == null ? 0 : inv.getLeadTimeDays());
        dto.setSafetyStock(inv.getSafetyStock() == null ? 0 : inv.getSafetyStock());

        // vendorType only - do not expose PII
        Vendor v = inv.getVendor();
        if (v != null) {
            dto.setVendorType(v.getVendorType());
        } else {
            dto.setVendorType("UNKNOWN");
        }
        return dto;
    }

    /**
     * Compute EOQ for the item for given SME using local formula.
     * orderingCost and holdingCost can be tuned or retrieved from config.
     */
    @Transactional(readOnly = true)
    public EOQResponseDTO computeEOQForItem(String smeCode, String itemCode) {
        InventoryStatusDTO status = getInventoryStatus(smeCode, itemCode);

        double daily = status.getDailyConsumption();
        double orderingCost = 50.0;
        double holdingCost = 1.0;

        double annualDemand = daily * 365.0;
        int eoq = computeEOQ(daily, orderingCost, holdingCost);

        EOQResponseDTO dto = new EOQResponseDTO();
        dto.setItemId(status.getItemId());
        dto.setAnnualDemand(annualDemand);
        dto.setOrderingCost(orderingCost);
        dto.setHoldingCostPerUnit(holdingCost);
        dto.setEoq(eoq);
        return dto;
    }

    private int computeEOQ(double dailyConsumption, double orderingCost, double holdingCostPerUnit) {
        if (dailyConsumption <= 0.0) return 0;
        double D = dailyConsumption * 365.0;
        double eoq = Math.sqrt((2.0 * D * orderingCost) / holdingCostPerUnit);
        return Math.max(1, (int)Math.round(eoq));
    }

    /**
     * Upsert inventory. If inventory exists for sme+item update, else create row.
     */
    @Transactional
    public InventoryStatusDTO upsertInventory(String smeCode, InventoryRequestDTO req) {
        SME sme = smeRepository.findBySmeCode(smeCode).orElseThrow(() -> new NoSuchElementException("SME not found: " + smeCode));
        Item item = itemRepository.findByItemCode(req.getItemCode()).orElseThrow(() -> new NoSuchElementException("Item not found: " + req.getItemCode()));

        Optional<Inventory> optInv = inventoryRepository.findBySme_SmeCodeAndItem_ItemCode(smeCode, req.getItemCode());
        Inventory inv;
        if (optInv.isPresent()) {
            inv = optInv.get();
        } else {
            inv = new Inventory();
            inv.setSme(sme);
            inv.setItem(item);
        }

        inv.setCurrentStock(req.getCurrentStock());
        inv.setDailyConsumption(req.getDailyConsumption());
        inv.setLeadTimeDays(req.getLeadTimeDays());
        inv.setSafetyStock(req.getSafetyStock());
        inv.setLastUpdated(java.time.OffsetDateTime.now());

        if (req.getVendorId() != null) {
            Vendor v = vendorRepository.findById(req.getVendorId())
                    .orElseThrow(() -> new NoSuchElementException("Vendor not found: " + req.getVendorId()));
            inv.setVendor(v);
        }

        Inventory saved = inventoryRepository.save(inv);

        // map back to DTO
        InventoryStatusDTO dto = new InventoryStatusDTO();
        dto.setItemId(saved.getItem().getItemCode());
        dto.setItemName(saved.getItem().getItemName());
        dto.setCurrentStock(saved.getCurrentStock());
        dto.setDailyConsumption(saved.getDailyConsumption());
        dto.setLeadTimeDays(saved.getLeadTimeDays());
        dto.setVendorType(saved.getVendor() != null ? saved.getVendor().getVendorType() : "UNKNOWN");
        dto.setSafetyStock(saved.getSafetyStock());
        return dto;
    }

    /**
     * Get a list of items given the sme.
     * @param smeCode > SME Code.
     * @return
     */
    @Transactional(readOnly = true)
    public List<ItemListDTO> listItemsForSme(String smeCode) {
        return inventoryRepository.findItemListBySmeCode(smeCode);
    }
}
