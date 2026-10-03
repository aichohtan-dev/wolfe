-- V43: Database-level integrity for money, coupons, retailer commissions and margins.
-- Keep these invariants enforced even if a future write path bypasses application validation.

ALTER TABLE orders
    ADD CONSTRAINT ck_orders_total_nonnegative CHECK (total >= 0),
    ADD CONSTRAINT ck_orders_subtotal_nonnegative CHECK (subtotal >= 0),
    ADD CONSTRAINT ck_orders_shipping_nonnegative CHECK (shipping_fee >= 0),
    ADD CONSTRAINT ck_orders_discount_nonnegative CHECK (discount_amount >= 0);

ALTER TABLE order_items
    ADD CONSTRAINT ck_order_items_unit_price_nonnegative CHECK (unit_price >= 0),
    ADD CONSTRAINT ck_order_items_bundle_discount_nonnegative CHECK (bundle_discount >= 0),
    ADD CONSTRAINT ck_order_items_coupon_discount_nonnegative CHECK (coupon_discount >= 0),
    ADD CONSTRAINT ck_order_items_line_net_nonnegative CHECK (line_net_amount >= 0);

ALTER TABLE coupons
    ADD CONSTRAINT ck_coupons_discount_type CHECK (discount_type IN ('PERCENT', 'FIXED')),
    ADD CONSTRAINT ck_coupons_value_positive CHECK (value > 0),
    ADD CONSTRAINT ck_coupons_minimum_nonnegative CHECK (minimum_subtotal >= 0),
    ADD CONSTRAINT ck_coupons_maximum_nonnegative CHECK (maximum_discount >= 0),
    ADD CONSTRAINT ck_coupons_percent_cap CHECK (discount_type <> 'PERCENT' OR value <= 100),
    ADD CONSTRAINT ck_coupons_usage_limit_positive CHECK (usage_limit IS NULL OR usage_limit >= 1),
    ADD CONSTRAINT ck_coupons_customer_usage_limit_positive CHECK (per_customer_usage_limit IS NULL OR per_customer_usage_limit >= 1),
    ADD CONSTRAINT ck_coupons_used_count_nonnegative CHECK (used_count >= 0),
    ADD CONSTRAINT ck_coupons_dates_ordered CHECK (starts_at IS NULL OR expires_at IS NULL OR expires_at > starts_at);

ALTER TABLE retailers
    ADD CONSTRAINT ck_retailers_delivery_radius_nonnegative CHECK (delivery_radius_km >= 0),
    ADD CONSTRAINT ck_retailers_rating_range CHECK (rating >= 0 AND rating <= 5),
    ADD CONSTRAINT ck_retailers_commission_range CHECK (commission_rate >= 0 AND commission_rate <= 100);

ALTER TABLE retailer_service_areas
    ADD CONSTRAINT ck_service_area_eta_positive CHECK (delivery_eta_hours > 0);

ALTER TABLE retailer_margin_rules
    ADD CONSTRAINT ck_margin_rule_type CHECK (margin_type IN ('PERCENTAGE', 'FIXED')),
    ADD CONSTRAINT ck_margin_rule_value_positive CHECK (margin_value > 0),
    ADD CONSTRAINT ck_margin_rule_percentage_cap CHECK (margin_type <> 'PERCENTAGE' OR margin_value <= 100);

ALTER TABLE retailer_settlements
    ADD CONSTRAINT ck_settlement_gross_nonnegative CHECK (gross_amount >= 0),
    ADD CONSTRAINT ck_settlement_margin_nonnegative CHECK (wolfe_margin_amount >= 0),
    ADD CONSTRAINT ck_settlement_payable_nonnegative CHECK (retailer_payable_amount >= 0),
    ADD CONSTRAINT ck_settlement_components_reconcile CHECK (wolfe_margin_amount + retailer_payable_amount = gross_amount),
    ADD CONSTRAINT ck_settlement_cash_expected_nonnegative CHECK (cash_expected_amount >= 0),
    ADD CONSTRAINT ck_settlement_cash_collected_nonnegative CHECK (cash_collected_amount >= 0),
    ADD CONSTRAINT ck_settlement_cash_deposited_nonnegative CHECK (cash_deposited_amount >= 0);

ALTER TABLE retailer_settlement_adjustments
    ADD CONSTRAINT ck_settlement_adjustment_amount_nonnegative CHECK (amount >= 0);
