const API_BASE = (import.meta.env.VITE_API_URL || '/api/v1').replace(/\/$/, '');
let refreshPromise: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
    const refreshToken = localStorage.getItem('wolfe_refresh_token');
    if (!refreshToken) return null;
    if (!refreshPromise) {
        refreshPromise = (async () => {
            try {
                const res = await fetch(`${API_BASE}/customers/refresh`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ refreshToken })
                });
                if (!res.ok) throw new Error('refresh failed');
                const data = await res.json();
                localStorage.setItem('wolfe_access_token', data.accessToken);
                localStorage.setItem('wolfe_refresh_token', data.refreshToken);
                return data.accessToken as string;
            } catch {
                localStorage.removeItem('wolfe_access_token');
                localStorage.removeItem('wolfe_refresh_token');
                localStorage.removeItem('wolfe_user');
                return null;
            } finally {
                refreshPromise = null;
            }
        })();
    }
    return refreshPromise;
}

async function request<T>(path: string, init: RequestInit = {}, retry = true): Promise<T> {
    const token = localStorage.getItem('wolfe_access_token');
    const headers = new Headers(init.headers);
    if (!(init.body instanceof FormData)) {
        headers.set('Content-Type', 'application/json');
    }
    if (token) {
        headers.set('Authorization', `Bearer ${token}`);
    }
    const res = await fetch(`${API_BASE}${path}`, { ...init, headers });
    if (res.status === 401 && retry && !path.includes('/customers/refresh') && !path.includes('/customers/login')) {
        const next = await refreshAccessToken();
        if (next) return request<T>(path, init, false);
    }
    if (!res.ok) {
        let body: any = {};
        try {
            body = await res.json();
        } catch {}
        throw new Error(body.error || `Request failed (${res.status})`);
    }
    if (res.status === 204) return undefined as T;
    return res.json();
}

export type Customer = {
    id: number;
    name: string;
    email: string;
    phone?: string;
};

export type ProductVariant = {
    id: number;
    productId: number;
    optionName?: string;
    optionValue?: string;
    title?: string;
    sku: string;
    color?: string;
    material?: string;
    size?: string;
    finish?: string;
    dimensions?: string;
    price: number;
    priceOverride?: number;
    stockQuantity: number;
    imageUrl?: string;
    attributesJson?: string;
    active: boolean;
};

export type Product = {
    id: number | string;
    slug: string;
    name: string;
    price: number;
    category: string;
    subcategory?: string;
    brandId?: number;
    brandName?: string;
    finish: string;
    material: string;
    color: string;
    style: string;
    dimensions?: string;
    modelNumber?: string;
    description: string;
    imageUrl?: string;
    mediaUrls?: string;
    attributesJson?: string;
    active: boolean;
    sortOrder: number;
    featured: boolean;
    variants?: ProductVariant[];
};

export type Brand = {
    id: number;
    name: string;
    slug: string;
    logoUrl?: string;
    description?: string;
    website?: string;
    active: boolean;
    sortOrder: number;
};

export type Subcategory = {
    id: number;
    categoryId?: number;
    categoryName: string;
    name: string;
    slug: string;
    description?: string;
    active: boolean;
    sortOrder: number;
};

export type PagedResponse<T> = {
    content: T[];
    page: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    hasNext: boolean;
    isFirst: boolean;
    isLast: boolean;
};

export type ProductQueryParams = {
    category?: string;
    subcategory?: string;
    brand?: string;
    brandId?: number;
    q?: string;
    finish?: string;
    material?: string;
    color?: string;
    style?: string;
    minPrice?: number;
    maxPrice?: number;
    featured?: boolean;
    sort?: string;
    page?: number;
    pageSize?: number;
};

export const api = {
    me: () => request<Customer>('/customers/me'),
    updateProfile: (body: { name: string; phone: string }) =>
        request<Customer>('/customers/me', { method: 'PUT', body: JSON.stringify(body) }),

    products: (
        category?: string,
        q?: string,
        finish?: string,
        featured?: boolean,
        material?: string,
        color?: string,
        style?: string,
        sort?: string,
        subcategory?: string,
        brand?: string,
        minPrice?: number,
        maxPrice?: number
    ) => {
        const p = new URLSearchParams();
        if (category && category !== 'All') p.set('category', category);
        if (subcategory && subcategory !== 'All') p.set('subcategory', subcategory);
        if (brand && brand !== 'All') p.set('brand', brand);
        if (q && q.trim()) p.set('q', q.trim());
        if (finish && finish !== 'All') p.set('finish', finish);
        if (material && material !== 'All') p.set('material', material);
        if (color && color !== 'All') p.set('color', color);
        if (style && style !== 'All') p.set('style', style);
        if (featured) p.set('featured', 'true');
        if (minPrice !== undefined && minPrice > 0) p.set('minPrice', String(minPrice));
        if (maxPrice !== undefined && maxPrice > 0) p.set('maxPrice', String(maxPrice));
        if (sort) p.set('sort', sort);
        return request<Product[]>(`/products${p.toString() ? `?${p.toString()}` : ''}`);
    },

    pagedProducts: (params: ProductQueryParams) => {
        const p = new URLSearchParams();
        if (params.category && params.category !== 'All') p.set('category', params.category);
        if (params.subcategory && params.subcategory !== 'All') p.set('subcategory', params.subcategory);
        if (params.brand && params.brand !== 'All') p.set('brand', params.brand);
        if (params.brandId) p.set('brandId', String(params.brandId));
        if (params.q && params.q.trim()) p.set('q', params.q.trim());
        if (params.finish && params.finish !== 'All') p.set('finish', params.finish);
        if (params.material && params.material !== 'All') p.set('material', params.material);
        if (params.color && params.color !== 'All') p.set('color', params.color);
        if (params.style && params.style !== 'All') p.set('style', params.style);
        if (params.minPrice !== undefined && params.minPrice > 0) p.set('minPrice', String(params.minPrice));
        if (params.maxPrice !== undefined && params.maxPrice > 0) p.set('maxPrice', String(params.maxPrice));
        if (params.featured) p.set('featured', 'true');
        if (params.sort) p.set('sort', params.sort);
        if (params.page !== undefined) p.set('page', String(params.page));
        if (params.pageSize !== undefined) p.set('pageSize', String(params.pageSize));
        return request<PagedResponse<Product>>(`/products/paged?${p.toString()}`);
    },

    brands: () => request<Brand[]>('/brands'),
    brand: (slug: string) => request<Brand>(`/brands/${encodeURIComponent(slug)}`),
    subcategories: (category?: string) =>
        request<Subcategory[]>(`/subcategories${category && category !== 'All' ? `?category=${encodeURIComponent(category)}` : ''}`),

    productFilters: () => request<any>('/products/filters'),
    productSuggestions: (q: string) => request<any[]>(`/products/suggestions?q=${encodeURIComponent(q)}`),
    productVariants: (slug: string) => request<ProductVariant[]>(`/products/${encodeURIComponent(slug)}/variants`),
    relatedProducts: (slug: string) => request<Product[]>(`/products/${encodeURIComponent(slug)}/related`),
    product: (slug: string) => request<Product>(`/products/${encodeURIComponent(slug)}`),
    visualContent: (placement = 'HERO') => request<any[]>(`/visual-content?placement=${encodeURIComponent(placement)}`),
    accessories: (slug: string) => request<any[]>(`/products/${encodeURIComponent(slug)}/accessories`),
    experience: {
        spin: (slug: string) => request<any[]>(`/experience/products/${encodeURIComponent(slug)}/spin`),
        visualAsset: (slug: string) => request<any>(`/experience/products/${encodeURIComponent(slug)}/visual-asset`),
        hotspots: (visualId: number) => request<any[]>(`/experience/visual/${visualId}/hotspots`),
        saveConfiguration: (body: any) => request<any>('/experience/configurations', { method: 'POST', body: JSON.stringify(body) }),
        getConfiguration: (token: string) => request<any>(`/experience/configurations/${encodeURIComponent(token)}`),
        recent: (customerId: number) => request<any[]>(`/experience/customers/${customerId}/recent`),
        touchRecent: (customerId: number, productId: number) => request<any>(`/experience/customers/${customerId}/recent/${productId}`, { method: 'POST' }),
        subscribeStock: (customerId: number, productId: number) => request<any>(`/experience/customers/${customerId}/back-in-stock/${productId}`, { method: 'POST' }),
        unsubscribeStock: (customerId: number, productId: number) => request<void>(`/experience/customers/${customerId}/back-in-stock/${productId}`, { method: 'DELETE' }),
        touchCartRecovery: (customerId: number) => request<any>(`/experience/customers/${customerId}/cart-recovery/touch`, { method: 'POST' })
    },
    login: (email: string, password: string) => request<any>('/customers/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
    register: (name: string, email: string, password: string) => request<any>('/customers/register', { method: 'POST', body: JSON.stringify({ name, email, password }) }),
    logout: async () => {
        const refreshToken = localStorage.getItem('wolfe_refresh_token');
        try {
            await request<void>('/customers/logout', { method: 'POST', body: JSON.stringify({ refreshToken }) }, false);
        } finally {
            localStorage.removeItem('wolfe_access_token');
            localStorage.removeItem('wolfe_refresh_token');
        }
    },
    bundles: {
        list: () => request<any[]>('/bundles'),
        get: (slug: string) => request<any>(`/bundles/${encodeURIComponent(slug)}`)
    },
    cart: {
        get: (id: number) => request<any[]>(`/cart/${id}`),
        put: (id: number, slug: string, quantity: number) => request<any>(`/cart/${id}/${encodeURIComponent(slug)}?quantity=${quantity}`, { method: 'PUT' }),
        remove: (id: number, slug: string) => request<void>(`/cart/${id}/${encodeURIComponent(slug)}`, { method: 'DELETE' })
    },
    wishlist: {
        get: (id: number) => request<string[]>(`/wishlist/${id}/slugs`),
        details: (id: number) => request<Product[]>(`/wishlist/${id}`),
        clear: (id: number) => request<void>(`/wishlist/${id}`, { method: 'DELETE' }),
        add: (id: number, slug: string) => request<any[]>(`/wishlist/${id}/${encodeURIComponent(slug)}`, { method: 'PUT' }),
        remove: (id: number, slug: string) => request<void>(`/wishlist/${id}/${encodeURIComponent(slug)}`, { method: 'DELETE' })
    },
    shippingQuote: (subtotal: number, shippingMethod: string = 'STANDARD', couponCode?: string) =>
        request(`/orders/shipping-quote?subtotal=${Math.round(subtotal * 100)}&shippingMethod=${encodeURIComponent(shippingMethod)}${couponCode ? `&couponCode=${encodeURIComponent(couponCode)}` : ''}`),
    couponQuote: (subtotal: number, couponCode: string) =>
        request(`/orders/coupon-quote?subtotal=${Math.round(subtotal * 100)}&couponCode=${encodeURIComponent(couponCode)}`),
    order: (body: {
        customerId: number;
        customerName: string;
        customerEmail: string;
        phone: string;
        address: string;
        city: string;
        pincode: string;
        paymentMethod: 'COD';
        shippingMethod: string;
        couponCode?: string;
        items: {
            slug: string;
            quantity: number;
            configurationToken?: string;
            bundleId?: number;
            variantId?: number;
            variantSku?: string;
        }[];
    }) => request<any>('/orders', { method: 'POST', body: JSON.stringify(body) }),
    orderDetail: (id: string) => request<any>(`/orders/${encodeURIComponent(id)}`),
    orderHistory: (id: string) => request<any[]>(`/orders/${encodeURIComponent(id)}/history`),
    customerOrders: (id: number) => request<any[]>(`/orders/customer/${id}`),
    cancelOrder: (id: string) => request<any>(`/orders/${encodeURIComponent(id)}/cancel`, { method: 'POST' }),
    addresses: {
        get: (id: number) => request<any[]>(`/customers/${id}/addresses`),
        create: (id: number, body: any) => request<any>(`/customers/${id}/addresses`, { method: 'POST', body: JSON.stringify(body) }),
        update: (id: number, addressId: number, body: any) => request<any>(`/customers/${id}/addresses/${addressId}`, { method: 'PUT', body: JSON.stringify(body) }),
        remove: (id: number, addressId: number) => request<void>(`/customers/${id}/addresses/${addressId}`, { method: 'DELETE' })
    },
    returns: {
        get: (id: number) => request<any[]>(`/customers/${id}/returns`),
        create: (id: number, orderId: string, reason: string) => request<any>(`/customers/${id}/returns/${encodeURIComponent(orderId)}`, { method: 'POST', body: JSON.stringify({ reason }) })
    },
    notifications: {
        get: (id: number) => request<any>(`/customers/${id}/notifications`),
        read: (id: number, nid: number) => request<any>(`/customers/${id}/notifications/${nid}/read`, { method: 'POST' }),
        readAll: (id: number) => request<any>(`/customers/${id}/notifications/read-all`, { method: 'POST' })
    },
    reviews: {
        get: (slug: string) => request<any>(`/reviews/product/${encodeURIComponent(slug)}`),
        create: (slug: string, body: { rating: number; review: string }) => request<any>(`/reviews/product/${encodeURIComponent(slug)}`, { method: 'POST', body: JSON.stringify(body) })
    },
    quotes: {
        get: (id: number) => request<any[]>(`/quotes/customers/${id}`),
        create: (id: number, body: any) => request<any>(`/quotes/customers/${id}`, { method: 'POST', body: JSON.stringify(body) })
    },
    customDesign: {
        get: (id: number) => request<any[]>(`/custom-design/customers/${id}`),
        create: (id: number, body: any) => request<any>(`/custom-design/customers/${id}`, { method: 'POST', body: JSON.stringify(body) })
    },
    admin: {
        brands: () => request<Brand[]>('/admin/brands'),
        createBrand: (body: any) => request<Brand>('/admin/brands', { method: 'POST', body: JSON.stringify(body) }),
        updateBrand: (id: number, body: any) => request<Brand>(`/admin/brands/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        deleteBrand: (id: number) => request<void>(`/admin/brands/${id}`, { method: 'DELETE' }),

        subcategories: () => request<Subcategory[]>('/admin/subcategories'),
        createSubcategory: (body: any) => request<Subcategory>('/admin/subcategories', { method: 'POST', body: JSON.stringify(body) }),
        updateSubcategory: (id: number, body: any) => request<Subcategory>(`/admin/subcategories/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        deleteSubcategory: (id: number) => request<void>(`/admin/subcategories/${id}`, { method: 'DELETE' }),

        pdfImports: {
            upload: (formData: FormData) => request<any>('/admin/pdf-imports/upload', { method: 'POST', body: formData }),
            list: () => request<any[]>('/admin/pdf-imports'),
            get: (jobId: number) => request<any>(`/admin/pdf-imports/${jobId}`),
            items: (jobId: number) => request<any[]>(`/admin/pdf-imports/${jobId}/items`),
            updateItem: (itemId: number, body: any) => request<any>(`/admin/pdf-imports/items/${itemId}`, { method: 'PUT', body: JSON.stringify(body) }),
            approveItem: (itemId: number) => request<any>(`/admin/pdf-imports/items/${itemId}/approve`, { method: 'POST' }),
            rejectItem: (itemId: number) => request<any>(`/admin/pdf-imports/items/${itemId}/reject`, { method: 'POST' }),
            approveAll: (jobId: number) => request<any[]>(`/admin/pdf-imports/${jobId}/approve-all`, { method: 'POST' })
        },

        bundles: () => request<any[]>('/admin/bundles'),
        createBundle: (body: any) => request<any>('/admin/bundles', { method: 'POST', body: JSON.stringify(body) }),
        updateBundle: (id: number, body: any) => request<any>(`/admin/bundles/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        deleteBundle: (id: number) => request<void>(`/admin/bundles/${id}`, { method: 'DELETE' }),
        coupons: () => request<any[]>('/admin/coupons'),
        createCoupon: (body: any) => request<any>('/admin/coupons', { method: 'POST', body: JSON.stringify(body) }),
        updateCoupon: (id: number, body: any) => request<any>(`/admin/coupons/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        deleteCoupon: (id: number) => request<void>(`/admin/coupons/${id}`, { method: 'DELETE' }),
        dashboard: () => request<any>('/admin/dashboard'),
        products: () => request<any[]>('/admin/products'),
        bulkProducts: (body: any) => request<any[]>('/admin/products/bulk', { method: 'POST', body: JSON.stringify(body) }),
        categories: () => request<any[]>('/admin/categories'),
        createCategory: (body: any) => request<any>('/admin/categories', { method: 'POST', body: JSON.stringify(body) }),
        updateCategory: (id: number, body: any) => request<any>(`/admin/categories/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        deleteCategory: (id: number) => request<void>(`/admin/categories/${id}`, { method: 'DELETE' }),
        collections: () => request<any[]>('/admin/collections'),
        createCollection: (body: any) => request<any>('/admin/collections', { method: 'POST', body: JSON.stringify(body) }),
        updateCollection: (id: number, body: any) => request<any>(`/admin/collections/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        deleteCollection: (id: number) => request<void>(`/admin/collections/${id}`, { method: 'DELETE' }),
        media: (id: number) => request<any[]>(`/admin/products/${id}/media`),
        createMedia: (id: number, body: any) => request<any>(`/admin/products/${id}/media`, { method: 'POST', body: JSON.stringify(body) }),
        updateMedia: (id: number, mediaId: number, body: any) => request<any>(`/admin/products/${id}/media/${mediaId}`, { method: 'PUT', body: JSON.stringify(body) }),
        deleteMedia: (id: number, mediaId: number) => request<void>(`/admin/products/${id}/media/${mediaId}`, { method: 'DELETE' }),
        createProduct: (body: any) => request<Product>('/admin/products', { method: 'POST', body: JSON.stringify(body) }),
        updateProduct: (id: number, body: any) => request<any>(`/admin/products/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        variants: (id: number) => request<any[]>(`/admin/products/${id}/variants`),
        createVariant: (id: number, body: any) => request<any>(`/admin/products/${id}/variants`, { method: 'POST', body: JSON.stringify(body) }),
        updateVariant: (id: number, variantId: number, body: any) => request<any>(`/admin/products/${id}/variants/${variantId}`, { method: 'PUT', body: JSON.stringify(body) }),
        deleteVariant: (id: number, variantId: number) => request<void>(`/admin/products/${id}/variants/${variantId}`, { method: 'DELETE' }),
        accessories: (id: number) => request<any[]>(`/admin/products/${id}/accessories`),
        createAccessory: (id: number, body: any) => request<any>(`/admin/products/${id}/accessories`, { method: 'POST', body: JSON.stringify(body) }),
        updateAccessory: (id: number, aid: number, body: any) => request<any>(`/admin/products/${id}/accessories/${aid}`, { method: 'PUT', body: JSON.stringify(body) }),
        deleteAccessory: (id: number, aid: number) => request<void>(`/admin/products/${id}/accessories/${aid}`, { method: 'DELETE' }),
        deleteProduct: (id: number) => request<void>(`/admin/products/${id}`, { method: 'DELETE' }),
        inventory: () => request<any[]>('/admin/inventory'),
        stockAlerts: () => request<any[]>('/admin/stock-alerts'),
        updateStock: (productId: number, quantity: number) => request<any>(`/admin/inventory/${productId}`, { method: 'PUT', body: JSON.stringify({ quantity }) }),
        orders: () => request<any[]>('/admin/orders'),
        orderHistory: (id: string) => request<any[]>(`/admin/order-history/${encodeURIComponent(id)}`),
        customers: () => request<any[]>('/admin/customers'),
        updateOrderStatus: (id: string, status: string) => request<any>(`/admin/orders/${encodeURIComponent(id)}/status`, { method: 'PUT', body: JSON.stringify({ status }) }),
        returns: () => request<any[]>('/admin/returns'),
        updateReturn: (id: number, body: any) => request<any>(`/admin/returns/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        reviews: () => request<any[]>('/admin/reviews'),
        reviewStatus: (id: number, status: string) => request<any>(`/admin/reviews/${id}/status`, { method: 'PUT', body: JSON.stringify({ status }) }),
        quotes: () => request<any[]>('/admin/quotes'),
        quoteStatus: (id: number, status: string) => request<any>(`/admin/quotes/${id}/status`, { method: 'PUT', body: JSON.stringify({ status }) }),
        customDesign: () => request<any[]>('/admin/custom-design'),
        customDesignStatus: (id: number, status: string) => request<any>(`/admin/custom-design/${id}/status`, { method: 'PUT', body: JSON.stringify({ status }) }),
        visualContent: () => request<any[]>('/admin/visual-content'),
        createVisualContent: (body: any) => request<any>('/admin/visual-content', { method: 'POST', body: JSON.stringify(body) }),
        updateVisualContent: (id: number, body: any) => request<any>(`/admin/visual-content/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
        deleteVisualContent: (id: number) => request<void>(`/admin/visual-content/${id}`, { method: 'DELETE' }),
        spin: (id: number, body: any) => request<any>(`/admin/experience/products/${id}/spin`, { method: 'POST', body: JSON.stringify(body) }),
        deleteSpin: (id: number) => request<void>(`/admin/experience/spin/${id}`, { method: 'DELETE' }),
        visualAsset: (id: number, body: any) => request<any>(`/admin/experience/products/${id}/visual-asset`, { method: 'PUT', body: JSON.stringify(body) }),
        hotspot: (id: number, body: any) => request<any>(`/admin/experience/visual/${id}/hotspots`, { method: 'POST', body: JSON.stringify(body) }),
        deleteHotspot: (id: number) => request<void>(`/admin/experience/hotspots/${id}`, { method: 'DELETE' }),
        stockSubscriptions: () => request<any[]>('/admin/experience/back-in-stock'),
        cartRecovery: () => request<any[]>('/admin/experience/cart-recovery')
    }
};
