-- Record the actual payable amount at settlement time so return adjustments cannot be
-- applied to the ledger while the payout endpoint still records the original gross payable.
ALTER TABLE retailer_settlements
    ADD COLUMN IF NOT EXISTS settled_amount BIGINT NOT NULL DEFAULT 0;

-- Preserve historical settled rows with the amount that the current settlement model
-- would have paid after recorded pre-payout adjustments.
UPDATE retailer_settlements
SET settled_amount = GREATEST(retailer_payable_amount - adjustment_amount, 0)
WHERE status = 'SETTLED' AND settled_amount = 0;
