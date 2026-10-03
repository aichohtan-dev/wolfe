import type { CartItem } from '../types/cart';

export function cartUnitPrice(p: any, item: CartItem, configs: Record<string, any>): number {
    if (item.configurationToken) {
        const cfg = configs[item.configurationToken];
        if (cfg && Number.isFinite(Number(cfg.basePrice))) {
            return (Number(cfg.basePrice) + Number(cfg.addonPrice || 0)) / 100;
        }
        return Number(p?.price || 0);
    }
    if (item.variantPrice && Number.isFinite(Number(item.variantPrice))) {
        return Number(item.variantPrice);
    }
    return Number(p?.price || 0);
}
