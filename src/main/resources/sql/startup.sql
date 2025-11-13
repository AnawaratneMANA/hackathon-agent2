-- ==============================================================
-- ProcureLens: Schema + Mock Data + Useful Queries
-- Run against database: procurelensdb
-- ==============================================================

BEGIN;

-- 1) SME table
CREATE TABLE IF NOT EXISTS sme (
                                   id SERIAL PRIMARY KEY,
                                   sme_code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    phone VARCHAR(50),
    created_at TIMESTAMPTZ DEFAULT now()
    );

-- 2) Vendor table (contains PII; keep behind Spring)
CREATE TABLE IF NOT EXISTS vendor (
                                      id SERIAL PRIMARY KEY,
                                      vendor_name VARCHAR(255) NOT NULL,
    vendor_type VARCHAR(100) NOT NULL,     -- e.g., "Local Distributor", "Importer"
    contact_name VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
    );

-- 3) Item master table
CREATE TABLE IF NOT EXISTS item (
                                    id SERIAL PRIMARY KEY,
                                    item_code VARCHAR(100) UNIQUE NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
    );

-- 4) Inventory table (per SME per item)
CREATE TABLE IF NOT EXISTS inventory (
                                         id SERIAL PRIMARY KEY,
                                         sme_id INTEGER NOT NULL REFERENCES sme(id) ON DELETE CASCADE,
    item_id INTEGER NOT NULL REFERENCES item(id) ON DELETE CASCADE,
    vendor_id INTEGER REFERENCES vendor(id),
    current_stock INTEGER NOT NULL DEFAULT 0,
    daily_consumption NUMERIC(10,2) NOT NULL DEFAULT 0.0,
    safety_stock INTEGER NOT NULL DEFAULT 0,
    lead_time_days INTEGER NOT NULL DEFAULT 0,
    last_updated TIMESTAMPTZ DEFAULT now(),
    UNIQUE (sme_id, item_id)
    );

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_inventory_sme ON inventory(sme_id);
CREATE INDEX IF NOT EXISTS idx_inventory_item ON inventory(item_id);
CREATE INDEX IF NOT EXISTS idx_item_code ON item(item_code);

-- --------------------------------------------------------------
-- Mock data inserts
-- --------------------------------------------------------------

-- SMEs
INSERT INTO sme (sme_code, name, address, phone)
VALUES
    ('SME_001', 'Anami Traders', 'Colombo, Sri Lanka', '071-000-0001'),
    ('SME_002', 'Kala Foods', 'Kandy, Sri Lanka', '071-000-0002')
    ON CONFLICT (sme_code) DO NOTHING;

-- Vendors
INSERT INTO vendor (vendor_name, vendor_type, contact_name, phone, address)
VALUES
    ('LocalDistCo', 'Local Distributor', 'Kumar Perera', '071-111-1111', 'Colombo'),
    ('ImportSupplier', 'International Importer', 'Meera Fernando', '072-222-2222', 'Colombo Port Area'),
    ('WholesaleMart', 'Wholesaler', 'Ajith Silva', '071-333-3333', 'Galle Road'),
    ('AltSupplier', 'Alt Manufacturer', 'Nimal Jay', '071-444-4444', 'Kurunegala')
    ON CONFLICT DO NOTHING;

-- Items
INSERT INTO item (item_code, item_name, description)
VALUES
    ('ITEM_ABC','Widget A','Primary widget A - used in assembly X'),
    ('ITEM_ALT_A','Widget A - Alt','Alternative widget with similar spec'),
    ('ITEM_XYZ','Widget X','Consumable X - cheap item'),
    ('ITEM_PARTY','Party Kit','Seasonal party kit')
    ON CONFLICT (item_code) DO NOTHING;

-- Inventory: link SMEs, items and vendors
-- We will upsert so rerunning script is safe
-- Get IDs with subselects
WITH
    s1 AS (SELECT id AS sme_id FROM sme WHERE sme_code='SME_001'),
    s2 AS (SELECT id AS sme_id FROM sme WHERE sme_code='SME_002'),
    i_abc AS (SELECT id AS item_id FROM item WHERE item_code='ITEM_ABC'),
    i_alt AS (SELECT id AS item_id FROM item WHERE item_code='ITEM_ALT_A'),
    i_xyz AS (SELECT id AS item_id FROM item WHERE item_code='ITEM_XYZ'),
    v_local AS (SELECT id AS vendor_id FROM vendor WHERE vendor_name='LocalDistCo'),
    v_import AS (SELECT id AS vendor_id FROM vendor WHERE vendor_name='ImportSupplier'),
    v_wh AS (SELECT id AS vendor_id FROM vendor WHERE vendor_name='WholesaleMart'),
    v_alt AS (SELECT id AS vendor_id FROM vendor WHERE vendor_name='AltSupplier')
INSERT INTO inventory (sme_id, item_id, vendor_id, current_stock, daily_consumption, safety_stock, lead_time_days)
SELECT s1.sme_id, i_abc.item_id, v_local.vendor_id, 10, 5.0, 5, 14  -- ITEM_ABC for SME_001 low stock, high lead time
FROM s1, i_abc, v_local
    ON CONFLICT (sme_id, item_id) DO UPDATE SET
    current_stock = EXCLUDED.current_stock,
                                         daily_consumption = EXCLUDED.daily_consumption,
                                         safety_stock = EXCLUDED.safety_stock,
                                         lead_time_days = EXCLUDED.lead_time_days,
                                         vendor_id = EXCLUDED.vendor_id,
                                         last_updated = now();

-- Another inventory rows
WITH
    s1 AS (SELECT id AS sme_id FROM sme WHERE sme_code='SME_001'),
    i_xyz AS (SELECT id AS item_id FROM item WHERE item_code='ITEM_XYZ'),
    v_wh AS (SELECT id AS vendor_id FROM vendor WHERE vendor_name='WholesaleMart')
INSERT INTO inventory (sme_id, item_id, vendor_id, current_stock, daily_consumption, safety_stock, lead_time_days)
SELECT s1.sme_id, i_xyz.item_id, v_wh.vendor_id, 500, 3.0, 50, 7
FROM s1, i_xyz, v_wh
    ON CONFLICT (sme_id, item_id) DO UPDATE SET
    current_stock = EXCLUDED.current_stock,
                                         daily_consumption = EXCLUDED.daily_consumption,
                                         safety_stock = EXCLUDED.safety_stock,
                                         lead_time_days = EXCLUDED.lead_time_days,
                                         vendor_id = EXCLUDED.vendor_id,
                                         last_updated = now();

-- SME_002 inventory
WITH
    s2 AS (SELECT id AS sme_id FROM sme WHERE sme_code='SME_002'),
    i_abc AS (SELECT id AS item_id FROM item WHERE item_code='ITEM_ABC'),
    v_import AS (SELECT id AS vendor_id FROM vendor WHERE vendor_name='ImportSupplier')
INSERT INTO inventory (sme_id, item_id, vendor_id, current_stock, daily_consumption, safety_stock, lead_time_days)
SELECT s2.sme_id, i_abc.item_id, v_import.vendor_id, 200, 2.0, 20, 21
FROM s2, i_abc, v_import
    ON CONFLICT (sme_id, item_id) DO UPDATE SET
    current_stock = EXCLUDED.current_stock,
                                         daily_consumption = EXCLUDED.daily_consumption,
                                         safety_stock = EXCLUDED.safety_stock,
                                         lead_time_days = EXCLUDED.lead_time_days,
                                         vendor_id = EXCLUDED.vendor_id,
                                         last_updated = now();

-- add one alternative supplier mapping in vendor table (not relational mapping to Neo4j)
-- (Neo4j will hold alternative material relationships)

-- Commit
COMMIT;

-- ==============================================================
-- Useful SQL queries (copy/paste)
-- ==============================================================

-- 1) Inventory status DTO (what Spring should expose to FastAPI)
-- This returns de-identified vendor_type (not contact/phone).
-- Replace 'ITEM_ABC' or bind param for app usage.

-- Example using item_code:
SELECT
    it.item_code AS item_id,
    it.item_name AS item_name,
    inv.current_stock,
    inv.daily_consumption,
    inv.lead_time_days,
    v.vendor_type AS vendor_type,
    inv.safety_stock,
    inv.last_updated
FROM inventory inv
         JOIN item it ON it.id = inv.item_id
         LEFT JOIN vendor v ON v.id = inv.vendor_id
         JOIN sme s ON s.id = inv.sme_id
WHERE it.item_code = 'ITEM_ABC' AND s.sme_code = 'SME_001';

-- 2) Compute days_of_cover and EOQ inline (EOQ in SQL using formula)
-- EOQ = sqrt((2 * D * S) / H) where D = daily_consumption * 365
-- We'll assume ordering_cost S = 50, holding cost H = 1.0
SELECT
    it.item_code AS item_id,
    inv.current_stock,
    inv.daily_consumption,
    (inv.current_stock / NULLIF(inv.daily_consumption,0))::numeric(10,2) AS days_of_cover,
    inv.lead_time_days,
    ROUND(SQRT((2 * inv.daily_consumption * 365 * 50.0) / 1.0))::int AS eoq_estimate
FROM inventory inv
         JOIN item it ON it.id = inv.item_id
WHERE it.item_code = 'ITEM_ABC' AND inv.sme_id = (SELECT id FROM sme WHERE sme_code='SME_001');

-- 3) List low cover items for a given SME (days_of_cover < lead_time_days)
SELECT
    s.sme_code,
    it.item_code,
    it.item_name,
    inv.current_stock,
    inv.daily_consumption,
    (inv.current_stock / NULLIF(inv.daily_consumption,0))::numeric(10,2) AS days_of_cover,
    inv.lead_time_days
FROM inventory inv
         JOIN item it ON it.id = inv.item_id
         JOIN sme s ON s.id = inv.sme_id
WHERE s.sme_code = 'SME_001'
  AND (inv.current_stock / NULLIF(inv.daily_consumption,0)) < inv.lead_time_days
ORDER BY days_of_cover ASC;

-- 4) Get all items + vendor_type for SME
SELECT
    it.item_code,
    it.item_name,
    COALESCE(v.vendor_type,'UNKNOWN') AS vendor_type,
    inv.current_stock,
    inv.daily_consumption,
    inv.lead_time_days
FROM inventory inv
         JOIN item it ON it.id = inv.item_id
         LEFT JOIN vendor v ON v.id = inv.vendor_id
         JOIN sme s ON s.id = inv.sme_id
WHERE s.sme_code = 'SME_001'
ORDER BY it.item_code;

-- 5) Upsert inventory row (example SQL to update or insert inventory)
-- Use for API that updates inventory from UI
INSERT INTO inventory (sme_id, item_id, vendor_id, current_stock, daily_consumption, safety_stock, lead_time_days, last_updated)
VALUES (
           (SELECT id FROM sme WHERE sme_code='SME_001'),
           (SELECT id FROM item WHERE item_code='ITEM_ABC'),
           (SELECT id FROM vendor WHERE vendor_name='LocalDistCo'),
           15, 5.0, 5, 14, now()
       )
    ON CONFLICT (sme_id, item_id) DO UPDATE SET
    current_stock = EXCLUDED.current_stock,
                                         daily_consumption = EXCLUDED.daily_consumption,
                                         safety_stock = EXCLUDED.safety_stock,
                                         lead_time_days = EXCLUDED.lead_time_days,
                                         vendor_id = EXCLUDED.vendor_id,
                                         last_updated = EXCLUDED.last_updated;

-- 6) Simple EOQ function (optional) - create a SQL function to compute EOQ
-- Installing a function so Spring can call if desired:
CREATE OR REPLACE FUNCTION compute_eoq(daily_consumption numeric, ordering_cost numeric DEFAULT 50.0, holding_cost numeric DEFAULT 1.0)
    RETURNS integer LANGUAGE plpgsql AS $$
DECLARE
demand_year numeric;
    eoq numeric;
BEGIN
    IF daily_consumption IS NULL OR daily_consumption <= 0 THEN
        RETURN 0;
END IF;
    demand_year := daily_consumption * 365.0;
    eoq := sqrt((2.0 * demand_year * ordering_cost) / holding_cost);
RETURN GREATEST(1, ROUND(eoq)::int);
END;
$$;

-- Test EOQ function
SELECT compute_eoq(5.0) AS eoq_for_5_per_day;

-- ==============================================================
-- EOF
-- ==============================================================
