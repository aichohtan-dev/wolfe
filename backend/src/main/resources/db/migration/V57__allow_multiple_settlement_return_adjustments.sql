-- Multiple legitimate partial returns for one order/settlement each create their own ledger event.
-- The original V34 uniqueness constraint allowed only one RETURN adjustment and conflicted with
-- the current settlement model. Keep V34 immutable and roll the constraint forward.
DROP INDEX IF EXISTS uq_settlement_return_adjustment;
