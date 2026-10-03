export type CartItem = {
    id: string;
    qty: number;
    configurationToken?: string;
    bundleId?: number;
    bundleSlug?: string;
    bundleUnits?: number;
    bundleBaseQuantity?: number;
    variantId?: number;
    variantSku?: string;
    variantTitle?: string;
    variantColor?: string;
    variantMaterial?: string;
    variantSize?: string;
    variantFinish?: string;
    variantImage?: string;
    variantPrice?: number;
};
