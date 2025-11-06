-- =========================================================
-- Payroll DB schema (V1)
-- Atomic, idempotent, and production-friendly for SQLite.
-- =========================================================

BEGIN;

-- Core integrity is also enforced from Java via PRAGMA foreign_keys=ON
-- (PRAGMAs here are connection-local; included for clarity)
PRAGMA foreign_keys = ON;

-- === Tables ===

CREATE TABLE IF NOT EXISTS employees (
    id         INTEGER PRIMARY KEY,
    code       TEXT,
    name       TEXT NOT NULL,
    ei_exempt  INTEGER NOT NULL DEFAULT 0 CHECK (ei_exempt IN (0,1)),
    UNIQUE(code),
    UNIQUE(name COLLATE NOCASE)
    );

CREATE TABLE IF NOT EXISTS pay_periods (
    id          INTEGER PRIMARY KEY,
    start_date  TEXT NOT NULL,          -- ISO YYYY-MM-DD
    end_date    TEXT NOT NULL,          -- ISO YYYY-MM-DD
    pay_date    TEXT NOT NULL,          -- ISO YYYY-MM-DD
    UNIQUE(start_date, end_date),
    CHECK (date(start_date) <= date(end_date)),
    CHECK (date(pay_date)   >= date(end_date))
    );

CREATE TABLE IF NOT EXISTS paystubs (
    id               INTEGER PRIMARY KEY,
    employee_id      INTEGER NOT NULL REFERENCES employees(id) ON DELETE RESTRICT,
    period_id        INTEGER NOT NULL REFERENCES pay_periods(id) ON DELETE RESTRICT,
    vac_rate         NUMERIC NOT NULL CHECK (vac_rate >= 0 AND vac_rate <= 1),

    gross            NUMERIC NOT NULL CHECK (gross    >= 0),
    cpp              NUMERIC NOT NULL CHECK (cpp      >= 0),
    ei               NUMERIC NOT NULL CHECK (ei       >= 0),
    fed_tax          NUMERIC NOT NULL CHECK (fed_tax  >= 0),
    prov_tax         NUMERIC NOT NULL CHECK (prov_tax >= 0),
    net              NUMERIC NOT NULL CHECK (net      >= 0),

    tax_config_key   TEXT NOT NULL, -- e.g., "2025-01-01" or "2025-07-01"
    created_at       TEXT NOT NULL DEFAULT (datetime('now')),

    UNIQUE(employee_id, period_id)
    );

CREATE TABLE IF NOT EXISTS earning_slices (
    id          INTEGER PRIMARY KEY,
    paystub_id  INTEGER NOT NULL REFERENCES paystubs(id) ON DELETE CASCADE,
    etype       TEXT NOT NULL CHECK (etype IN ('REGULAR','OVERTIME','STAT_HOLIDAY')),
    hours       NUMERIC NOT NULL CHECK (hours  >= 0),
    rate        NUMERIC NOT NULL CHECK (rate   >= 0),

    -- Always correct by construction; prevents app/rounding bugs
    subtotal    NUMERIC GENERATED ALWAYS AS (hours * rate) STORED
    );

-- === Indexes (query helpers) ===
CREATE INDEX IF NOT EXISTS idx_ps_employee ON paystubs(employee_id);
CREATE INDEX IF NOT EXISTS idx_ps_period   ON paystubs(period_id);
CREATE INDEX IF NOT EXISTS idx_pp_paydate  ON pay_periods(pay_date);
CREATE INDEX IF NOT EXISTS idx_es_stub     ON earning_slices(paystub_id);

-- === Views ===

-- Export-friendly flattened paystub rows with JSON array of earning slices
CREATE VIEW IF NOT EXISTS paystubs_flat AS
SELECT
    ps.id,
    e.name            AS employee_name,
    e.ei_exempt,
    pp.start_date, pp.end_date, pp.pay_date,
    ps.vac_rate, ps.gross, ps.cpp, ps.ei, ps.fed_tax, ps.prov_tax, ps.net, ps.tax_config_key,
    json_group_array(
            json_object('etype', s.etype, 'hours', s.hours, 'rate', s.rate, 'subtotal', s.subtotal)
    ) AS slices_json
FROM paystubs ps
         JOIN employees   e  ON e.id  = ps.employee_id
         JOIN pay_periods pp ON pp.id = ps.period_id
         LEFT JOIN earning_slices s   ON s.paystub_id = ps.id
GROUP BY ps.id;

-- YTD rollups per employee per paystub (inclusive)
CREATE VIEW IF NOT EXISTS paystubs_with_ytd AS
SELECT
    ps.id,
    ps.employee_id,
    ps.period_id,
    ps.vac_rate, ps.gross, ps.cpp, ps.ei, ps.fed_tax, ps.prov_tax, ps.net,
    ps.tax_config_key, ps.created_at,
    SUM(ps.gross)                 OVER (PARTITION BY ps.employee_id ORDER BY pp.pay_date) AS ytd_gross,
    SUM(ps.cpp)                   OVER (PARTITION BY ps.employee_id ORDER BY pp.pay_date) AS ytd_cpp,
    SUM(ps.ei)                    OVER (PARTITION BY ps.employee_id ORDER BY pp.pay_date) AS ytd_ei,
    SUM(ps.fed_tax + ps.prov_tax) OVER (PARTITION BY ps.employee_id ORDER BY pp.pay_date) AS ytd_income_tax
FROM paystubs ps
         JOIN pay_periods pp ON pp.id = ps.period_id;

COMMIT;