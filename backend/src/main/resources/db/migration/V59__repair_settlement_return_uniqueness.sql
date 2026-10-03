-- V57 intentionally introduced a forward drop of the historical one-return constraint,
-- but older deployments may have re-created the index from the historical migration chain.
-- Re-assert the intended state as a final forward-only migration: multiple RETURN ledger
-- events are valid for multiple partial returns on one settlement.
DROP INDEX IF EXISTS uq_settlement_return_adjustment;
