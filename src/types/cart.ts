export type CartItem = {
    id: string;
    qty: number;
    configurationToken?: string;
    bundleId?: number;
    bundleSlug?: string;
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
