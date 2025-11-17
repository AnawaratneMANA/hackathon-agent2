package com.hackathon.hackathon.agent2.controller;

import com.hackathon.hackathon.agent2.dto.EOQResponseDTO;
import com.hackathon.hackathon.agent2.dto.InventoryRequestDTO;
import com.hackathon.hackathon.agent2.dto.InventoryStatusDTO;
import com.hackathon.hackathon.agent2.dto.ItemListDTO;
import com.hackathon.hackathon.agent2.service.impl.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    /**
     * GET inventory status for an SME item.
     * SME code passed as header "x-sme-code" or query param (fallback).
     */
    @GetMapping("/inventory/status/{itemCode}")
    public ResponseEntity<?> getInventoryStatus(
            @RequestHeader(value = "x-sme-code", required = false) String smeCodeHeader,
            @PathVariable("itemCode") String itemCode,
            @RequestParam(value = "sme", required = false) String smeQuery
    ) {
        String smeCode = (smeCodeHeader != null && !smeCodeHeader.isBlank()) ? smeCodeHeader : smeQuery;
        if (smeCode == null || smeCode.isBlank()) {
            return ResponseEntity.badRequest().body("Missing SME identifier: pass x-sme-code header or ?sme=SME_001");
        }
        try {
            InventoryStatusDTO dto = service.getInventoryStatus(smeCode, itemCode);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Internal error");
        }
    }

    /**
     * GET compute EOQ for item (calls service computeEOQ)
     */
    @GetMapping("/calc/eoq/{itemCode}")
    public ResponseEntity<?> getEOQ(
            @RequestHeader(value = "x-sme-code", required = false) String smeCodeHeader,
            @PathVariable("itemCode") String itemCode,
            @RequestParam(value = "sme", required = false) String smeQuery
    ) {
        String smeCode = (smeCodeHeader != null && !smeCodeHeader.isBlank()) ? smeCodeHeader : smeQuery;
        if (smeCode == null || smeCode.isBlank()) {
            return ResponseEntity.badRequest().body("Missing SME identifier: pass x-sme-code header or ?sme=SME_001");
        }
        try {
            EOQResponseDTO dto = service.computeEOQForItem(smeCode, itemCode);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Internal error");
        }
    }

    /**
     * Upsert inventory record for SME: create or update inventory row.
     */
    @PostMapping("/inventory/{itemCode}")
    public ResponseEntity<?> upsertInventory(
            @RequestHeader(value = "x-sme-code", required = false) String smeCodeHeader,
            @PathVariable("itemCode") String itemCode,
            @RequestBody InventoryRequestDTO req,
            @RequestParam(value = "sme", required = false) String smeQuery
    ) {
        String smeCode = (smeCodeHeader != null && !smeCodeHeader.isBlank()) ? smeCodeHeader : smeQuery;
        if (smeCode == null || smeCode.isBlank()) {
            return ResponseEntity.badRequest().body("Missing SME identifier");
        }
        // ensure path itemCode matches DTO itemCode if DTO provided
        if (req.getItemCode() == null || req.getItemCode().isBlank()) {
            req.setItemCode(itemCode);
        }
        try {
            InventoryStatusDTO dto = service.upsertInventory(smeCode, req);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Internal error");
        }
    }

    @GetMapping("/items")
    public ResponseEntity<?> listItems(
            @RequestHeader(value = "x-sme-code", required = false) String smeCodeHeader,
            @RequestParam(value = "sme", required = false) String smeQuery
    ) {
        String smeCode = (smeCodeHeader != null && !smeCodeHeader.isBlank()) ? smeCodeHeader : smeQuery;
        if (smeCode == null || smeCode.isBlank()) {
            return ResponseEntity.badRequest().body("Missing SME identifier: pass x-sme-code header or ?sme=SME_001");
        }
        try {
            List<ItemListDTO> rows = service.listItemsForSme(smeCode);
            return ResponseEntity.ok(rows);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Internal error");
        }
    }
}
