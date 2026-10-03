import React, { useState, useEffect } from 'react';
import { api } from '../../api';
import { money } from '../../utils/format';
import ProductModal from './ProductModal';

export default function Admin() {
  const [tab, setTab] = useState<
    | 'dashboard'
    | 'products'
    | 'catalog'
    | 'brands'
    | 'subcategories'
    | 'pdf-import'
    | 'retailers'
    | 'allocations'
    | 'settlements'
    | 'bundles'
    | 'media'
    | 'inventory'
    | 'orders'
    | 'customers'
    | 'reviews'
    | 'quotes'
    | 'custom'
    | 'consultations'
    | 'visual'
    | 'experience'
    | 'commerce'
  >('dashboard');

  const [dashboard, setDashboard] = useState<any>(null);
  const [adminProducts, setAdminProducts] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [brands, setBrands] = useState<any[]>([]);
  const [subcategories, setSubcategories] = useState<any[]>([]);
  const [collections, setCollections] = useState<any[]>([]);
  const [stock, setStock] = useState<any[]>([]);
  const [adminOrders, setAdminOrders] = useState<any[]>([]);
  const [adminCustomers, setAdminCustomers] = useState<any[]>([]);
  const [adminReviews, setAdminReviews] = useState<any[]>([]);
  const [adminQuotes, setAdminQuotes] = useState<any[]>([]);
  const [adminCustom, setAdminCustom] = useState<any[]>([]);
  const [adminConsultations, setAdminConsultations] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [adminBundles, setAdminBundles] = useState<any[]>([]);
  const [selected, setSelected] = useState<number[]>([]);
  const [visualItems, setVisualItems] = useState<any[]>([]);
  const [visualDraft, setVisualDraft] = useState<any>({
    placement: 'HERO',
    title: '',
    subtitle: '',
    mediaType: 'VIDEO',
    mediaUrl: '',
    posterUrl: '',
    linkUrl: '',
    active: true,
    sortOrder: 0,
  });

  const [experienceProductId, setExperienceProductId] = useState<number | null>(null);
  const [experienceAsset, setExperienceAsset] = useState<any | null>(null);
  const [spinItems, setSpinItems] = useState<any[]>([]);
  const [spinDraft, setSpinDraft] = useState<any>({ imageUrl: '', sortOrder: 0 });
  const [editingSpinId, setEditingSpinId] = useState<number | null>(null);
  const [experienceVisualId, setExperienceVisualId] = useState<number | null>(null);
  const [hotspotItems, setHotspotItems] = useState<any[]>([]);
  const [hotspotDraft, setHotspotDraft] = useState<any>({ label: '', targetSlug: '', x: 50, y: 50, active: true });
  const [editingHotspotId, setEditingHotspotId] = useState<number | null>(null);

  const [editing, setEditing] = useState<any | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [collectionDraft, setCollectionDraft] = useState<any>({
    name: '',
    description: '',
    active: true,
    productIds: [],
  });
  const [categoryDraft, setCategoryDraft] = useState('');
  const [brandDraft, setBrandDraft] = useState<any>({
    name: '',
    description: '',
    countryOfOrigin: 'India',
    websiteUrl: '',
    logoUrl: '',
  });
  const [subcategoryDraft, setSubcategoryDraft] = useState<any>({
    name: '',
    categoryId: 1,
    description: '',
  });

  // PDF Import state
  const [pdfJobs, setPdfJobs] = useState<any[]>([]);
  const [selectedJob, setSelectedJob] = useState<any>(null);
  const [pdfUploading, setPdfUploading] = useState(false);
  const [pdfFile, setPdfFile] = useState<File | null>(null);
  const [pdfBrandId, setPdfBrandId] = useState<number | undefined>(undefined);
  const [pdfCategoryId, setPdfCategoryId] = useState<number | undefined>(undefined);
  const [reviewingPdfItem, setReviewingPdfItem] = useState<any | null>(null);

  const [bundleDraft, setBundleDraft] = useState<any>({
    slug: '',
    name: '',
    description: '',
    discountType: 'PERCENT',
    discountValue: 10,
    productIds: [],
  });
  const [couponItems, setCouponItems] = useState<any[]>([]);
  const [returnItems, setReturnItems] = useState<any[]>([]);
  const [stockAlerts, setStockAlerts] = useState<any[]>([]);
  const [stockSubscriptions, setStockSubscriptions] = useState<any[]>([]);
  const [cartRecovery, setCartRecovery] = useState<any[]>([]);

  // Retailer Network & Fulfillment state
  const [adminRetailers, setAdminRetailers] = useState<any[]>([]);
  const [selectedRetailerData, setSelectedRetailerData] = useState<any | null>(null);
  const [retailerDraft, setRetailerDraft] = useState<any>({
    name: '',
    ownerName: '',
    email: '',
    phone: '',
    address: '',
    city: '',
    state: 'Rajasthan',
    pincode: '',
    deliveryRadiusKm: 25.0,
    commissionRate: 10.0,
  });
  const [serviceAreaDraft, setServiceAreaDraft] = useState<any>({ pincode: '', city: '', areaName: '', deliveryEtaHours: 24 });
  const [retailerStockAdjust, setRetailerStockAdjust] = useState<any | null>(null);
  const [adminMarginRules, setAdminMarginRules] = useState<any[]>([]);
  const [marginDraft, setMarginDraft] = useState<any>({ category: 'Hardware', marginType: 'PERCENTAGE', marginValue: 10.0, priority: 1 });
  const [adminSettlements, setAdminSettlements] = useState<any[]>([]);
  const [evalOrderId, setEvalOrderId] = useState<string | null>(null);
  const [evalData, setEvalData] = useState<any | null>(null);
  const [manualAssignRetId, setManualAssignRetId] = useState<number | null>(null);
  const [manualAssignNotes, setManualAssignNotes] = useState<string>('');

  const loadExperienceProduct = async (id: number | null) => {
    setExperienceProductId(id);
    setExperienceAsset(null);
    setSpinItems([]);
    if (!id) return;
    const [asset, spins] = await Promise.all([
      api.admin.getVisualAsset(id).catch(() => null),
      api.admin.listSpin(id).catch(() => []),
    ]);
    setExperienceAsset(asset);
    setSpinItems(spins || []);
  };

  const loadExperienceVisual = async (id: number | null) => {
    setExperienceVisualId(id);
    setHotspotItems([]);
    setEditingHotspotId(null);
    if (!id) return;
    const hotspots = await api.admin.listHotspots(id).catch(() => []);
    setHotspotItems(hotspots || []);
  };

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const [
        d,
        p,
        i,
        o,
        c,
        co,
        cu,
        rv,
        qt,
        cd,
        consultations,
        vc,
        b,
        cps,
        rr,
        sa,
        ss,
        cr,
        br,
        sc,
        pj,
        rets,
        margins,
        settles,
      ] = await Promise.all([
        api.admin.dashboard(),
        api.admin.products(),
        api.admin.inventory(),
        api.admin.orders(),
        api.admin.categories(),
        api.admin.collections(),
        api.admin.customers(),
        api.admin.reviews(),
        api.admin.quotes(),
        api.admin.customDesign(),
        api.admin.consultations().catch(() => []),
        api.admin.visualContent(),
        api.admin.bundles(),
        api.admin.coupons(),
        api.admin.returns(),
        api.admin.stockAlerts(),
        api.admin.stockSubscriptions(),
        api.admin.cartRecovery(),
        api.admin.brands().catch(() => []),
        api.admin.subcategories().catch(() => []),
        api.admin.pdfImports.list().catch(() => []),
        api.admin.retailers.list(0, 50).catch(() => ({ content: [] })),
        api.admin.marginRules.list().catch(() => []),
        api.admin.settlements.list(0, 50).catch(() => ({ content: [] })),
      ]);
      setDashboard(d);
      setAdminProducts(p);
      setAdminBundles(b);
      setStock(i);
      setAdminOrders(o);
      setCategories(c);
      setCollections(co);
      setAdminCustomers(cu);
      setAdminReviews(rv);
      setAdminQuotes(qt);
      setAdminCustom(cd);
      setAdminConsultations(consultations);
      setVisualItems(vc);
      setCouponItems(cps);
      setReturnItems(rr);
      setStockAlerts(sa);
      setStockSubscriptions(ss);
      setCartRecovery(cr);
      setBrands(br);
      setSubcategories(sc);
      setPdfJobs(pj);
      setAdminRetailers((rets as any).content || []);
      setAdminMarginRules(margins);
      setAdminSettlements((settles as any).content || []);
      setSelected([]);
    } catch (e: any) {
      setError(e.message || 'Admin access required');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const openCreate = () => {
    setEditing(null);
    setShowForm(true);
  };

  const openEdit = (p: any) => {
    setEditing(p);
    setShowForm(true);
  };

  const toggle = (id: number) =>
    setSelected((v) =>
      v.includes(id) ? v.filter((x) => x !== id) : [...v, id]
    );

  const bulk = async (body: any) => {
    if (!selected.length) return;
    try {
      await api.admin.bulkProducts({ ids: selected, ...body });
      await load();
    } catch (e: any) {
      setError(e.message || 'Bulk operation failed');
    }
  };

  const setQty = async (productId: number, quantity: number) => {
    try {
      await api.admin.updateStock(productId, quantity);
      await load();
    } catch (e: any) {
      setError(e.message || 'Could not update stock');
    }
  };

  const setStatus = async (id: string, status: string) => {
    try {
      await api.admin.updateOrderStatus(id, status);
      await load();
    } catch (e: any) {
      setError(e.message || 'Could not update order');
    }
  };

  const saveCategory = async () => {
    if (!categoryDraft.trim()) return;
    try {
      await api.admin.createCategory({
        name: categoryDraft.trim(),
        active: true,
      });
      setCategoryDraft('');
      await load();
    } catch (e: any) {
      setError(e.message);
    }
  };

  const saveBrand = async () => {
    if (!brandDraft.name.trim()) return;
    try {
      await api.admin.createBrand({
        ...brandDraft,
        slug: brandDraft.name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, ''),
        active: true,
      });
      setBrandDraft({
        name: '',
        description: '',
        countryOfOrigin: 'India',
        websiteUrl: '',
        logoUrl: '',
      });
      const br = await api.admin.brands();
      setBrands(br);
    } catch (e: any) {
      setError(e.message || 'Could not save brand');
    }
  };

  const toggleBrand = async (brand: any) => {
    try {
      await api.admin.updateBrand(brand.id, {
        ...brand,
        active: !brand.active,
      });
      const br = await api.admin.brands();
      setBrands(br);
    } catch (e: any) {
      setError(e.message || 'Could not update brand');
    }
  };

  const saveSubcategory = async () => {
    if (!subcategoryDraft.name.trim()) return;
    try {
      await api.admin.createSubcategory({
        ...subcategoryDraft,
        slug: subcategoryDraft.name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, ''),
        active: true,
      });
      setSubcategoryDraft({
        name: '',
        categoryId: categories[0]?.id || 1,
        description: '',
      });
      const sc = await api.admin.subcategories();
      setSubcategories(sc);
    } catch (e: any) {
      setError(e.message || 'Could not save subcategory');
    }
  };

  const handleUploadPdf = async () => {
    if (!pdfFile) return;
    setPdfUploading(true);
    setError('');
    try {
      const fd = new FormData();
      fd.append('file', pdfFile);
      if (pdfBrandId) fd.append('brandId', String(pdfBrandId));
      if (pdfCategoryId) fd.append('categoryId', String(pdfCategoryId));
      const job = await api.admin.pdfImports.upload(fd);
      const jobs = await api.admin.pdfImports.list();
      setPdfJobs(jobs);
      setSelectedJob(job);
      setPdfFile(null);
    } catch (e: any) {
      setError(e.message || 'PDF upload/analysis failed');
    } finally {
      setPdfUploading(false);
    }
  };

  const loadJobDetails = async (jobId: number) => {
    try {
      const job = await api.admin.pdfImports.get(jobId);
      setSelectedJob(job);
    } catch (e: any) {
      setError(e.message || 'Could not load PDF job details');
    }
  };

  const approvePdfItem = async (itemId: number) => {
    try {
      await api.admin.pdfImports.approveItem(itemId);
      if (selectedJob) {
        await loadJobDetails(selectedJob.id);
      }
      await load();
    } catch (e: any) {
      setError(e.message || 'Approval failed');
    }
  };

  const rejectPdfItem = async (itemId: number) => {
    try {
      await api.admin.pdfImports.rejectItem(itemId);
      if (selectedJob) {
        await loadJobDetails(selectedJob.id);
      }
    } catch (e: any) {
      setError(e.message || 'Rejection failed');
    }
  };

  const approveAllPdfJob = async (jobId: number) => {
    try {
      await api.admin.pdfImports.approveAll(jobId);
      await loadJobDetails(jobId);
      await load();
    } catch (e: any) {
      setError(e.message || 'Approve all failed');
    }
  };

  const saveCollection = async () => {
    if (!collectionDraft.name.trim()) return;
    try {
      await api.admin.createCollection({
        ...collectionDraft,
        productIds: collectionDraft.productIds.map(Number),
      });
      setCollectionDraft({
        name: '',
        description: '',
        active: true,
        productIds: [],
      });
      await load();
    } catch (e: any) {
      setError(e.message);
    }
  };

  return (
    <main className="container-w section admin-page">
      <div className="admin-head">
        <div>
          <p className="eyebrow">Wolfe operations</p>
          <h1>Store management.</h1>
          <p>Catalog, customers, orders, content and commerce controls.</p>
        </div>
        <button className="btn btn-orange" onClick={openCreate}>
          Add product
        </button>
      </div>
      <div className="admin-tabs">
        {(
          [
            'dashboard',
            'products',
            'catalog',
            'brands',
            'subcategories',
            'pdf-import',
            'retailers',
            'allocations',
            'settlements',
            'bundles',
            'media',
            'inventory',
            'orders',
            'customers',
            'reviews',
            'quotes',
            'custom',
            'consultations',
            'visual',
            'experience',
            'commerce',
          ] as const
        ).map((x) => (
          <button
            key={x}
            onClick={() => setTab(x)}
            className={tab === x ? 'active' : ''}
          >
            {x}
          </button>
        ))}
      </div>
      {error && (
        <div className="admin-note">
          <strong>{error}</strong>
        </div>
      )}
      {loading ? (
        <div className="admin-note">
          <h2>Loading operations…</h2>
        </div>
      ) : (
        <>
          {tab === 'dashboard' && (
            <>
              <div className="admin-stats">
                {[
                  ['Products', dashboard?.products ?? 0],
                  ['Active', dashboard?.activeProducts ?? 0],
                  ['Customers', dashboard?.customers ?? 0],
                  ['Orders', dashboard?.orders ?? 0],
                  ['Revenue', money(Number(dashboard?.revenue ?? 0) / 100)],
                  ['Low stock', dashboard?.lowStock ?? 0],
                  ['Pending reviews', dashboard?.pendingReviews ?? 0],
                  ['Open quotes', dashboard?.openQuotes ?? 0],
                ].map((x) => (
                  <div className="admin-stat" key={String(x[0])}>
                    <span>{x[0]}</span>
                    <strong>{x[1]}</strong>
                  </div>
                ))}
              </div>
              <div className="admin-overview-grid">
                <div className="admin-panel">
                  <h2>Order pipeline</h2>
                  {[
                    ['Confirmed', dashboard?.confirmed],
                    ['Processing', dashboard?.processing],
                    ['Shipped', dashboard?.shipped],
                    ['Delivered', dashboard?.delivered],
                  ].map((x) => (
                    <div className="admin-row" key={String(x[0])}>
                      <span>{x[0]}</span>
                      <strong>{x[1] ?? 0}</strong>
                    </div>
                  ))}
                </div>
                <div className="admin-panel">
                  <h2>Attention</h2>
                  <div className="admin-row">
                    <span>Low-stock items</span>
                    <strong>{dashboard?.lowStock ?? 0}</strong>
                  </div>
                  <div className="admin-row">
                    <span>Pending reviews</span>
                    <strong>{dashboard?.pendingReviews ?? 0}</strong>
                  </div>
                  <div className="admin-row">
                    <span>Open custom designs</span>
                    <strong>{dashboard?.openCustomDesigns ?? 0}</strong>
                  </div>
                  <div className="admin-row">
                    <span>Returns</span>
                    <strong>{dashboard?.returns ?? 0}</strong>
                  </div>
                </div>
              </div>
            </>
          )}
          {tab === 'products' && (
            <>
              <div className="admin-panel" style={{ marginBottom: 16 }}>
                <strong>{selected.length} selected</strong>{' '}
                <button
                  className="btn btn-light"
                  onClick={() => bulk({ active: true })}
                  disabled={!selected.length}
                >
                  Publish
                </button>{' '}
                <button
                  className="btn btn-light"
                  onClick={() => bulk({ active: false })}
                  disabled={!selected.length}
                >
                  Hide
                </button>{' '}
                <button
                  className="btn btn-light"
                  onClick={() => bulk({ featured: true })}
                  disabled={!selected.length}
                >
                  Feature
                </button>{' '}
                <button
                  className="btn btn-light"
                  onClick={() => bulk({ featured: false })}
                  disabled={!selected.length}
                >
                  Unfeature
                </button>
              </div>
              <div className="admin-list">
                {adminProducts.map((p) => (
                  <div key={p.id}>
                    <span>
                      <input
                        type="checkbox"
                        checked={selected.includes(p.id)}
                        onChange={() => toggle(p.id)}
                      />{' '}
                      {p.name}
                      <small>
                        {p.slug} · {p.category} · {p.finish} · order{' '}
                        {p.sortOrder ?? 0} ·{' '}
                        {p.active === false ? 'Hidden' : 'Visible'}
                      </small>
                    </span>
                    <span className="admin-actions">
                      <strong>{money(Number(p.price))}</strong>
                      <button onClick={() => openEdit(p)}>Edit</button>
                      <button
                        onClick={async () => {
                          await api.admin.deleteProduct(p.id);
                          await load();
                        }}
                      >
                        Hide
                      </button>
                    </span>
                  </div>
                ))}
              </div>
            </>
          )}
          {tab === 'catalog' && (
            <div className="admin-overview-grid">
              <div className="admin-panel">
                <h2>Categories</h2>
                <div className="coupon-row">
                  <input
                    className="field"
                    value={categoryDraft}
                    onChange={(e) => setCategoryDraft(e.target.value)}
                    placeholder="New category"
                  />
                  <button className="btn" onClick={saveCategory}>
                    Add
                  </button>
                </div>
                {categories.map((c) => (
                  <div className="admin-row" key={c.id}>
                    <span>
                      {c.name}
                      <small>{c.active ? 'Active' : 'Hidden'}</small>
                    </span>
                    <button
                      onClick={async () => {
                        const name = window.prompt('Category name', c.name);
                        if (name?.trim()) {
                          await api.admin.updateCategory(c.id, {
                            name: name.trim(),
                            active: c.active,
                          });
                          await load();
                        }
                      }}
                    >
                      Edit
                    </button>
                    <button
                      onClick={async () => {
                        await api.admin.deleteCategory(c.id);
                        await load();
                      }}
                    >
                      Hide
                    </button>
                  </div>
                ))}
              </div>
              <div className="admin-panel">
                <h2>Collections</h2>
                <div className="checkout-form">
                  <input
                    className="field"
                    value={collectionDraft.name}
                    onChange={(e) =>
                      setCollectionDraft({
                        ...collectionDraft,
                        name: e.target.value,
                      })
                    }
                    placeholder="Collection name"
                  />
                  <textarea
                    className="field admin-textarea"
                    value={collectionDraft.description}
                    onChange={(e) =>
                      setCollectionDraft({
                        ...collectionDraft,
                        description: e.target.value,
                      })
                    }
                    placeholder="Description"
                  />
                  <select
                    className="select-field"
                    multiple
                    value={collectionDraft.productIds.map(String)}
                    onChange={(e) =>
                      setCollectionDraft({
                        ...collectionDraft,
                        productIds: Array.from(e.target.selectedOptions).map(
                          (x) => Number(x.value)
                        ),
                      })
                    }
                  >
                    {adminProducts.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.name}
                      </option>
                    ))}
                  </select>
                  <button
                    type="button"
                    className="btn btn-orange"
                    onClick={saveCollection}
                  >
                    Create collection
                  </button>
                </div>
                {collections.map((c) => (
                  <div className="admin-row" key={c.id}>
                    <span>
                      {c.name}
                      <small>
                        {c.productCount} products ·{' '}
                        {c.active ? 'Active' : 'Hidden'}
                      </small>
                    </span>
                    <button
                      onClick={async () => {
                        const name = window.prompt('Collection name', c.name);
                        if (name?.trim()) {
                          const description =
                            (window.prompt(
                              'Description',
                              c.description || ''
                            ) ?? c.description) || '';
                          await api.admin.updateCollection(c.id, {
                            name: name.trim(),
                            description,
                            active: c.active,
                            productIds: c.productIds || [],
                          });
                          await load();
                        }
                      }}
                    >
                      Edit
                    </button>
                    <button
                      onClick={async () => {
                        await api.admin.deleteCollection(c.id);
                        await load();
                      }}
                    >
                      Delete
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
          {tab === 'brands' && (
            <div className="admin-overview-grid">
              <div className="admin-panel">
                <h2>Manage Brands</h2>
                <p className="admin-help">
                  First-class brand entities. Active brands appear in store filters and product cards.
                </p>
                <div className="checkout-form" style={{ marginBottom: 20 }}>
                  <input
                    className="field"
                    value={brandDraft.name}
                    onChange={(e) => setBrandDraft({ ...brandDraft, name: e.target.value })}
                    placeholder="Brand Name (e.g. Hafele, Greenply, CenturyPly)"
                  />
                  <input
                    className="field"
                    value={brandDraft.countryOfOrigin}
                    onChange={(e) => setBrandDraft({ ...brandDraft, countryOfOrigin: e.target.value })}
                    placeholder="Country of Origin"
                  />
                  <input
                    className="field"
                    value={brandDraft.websiteUrl}
                    onChange={(e) => setBrandDraft({ ...brandDraft, websiteUrl: e.target.value })}
                    placeholder="Official Website URL"
                  />
                  <textarea
                    className="field admin-textarea"
                    value={brandDraft.description}
                    onChange={(e) => setBrandDraft({ ...brandDraft, description: e.target.value })}
                    placeholder="Brand story / Overview"
                  />
                  <button type="button" className="btn btn-orange" onClick={saveBrand}>
                    Add Brand
                  </button>
                </div>
              </div>
              <div className="admin-panel">
                <h2>All Brands ({brands.length})</h2>
                <div className="admin-list">
                  {brands.map((b) => (
                    <div key={b.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 0', borderBottom: '1px solid var(--w-border, #eee)' }}>
                      <span>
                        <strong>{b.name}</strong> ({b.countryOfOrigin || 'India'})
                        <br />
                        <small style={{ color: '#666' }}>{b.slug} · {b.description || 'No description'}</small>
                      </span>
                      <div className="admin-actions">
                        <button
                          className={b.active ? 'btn btn-light' : 'btn btn-orange'}
                          onClick={() => toggleBrand(b)}
                        >
                          {b.active ? 'Deactivate' : 'Activate'}
                        </button>
                      </div>
                    </div>
                  ))}
                  {!brands.length && <p className="empty-state">No brands added yet.</p>}
                </div>
              </div>
            </div>
          )}
          {tab === 'subcategories' && (
            <div className="admin-overview-grid">
              <div className="admin-panel">
                <h2>Add Subcategory</h2>
                <p className="admin-help">
                  Create fine-grained taxonomy for Hardware, Plywood, Laminates, and Kitchen Accessories.
                </p>
                <div className="checkout-form" style={{ marginBottom: 20 }}>
                  <label>Category</label>
                  <select
                    className="field select-field"
                    value={subcategoryDraft.categoryId}
                    onChange={(e) => setSubcategoryDraft({ ...subcategoryDraft, categoryId: Number(e.target.value) })}
                  >
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                  <input
                    className="field"
                    value={subcategoryDraft.name}
                    onChange={(e) => setSubcategoryDraft({ ...subcategoryDraft, name: e.target.value })}
                    placeholder="Subcategory Name (e.g. Telescopic Channels, Marine Plywood)"
                  />
                  <textarea
                    className="field admin-textarea"
                    value={subcategoryDraft.description}
                    onChange={(e) => setSubcategoryDraft({ ...subcategoryDraft, description: e.target.value })}
                    placeholder="Description (optional)"
                  />
                  <button type="button" className="btn btn-orange" onClick={saveSubcategory}>
                    Add Subcategory
                  </button>
                </div>
              </div>
              <div className="admin-panel">
                <h2>Existing Subcategories ({subcategories.length})</h2>
                <div className="admin-list">
                  {subcategories.map((sc) => (
                    <div key={sc.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 0', borderBottom: '1px solid var(--w-border, #eee)' }}>
                      <span>
                        <strong>{sc.name}</strong>
                        <br />
                        <small style={{ color: '#666' }}>
                          Category ID: {sc.categoryId} ({categories.find((c) => c.id === sc.categoryId)?.name || 'Category'}) · {sc.slug}
                        </small>
                      </span>
                      <div className="admin-actions">
                        <button
                          className="btn btn-light"
                          onClick={async () => {
                            await api.admin.deleteSubcategory(sc.id);
                            setSubcategories(await api.admin.subcategories());
                          }}
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  ))}
                  {!subcategories.length && <p className="empty-state">No subcategories defined.</p>}
                </div>
              </div>
            </div>
          )}
          {tab === 'pdf-import' && (
            <div>
              <div className="admin-overview-grid" style={{ marginBottom: 24 }}>
                <div className="admin-panel">
                  <h2>Upload Catalog PDF</h2>
                  <p className="admin-help">
                    Upload a supplier or brand catalog PDF (Hafele, Blum, Greenply, CenturyPly, etc.).
                    Wolfe extracts text, specifications, product codes/SKUs, dimensions, and high-resolution images automatically.
                  </p>
                  <div className="checkout-form">
                    <label>PDF Catalog File (*.pdf, up to 50MB)</label>
                    <input
                      type="file"
                      accept=".pdf"
                      className="field"
                      onChange={(e) => setPdfFile(e.target.files?.[0] || null)}
                    />
                    <label>Default Brand (Optional)</label>
                    <select
                      className="field select-field"
                      value={pdfBrandId || ''}
                      onChange={(e) => setPdfBrandId(e.target.value ? Number(e.target.value) : undefined)}
                    >
                      <option value="">Auto-detect from text / None</option>
                      {brands.map((b) => (
                        <option key={b.id} value={b.id}>{b.name}</option>
                      ))}
                    </select>
                    <label>Default Category (Optional)</label>
                    <select
                      className="field select-field"
                      value={pdfCategoryId || ''}
                      onChange={(e) => setPdfCategoryId(e.target.value ? Number(e.target.value) : undefined)}
                    >
                      <option value="">Auto-detect / None</option>
                      {categories.map((c) => (
                        <option key={c.id} value={c.id}>{c.name}</option>
                      ))}
                    </select>
                    <button
                      type="button"
                      className="btn btn-orange"
                      onClick={handleUploadPdf}
                      disabled={!pdfFile || pdfUploading}
                    >
                      {pdfUploading ? 'Analyzing & Extracting PDF…' : 'Upload & Extract Catalog'}
                    </button>
                  </div>
                </div>

                <div className="admin-panel">
                  <h2>Import Jobs History ({pdfJobs.length})</h2>
                  <div className="admin-list">
                    {pdfJobs.map((job) => (
                      <div
                        key={job.id}
                        style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          padding: '12px',
                          marginBottom: 8,
                          borderRadius: 8,
                          background: selectedJob?.id === job.id ? 'var(--w-surface-2, #f3f3f3)' : 'transparent',
                          border: '1px solid var(--w-border, #eee)',
                          cursor: 'pointer',
                        }}
                        onClick={() => loadJobDetails(job.id)}
                      >
                        <div>
                          <strong>{job.fileName}</strong>
                          <br />
                          <small style={{ color: '#666' }}>
                            Status: <span style={{ fontWeight: 600, color: job.status === 'COMPLETED' ? 'green' : 'orange' }}>{job.status}</span> · Items: {job.totalItems || 0} (Approved: {job.approvedItems || 0}, Rejected: {job.rejectedItems || 0})
                          </small>
                        </div>
                        <button
                          className="btn btn-light"
                          onClick={(e) => {
                            e.stopPropagation();
                            loadJobDetails(job.id);
                          }}
                        >
                          Review Items
                        </button>
                      </div>
                    ))}
                    {!pdfJobs.length && <p className="empty-state">No PDF import jobs yet.</p>}
                  </div>
                </div>
              </div>

              {selectedJob && (
                <div className="admin-panel" style={{ marginTop: 24 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                    <div>
                      <h2>Job Review: {selectedJob.fileName}</h2>
                      <p className="admin-help">
                        Review extracted products and images. Approve valid products or reject duplicates.
                      </p>
                    </div>
                    <button
                      className="btn btn-orange"
                      onClick={() => approveAllPdfJob(selectedJob.id)}
                    >
                      Approve All Valid Items
                    </button>
                  </div>

                  <div className="admin-list">
                    {selectedJob.items?.map((item: any) => (
                      <div
                        key={item.id}
                        style={{
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'center',
                          padding: '16px',
                          marginBottom: 12,
                          borderRadius: 8,
                          border: '1px solid var(--w-border, #eee)',
                          background: item.status === 'APPROVED' ? '#f0fdf4' : item.status === 'REJECTED' ? '#fef2f2' : '#ffffff',
                        }}
                      >
                        <div style={{ display: 'flex', gap: 16, alignItems: 'center' }}>
                          {item.images && item.images.length > 0 ? (
                            <img
                              src={item.images[0]}
                              alt={item.productName}
                              style={{ width: 64, height: 64, objectFit: 'cover', borderRadius: 6, border: '1px solid #ddd' }}
                            />
                          ) : (
                            <div style={{ width: 64, height: 64, background: '#eee', borderRadius: 6, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 10, color: '#888' }}>
                              No Image
                            </div>
                          )}
                          <div>
                            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                              <strong style={{ fontSize: 16 }}>{item.productName}</strong>
                              <span style={{ fontSize: 11, padding: '2px 6px', borderRadius: 4, background: item.status === 'APPROVED' ? '#dcfce7' : item.status === 'REJECTED' ? '#fee2e2' : '#fef3c7', fontWeight: 600 }}>
                                {item.status}
                              </span>
                              {item.confidenceScore && (
                                <span style={{ fontSize: 11, padding: '2px 6px', borderRadius: 4, background: '#e0f2fe', color: '#0369a1' }}>
                                  {(item.confidenceScore * 100).toFixed(0)}% confidence
                                </span>
                              )}
                              {item.duplicateDetected && (
                                <span style={{ fontSize: 11, padding: '2px 6px', borderRadius: 4, background: '#fee2e2', color: '#b91c1c' }}>
                                  Duplicate SKU
                                </span>
                              )}
                            </div>
                            <small style={{ color: '#666', display: 'block', marginTop: 4 }}>
                              SKU: <strong>{item.sku || 'N/A'}</strong> · Price: <strong>{money(item.price ? Number(item.price) : 0)}</strong> · Category: {item.category || 'N/A'} · Brand: {item.brand || 'N/A'}
                            </small>
                            {item.specs && Object.keys(item.specs).length > 0 && (
                              <small style={{ color: '#888', display: 'block', marginTop: 2 }}>
                                Specs: {Object.entries(item.specs).map(([k, v]) => `${k}: ${v}`).join(' | ')}
                              </small>
                            )}
                          </div>
                        </div>

                        <div className="admin-actions" style={{ display: 'flex', gap: 8 }}>
                          {item.status !== 'APPROVED' && (
                            <button
                              className="btn btn-orange"
                              style={{ padding: '6px 12px', fontSize: 13 }}
                              onClick={() => approvePdfItem(item.id)}
                            >
                              Approve
                            </button>
                          )}
                          {item.status !== 'REJECTED' && (
                            <button
                              className="btn btn-light"
                              style={{ padding: '6px 12px', fontSize: 13 }}
                              onClick={() => rejectPdfItem(item.id)}
                            >
                              Reject
                            </button>
                          )}
                        </div>
                      </div>
                    ))}
                    {(!selectedJob.items || !selectedJob.items.length) && (
                      <p className="empty-state">No items extracted for this job.</p>
                    )}
                  </div>
                </div>
              )}
            </div>
          )}
          {tab === 'retailers' && (
            <div className="admin-overview-grid">
              <div className="admin-panel">
                <h2>Add fulfillment partner</h2>
                <p className="admin-help">
                  Register a local hardware / ply / laminate retailer to serve regional delivery pincodes.
                </p>
                <input
                  className="field"
                  value={retailerDraft.name}
                  onChange={(e) => setRetailerDraft({ ...retailerDraft, name: e.target.value })}
                  placeholder="Business / store name *"
                />
                <input
                  className="field"
                  value={retailerDraft.ownerName}
                  onChange={(e) => setRetailerDraft({ ...retailerDraft, ownerName: e.target.value })}
                  placeholder="Owner / contact name"
                />
                <input
                  className="field"
                  type="email"
                  value={retailerDraft.email}
                  onChange={(e) => setRetailerDraft({ ...retailerDraft, email: e.target.value })}
                  placeholder="Email (unique login identifier) *"
                />
                <input
                  className="field"
                  value={retailerDraft.phone}
                  onChange={(e) => setRetailerDraft({ ...retailerDraft, phone: e.target.value })}
                  placeholder="Phone number *"
                />
                <input
                  className="field"
                  value={retailerDraft.address}
                  onChange={(e) => setRetailerDraft({ ...retailerDraft, address: e.target.value })}
                  placeholder="Store address *"
                />
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
                  <input
                    className="field"
                    value={retailerDraft.city}
                    onChange={(e) => setRetailerDraft({ ...retailerDraft, city: e.target.value })}
                    placeholder="City *"
                  />
                  <input
                    className="field"
                    value={retailerDraft.pincode}
                    onChange={(e) => setRetailerDraft({ ...retailerDraft, pincode: e.target.value })}
                    placeholder="Pincode *"
                  />
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
                  <input
                    className="field"
                    type="number"
                    value={retailerDraft.deliveryRadiusKm}
                    onChange={(e) => setRetailerDraft({ ...retailerDraft, deliveryRadiusKm: Number(e.target.value) })}
                    placeholder="Radius (km)"
                  />
                  <input
                    className="field"
                    type="number"
                    value={retailerDraft.commissionRate}
                    onChange={(e) => setRetailerDraft({ ...retailerDraft, commissionRate: Number(e.target.value) })}
                    placeholder="Commission %"
                  />
                </div>
                <button
                  className="btn btn-orange"
                  onClick={async () => {
                    if (!retailerDraft.name || !retailerDraft.email || !retailerDraft.phone || !retailerDraft.address || !retailerDraft.city || !retailerDraft.pincode) {
                      setError('Please complete all required retailer fields');
                      return;
                    }
                    try {
                      await api.admin.retailers.create(retailerDraft);
                      setRetailerDraft({ name: '', ownerName: '', email: '', phone: '', address: '', city: '', state: 'Rajasthan', pincode: '', deliveryRadiusKm: 25.0, commissionRate: 10.0 });
                      await load();
                    } catch (err: any) {
                      setError(err.message || 'Failed to create retailer');
                    }
                  }}
                >
                  Register Partner
                </button>
              </div>

              <div className="admin-panel">
                <h2>Fulfillment partner directory ({adminRetailers.length})</h2>
                <div className="admin-list" style={{ maxHeight: 600, overflowY: 'auto' }}>
                  {adminRetailers.map((r) => (
                    <div key={r.id} style={{ display: 'flex', flexDirection: 'column', gap: 8, padding: 12, borderBottom: '1px solid #292524' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div>
                          <strong>{r.name}</strong>
                          <span style={{ fontSize: 12, color: '#a8a29e', display: 'block' }}>
                            {r.city} ({r.pincode}) · Radius: {r.deliveryRadiusKm}km · Margin: {r.commissionRate}%
                          </span>
                          <span style={{ fontSize: 11, color: '#78716c', fontFamily: 'monospace' }}>
                            {r.email} · {r.phone}
                          </span>
                        </div>
                        <div style={{ display: 'flex', gap: 6 }}>
                          <select
                            className="select-field"
                            style={{ fontSize: 12, padding: '4px 8px' }}
                            value={r.status}
                            onChange={async (e) => {
                              await api.admin.retailers.update(r.id, { status: e.target.value as any });
                              await load();
                            }}
                          >
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="PENDING">PENDING</option>
                            <option value="SUSPENDED">SUSPENDED</option>
                            <option value="INACTIVE">INACTIVE</option>
                          </select>
                          <button
                            className="btn btn-light"
                            style={{ padding: '4px 8px', fontSize: 12 }}
                            onClick={async () => {
                              const details = await api.admin.retailers.get(r.id);
                              setSelectedRetailerData(details);
                            }}
                          >
                            Manage
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                  {!adminRetailers.length && <p className="empty-state">No retailer partners registered.</p>}
                </div>
              </div>

              {selectedRetailerData && (
                <div className="admin-panel" style={{ gridColumn: '1 / -1', marginTop: 16 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                    <h2>Partner Details: {selectedRetailerData.retailer.name}</h2>
                    <button className="btn btn-light" onClick={() => setSelectedRetailerData(null)}>Close</button>
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                    <div>
                      <h3>Service areas ({selectedRetailerData.serviceAreas?.length || 0})</h3>
                      <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
                        <input
                          className="field"
                          placeholder="Pincode"
                          value={serviceAreaDraft.pincode}
                          onChange={(e) => setServiceAreaDraft({ ...serviceAreaDraft, pincode: e.target.value })}
                        />
                        <input
                          className="field"
                          placeholder="City"
                          value={serviceAreaDraft.city}
                          onChange={(e) => setServiceAreaDraft({ ...serviceAreaDraft, city: e.target.value })}
                        />
                        <button
                          className="btn btn-orange"
                          onClick={async () => {
                            if (!serviceAreaDraft.pincode || !serviceAreaDraft.city) return;
                            await api.admin.retailers.addServiceArea(selectedRetailerData.retailer.id, serviceAreaDraft);
                            setServiceAreaDraft({ pincode: '', city: '', areaName: '', deliveryEtaHours: 24 });
                            const updated = await api.admin.retailers.get(selectedRetailerData.retailer.id);
                            setSelectedRetailerData(updated);
                          }}
                        >
                          Add Area
                        </button>
                      </div>
                      <div className="admin-list" style={{ maxHeight: 200, overflowY: 'auto' }}>
                        {selectedRetailerData.serviceAreas?.map((sa: any) => (
                          <div key={sa.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 8px' }}>
                            <span>{sa.pincode} - {sa.city} ({sa.deliveryEtaHours}h ETA)</span>
                            <button
                              className="btn btn-light"
                              style={{ padding: '2px 6px', fontSize: 11 }}
                              onClick={async () => {
                                await api.admin.retailers.removeServiceArea(sa.id);
                                const updated = await api.admin.retailers.get(selectedRetailerData.retailer.id);
                                setSelectedRetailerData(updated);
                              }}
                            >
                              Remove
                            </button>
                          </div>
                        ))}
                      </div>
                    </div>

                    <div>
                      <h3>Low stock items ({selectedRetailerData.lowStockItems?.length || 0})</h3>
                      <div className="admin-list" style={{ maxHeight: 240, overflowY: 'auto' }}>
                        {selectedRetailerData.lowStockItems?.map((ls: any) => (
                          <div key={ls.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 8px' }}>
                            <span><strong>{ls.sku}</strong> (Avail: {ls.availableStock})</span>
                            <button
                              className="btn btn-light"
                              style={{ padding: '2px 6px', fontSize: 11 }}
                              onClick={() => setRetailerStockAdjust(ls)}
                            >
                              Adjust
                            </button>
                          </div>
                        ))}
                        {!selectedRetailerData.lowStockItems?.length && <p className="empty-state">No low stock items.</p>}
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {tab === 'allocations' && (
            <div className="admin-overview-grid">
              <div className="admin-panel" style={{ gridColumn: '1 / -1' }}>
                <h2>Order Allocation & Fulfillment Matrix</h2>
                <p className="admin-help">
                  Inspect regional orders, evaluate candidate fulfillment partners based on inventory and delivery radius, and assign/override fulfillment.
                </p>

                <div className="admin-list">
                  {adminOrders.map((ord) => (
                    <div key={ord.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: 12 }}>
                      <div>
                        <strong>{ord.id}</strong> — <span style={{ color: '#fbbf24' }}>{money(ord.total / 100)}</span>
                        <span style={{ fontSize: 12, color: '#a8a29e', display: 'block' }}>
                          Customer: {ord.customerName} · {ord.city} ({ord.pincode}) · Status: {ord.status}
                        </span>
                      </div>
                      <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                        <button
                          className="btn btn-orange"
                          style={{ padding: '6px 12px', fontSize: 13 }}
                          onClick={async () => {
                            setEvalOrderId(ord.id);
                            try {
                              const res = await api.admin.allocations.eval(ord.id);
                              setEvalData(res);
                              if (res.candidates && res.candidates.length > 0) {
                                setManualAssignRetId(res.candidates[0].retailer.id);
                              }
                            } catch (err: any) {
                              setError(err.message);
                            }
                          }}
                        >
                          Evaluate & Assign
                        </button>
                      </div>
                    </div>
                  ))}
                  {!adminOrders.length && <p className="empty-state">No orders available.</p>}
                </div>
              </div>

              {evalOrderId && evalData && (
                <div className="admin-panel" style={{ gridColumn: '1 / -1', marginTop: 16 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
                    <h2>Allocation Evaluation for Order {evalOrderId}</h2>
                    <button className="btn btn-light" onClick={() => { setEvalOrderId(null); setEvalData(null); }}>Close</button>
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: 16 }}>
                    <div style={{ background: '#1c1917', padding: 12, borderRadius: 8 }}>
                      <h3 style={{ fontSize: 13, textTransform: 'uppercase', color: '#a8a29e', marginBottom: 8 }}>Customer Delivery Target</h3>
                      <p style={{ fontSize: 13, color: '#f5f5f4' }}><strong>{evalData.order.customerName}</strong></p>
                      <p style={{ fontSize: 12, color: '#a8a29e' }}>{evalData.order.address}</p>
                      <p style={{ fontSize: 12, color: '#a8a29e' }}>{evalData.order.city} - {evalData.order.pincode}</p>
                      <p style={{ fontSize: 12, color: '#d6d3d1', marginTop: 6 }}>Phone: {evalData.order.phone}</p>

                      <h3 style={{ fontSize: 13, textTransform: 'uppercase', color: '#a8a29e', marginTop: 16, marginBottom: 8 }}>Order Items ({evalData.items?.length || 0})</h3>
                      <ul style={{ fontSize: 12, color: '#d6d3d1', paddingLeft: 16 }}>
                        {evalData.items?.map((it: any) => (
                          <li key={it.id}>{it.productName} ({it.variantSku || 'BASE-SKU'}) x{it.quantity}</li>
                        ))}
                      </ul>
                    </div>

                    <div>
                      <h3 style={{ fontSize: 13, textTransform: 'uppercase', color: '#a8a29e', marginBottom: 8 }}>Eligible Partner Candidates</h3>
                      <div className="admin-list" style={{ maxHeight: 300, overflowY: 'auto' }}>
                        {evalData.candidates?.map((c: any) => (
                          <div key={c.retailer.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 12px' }}>
                            <div>
                              <strong>{c.retailer.name}</strong> ({c.retailer.city})
                              <span style={{ fontSize: 12, color: '#a8a29e', display: 'block' }}>
                                Serviceable: {c.serviceable ? '✅ Yes' : '❌ Out of area'} · Stock match: {c.matchingItemsCount}/{c.totalItemsCount} · ETA: {c.etaHours}h
                              </span>
                            </div>
                            <button
                              className="btn btn-light"
                              style={{ padding: '4px 10px', fontSize: 12 }}
                              onClick={async () => {
                                try {
                                  await api.admin.allocations.assign(evalOrderId, c.retailer.id, 'Admin manual partner assignment');
                                  const updated = await api.admin.allocations.eval(evalOrderId);
                                  setEvalData(updated);
                                  await load();
                                } catch (err: any) {
                                  setError(err.message);
                                }
                              }}
                            >
                              Assign This Partner
                            </button>
                          </div>
                        ))}
                        {!evalData.candidates?.length && <p className="empty-state">No partner candidates found.</p>}
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {tab === 'settlements' && (
            <div className="admin-overview-grid">
              <div className="admin-panel" style={{ gridColumn: '1 / -1' }}>
                <h2>Partner Settlement Ledger</h2>
                <p className="admin-help">
                  Historical margin and payout calculations. Settlements become ELIGIBLE upon customer delivery confirmation.
                </p>

                <div className="admin-list">
                  {adminSettlements.map((s) => (
                    <div key={s.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: 12 }}>
                      <div>
                        <strong>Order {s.orderId}</strong> · Partner #{s.retailerId}
                        <span style={{ fontSize: 12, color: '#a8a29e', display: 'block' }}>
                          Gross Total: {money(s.grossAmount / 100)} · Wolfe Margin: {money(s.wolfeMarginAmount / 100)} · <strong style={{ color: '#fbbf24' }}>Partner Payable: {money(s.retailerPayableAmount / 100)}</strong>
                        </span>
                      </div>
                      <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                        <span className={`badge ${s.status === 'SETTLED' ? 'badge-green' : s.status === 'ELIGIBLE' ? 'badge-blue' : 'badge-yellow'}`}>
                          {s.status}
                        </span>
                        {s.status === 'ELIGIBLE' && s.cashExpectedAmount && s.cashReconciliationStatus !== 'RECONCILED' && (
                          <button
                            className="btn btn-light"
                            style={{ padding: '4px 10px', fontSize: 12 }}
                            onClick={async () => {
                              const expected = Number(prompt('Expected COD cash (₹):', String((s.cashExpectedAmount || 0) / 100)));
                              if (!Number.isFinite(expected) || expected < 0) return;
                              const collected = Number(prompt('Collected COD cash (₹):', String(expected)));
                              const deposited = Number(prompt('Deposited COD cash (₹):', String(expected)));
                              const ref = prompt('Cash deposit/reconciliation reference:', `CASH-WLF-${Date.now()}`);
                              if (ref) {
                                await api.admin.settlements.reconcileCash(s.id, Math.round(expected * 100), Math.round(collected * 100), Math.round(deposited * 100), ref);
                                await load();
                              }
                            }}
                          >
                            Reconcile COD
                          </button>
                        )}
                        {s.status === 'ELIGIBLE' && (!s.cashExpectedAmount || s.cashReconciliationStatus === 'RECONCILED') && (
                          <button
                            className="btn btn-light"
                            style={{ padding: '4px 10px', fontSize: 12 }}
                            onClick={async () => {
                              const ref = prompt('Enter bank / settlement reference transaction ID:', `TXN-WLF-${Date.now()}`);
                              if (ref) {
                                await api.admin.settlements.settle(s.id, ref);
                                await load();
                              }
                            }}
                          >
                            Mark Settled
                          </button>
                        )}
                        {s.status === 'SETTLED' && (
                          <span style={{ fontSize: 11, color: '#78716c', fontFamily: 'monospace' }}>Ref: {s.referenceNumber}</span>
                        )}
                      </div>
                    </div>
                  ))}
                  {!adminSettlements.length && <p className="empty-state">No settlement records found.</p>}
                </div>
              </div>
            </div>
          )}
          {tab === 'bundles' && (
            <div className="admin-overview-grid">
              <div className="admin-panel">
                <h2>Product bundles</h2>
                <p className="admin-help">
                  Create curated sets. Bundle savings are recalculated
                  server-side at checkout.
                </p>
                <input
                  className="field"
                  value={bundleDraft.slug}
                  onChange={(e) =>
                    setBundleDraft({ ...bundleDraft, slug: e.target.value })
                  }
                  placeholder="Slug"
                />
                <input
                  className="field"
                  value={bundleDraft.name}
                  onChange={(e) =>
                    setBundleDraft({ ...bundleDraft, name: e.target.value })
                  }
                  placeholder="Bundle name"
                />
                <textarea
                  className="field admin-textarea"
                  value={bundleDraft.description}
                  onChange={(e) =>
                    setBundleDraft({
                      ...bundleDraft,
                      description: e.target.value,
                    })
                  }
                  placeholder="Description"
                />
                <select
                  className="select-field"
                  value={bundleDraft.discountType}
                  onChange={(e) =>
                    setBundleDraft({
                      ...bundleDraft,
                      discountType: e.target.value,
                    })
                  }
                >
                  <option>PERCENT</option>
                  <option>FIXED</option>
                </select>
                <input
                  className="field"
                  type="number"
                  min="0"
                  step="0.01"
                  value={bundleDraft.discountValue}
                  onChange={(e) =>
                    setBundleDraft({
                      ...bundleDraft,
                      discountValue: Number(e.target.value),
                    })
                  }
                  placeholder="Discount"
                />
                <select
                  className="select-field"
                  multiple
                  value={bundleDraft.productIds.map(String)}
                  onChange={(e) =>
                    setBundleDraft({
                      ...bundleDraft,
                      productIds: Array.from(e.target.selectedOptions).map(
                        (x) => Number(x.value)
                      ),
                    })
                  }
                >
                  {adminProducts
                    .filter((p) => p.active !== false)
                    .map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.name}
                      </option>
                    ))}
                </select>
                <button
                  className="btn btn-orange"
                  onClick={async () => {
                    if (
                      !bundleDraft.slug ||
                      !bundleDraft.name ||
                      bundleDraft.productIds.length < 2
                    )
                      return;
                    try {
                      if (bundleDraft.id)
                        await api.admin.updateBundle(bundleDraft.id, {
                          ...bundleDraft,
                          active: true,
                        });
                      else
                        await api.admin.createBundle({
                          ...bundleDraft,
                          active: true,
                        });
                      setBundleDraft({
                        slug: '',
                        name: '',
                        description: '',
                        discountType: 'PERCENT',
                        discountValue: 10,
                        productIds: [],
                      });
                      await load();
                    } catch (e: any) {
                      setError(e.message);
                    }
                  }}
                >
                  {bundleDraft.id ? 'Save bundle' : 'Create bundle'}
                </button>
              </div>
              <div className="admin-panel">
                <h2>Active bundles</h2>
                {adminBundles.map((b) => (
                  <div className="admin-row" key={b.id}>
                    <span>
                      <strong>{b.name}</strong>
                      <small>
                        {b.slug} · {b.discountType} {b.discountValue} ·{' '}
                        {b.productIds?.length || 0} products
                      </small>
                    </span>
                    <button
                      onClick={() =>
                        setBundleDraft({
                          id: b.id,
                          slug: b.slug,
                          name: b.name,
                          description: b.description || '',
                          discountType: b.discountType,
                          discountValue: Number(b.discountValue),
                          productIds: b.productIds || [],
                        })
                      }
                    >
                      Edit
                    </button>
                    <button
                      onClick={async () => {
                        await api.admin.deleteBundle(b.id);
                        await load();
                      }}
                    >
                      Hide
                    </button>
                  </div>
                ))}
                {!adminBundles.length && (
                  <p className="empty-state">No bundles yet.</p>
                )}
              </div>
            </div>
          )}
          {tab === 'media' && (
            <div className="admin-list">
              {adminProducts.map((p) => (
                <div key={p.id}>
                  <span>
                    {p.name}
                    <small>
                      {p.imageUrl || 'No primary image'} ·{' '}
                      {
                        String(p.mediaUrls || '')
                          .split(/\n|,/)
                          .filter(Boolean).length
                      }{' '}
                      legacy media
                    </small>
                  </span>
                  <button onClick={() => openEdit(p)}>Manage media</button>
                </div>
              ))}
            </div>
          )}
          {tab === 'inventory' && (
            <div className="admin-list">
              {stock.map((i) => (
                <div key={i.productId}>
                  <span>
                    {i.name}
                    <small>
                      {i.slug} · Reserved {i.reserved} · Available {i.available}
                      {i.available <= 5 ? ' · LOW STOCK' : ''}
                    </small>
                  </span>
                  <span className="stock-control">
                    <input
                      className="admin-stock-input"
                      type="number"
                      min="0"
                      value={i.quantity}
                      onChange={(e) =>
                        setStock((prev) =>
                          prev.map((x) =>
                            x.productId === i.productId
                              ? {
                                  ...x,
                                  quantity: Number(e.target.value),
                                  available:
                                    Number(e.target.value) - x.reserved,
                                }
                              : x
                          )
                        )
                      }
                      onBlur={(e) =>
                        setQty(i.productId, Number(e.target.value))
                      }
                    />
                    <button onClick={() => setQty(i.productId, i.quantity + 1)}>
                      +
                    </button>
                  </span>
                </div>
              ))}
            </div>
          )}
          {tab === 'orders' && (
            <div className="admin-list">
              {adminOrders.map((o) => (
                <div key={o.id}>
                  <span>
                    {o.id}
                    <small>
                      {new Date(o.createdAt).toLocaleString('en-IN')} ·{' '}
                      {o.customerName} · {o.customerEmail}
                    </small>
                  </span>
                  <span className="admin-order-actions">
                    <strong>{money(Number(o.total) / 100)}</strong>
                    <select
                      value={o.status}
                      onChange={(e) => setStatus(o.id, e.target.value)}
                      aria-label={`Order ${o.id} status`}
                    >
                      <option value={o.status}>{o.status}</option>
                      {o.status !== 'CANCELLED' && o.status !== 'DELIVERED' && <option value="CANCELLED">CANCELLED</option>}
                    </select>
                    <button
                      onClick={async () => {
                        const h = await api.admin.orderHistory(o.id);
                        alert(
                          h
                            .map(
                              (x: any) =>
                                `${x.status} — ${new Date(
                                  x.createdAt
                                ).toLocaleString('en-IN')}`
                            )
                            .join('\n')
                        );
                      }}
                    >
                      History
                    </button>
                  </span>
                </div>
              ))}
            </div>
          )}
          {tab === 'customers' && (
            <div className="admin-list">
              {adminCustomers.map((c) => (
                <div key={c.id}>
                  <span>
                    {c.name}
                    <small>
                      {c.email} · {c.phone || 'No phone'} · {c.role}
                    </small>
                  </span>
                  <strong>#{c.id}</strong>
                </div>
              ))}
            </div>
          )}
          {tab === 'reviews' && (
            <div className="admin-list">
              {adminReviews.map((r) => (
                <div key={r.id}>
                  <span>
                    Review #{r.id}
                    <small>
                      {r.rating}/5 · product {r.productId} · {r.review}
                    </small>
                  </span>
                  <select
                    value={r.status}
                    onChange={async (e) => {
                      await api.admin.reviewStatus(r.id, e.target.value);
                      setAdminReviews(await api.admin.reviews());
                    }}
                  >
                    <option>PENDING</option>
                    <option>APPROVED</option>
                    <option>REJECTED</option>
                  </select>
                </div>
              ))}
            </div>
          )}
          {tab === 'quotes' && (
            <div className="admin-list">
              {adminQuotes.map((q) => (
                <div key={q.id}>
                  <span>
                    Quote #{q.id}
                    <small>
                      Customer {q.customerId} · {q.message}
                    </small>
                  </span>
                  <select
                    value={q.status}
                    onChange={async (e) => {
                      await api.admin.quoteStatus(q.id, e.target.value);
                      setAdminQuotes(await api.admin.quotes());
                    }}
                  >
                    <option>NEW</option>
                    <option>CONTACTED</option>
                    <option>QUOTED</option>
                    <option>CLOSED</option>
                  </select>
                </div>
              ))}
            </div>
          )}
          {tab === 'consultations' && (
            <div className="admin-panel">
              <h2>Consultation requests</h2>
              <p className="admin-help">Customer requests submitted from the public consultation form.</p>
              <div className="admin-list">
                {adminConsultations.map((c) => (
                  <div className="admin-row" key={c.id}>
                    <span><strong>#{c.id} · {c.name}</strong><small>{c.email} · {c.project || 'No project'} · {c.message}</small><small>{new Date(c.createdAt).toLocaleString('en-IN')}</small></span>
                    <select value={c.status} onChange={async (e) => { await api.admin.consultationStatus(c.id, e.target.value); setAdminConsultations(await api.admin.consultations()); }}>
                      <option>NEW</option><option>CONTACTED</option><option>IN_PROGRESS</option><option>COMPLETED</option><option>CLOSED</option>
                    </select>
                  </div>
                ))}
                {!adminConsultations.length && <p className="empty-state">No consultation requests.</p>}
              </div>
            </div>
          )}
          {tab === 'visual' && (
            <div className="admin-overview-grid">
              <div className="admin-panel">
                <h2>Visual content studio</h2>
                <p className="admin-help">
                  Hero videos, campaign reels and visual stories. Use hosted
                  MP4/WebM URLs or image URLs.
                </p>
                <select
                  className="select-field"
                  value={visualDraft.placement}
                  onChange={(e) =>
                    setVisualDraft({
                      ...visualDraft,
                      placement: e.target.value,
                    })
                  }
                >
                  <option>HERO</option>
                  <option>COLLECTION_REEL</option>
                  <option>MOODBOARD</option>
                </select>
                <input
                  className="field"
                  value={visualDraft.title}
                  onChange={(e) =>
                    setVisualDraft({ ...visualDraft, title: e.target.value })
                  }
                  placeholder="Title"
                />
                <input
                  className="field"
                  value={visualDraft.subtitle}
                  onChange={(e) =>
                    setVisualDraft({ ...visualDraft, subtitle: e.target.value })
                  }
                  placeholder="Subtitle"
                />
                <select
                  className="select-field"
                  value={visualDraft.mediaType}
                  onChange={(e) =>
                    setVisualDraft({
                      ...visualDraft,
                      mediaType: e.target.value,
                    })
                  }
                >
                  <option>VIDEO</option>
                  <option>IMAGE</option>
                </select>
                <input
                  className="field"
                  value={visualDraft.mediaUrl}
                  onChange={(e) =>
                    setVisualDraft({ ...visualDraft, mediaUrl: e.target.value })
                  }
                  placeholder="Media URL"
                />
                <input
                  className="field"
                  value={visualDraft.posterUrl}
                  onChange={(e) =>
                    setVisualDraft({
                      ...visualDraft,
                      posterUrl: e.target.value,
                    })
                  }
                  placeholder="Video poster URL (optional)"
                />
                <input
                  className="field"
                  value={visualDraft.linkUrl}
                  onChange={(e) =>
                    setVisualDraft({ ...visualDraft, linkUrl: e.target.value })
                  }
                  placeholder="CTA link (optional)"
                />
                <button
                  className="btn btn-orange"
                  onClick={async () => {
                    if (!visualDraft.title || !visualDraft.mediaUrl) return;
                    await api.admin.createVisualContent({
                      ...visualDraft,
                      sortOrder: Number(visualDraft.sortOrder),
                    });
                    setVisualDraft({
                      ...visualDraft,
                      title: '',
                      subtitle: '',
                      mediaUrl: '',
                      posterUrl: '',
                      linkUrl: '',
                    });
                    setVisualItems(await api.admin.visualContent());
                  }}
                >
                  Publish visual
                </button>
              </div>
              <div className="admin-panel">
                <h2>Published visuals</h2>
                {visualItems.map((v) => (
                  <div className="admin-row" key={v.id}>
                    <span>
                      <strong>{v.title}</strong>
                      <small>
                        {v.placement} · {v.mediaType} · {v.mediaUrl}
                      </small>
                    </span>
                    <button
                      onClick={async () => {
                        const title = window.prompt('Title', v.title);
                        if (title?.trim()) {
                          const mediaUrl = window.prompt(
                            'Media URL',
                            v.mediaUrl
                          );
                          if (mediaUrl?.trim()) {
                            const linkUrl =
                              (window.prompt('CTA link', v.linkUrl || '') ??
                                v.linkUrl) || '';
                            await api.admin.updateVisualContent(v.id, {
                              placement: v.placement,
                              title: title.trim(),
                              subtitle: v.subtitle || '',
                              mediaType: v.mediaType,
                              mediaUrl: mediaUrl.trim(),
                              posterUrl: v.posterUrl || '',
                              linkUrl,
                              active: v.active,
                              sortOrder: v.sortOrder,
                            });
                            setVisualItems(await api.admin.visualContent());
                          }
                        }
                      }}
                    >
                      Edit
                    </button>
                    <button
                      onClick={async () => {
                        await api.admin.deleteVisualContent(v.id);
                        setVisualItems(await api.admin.visualContent());
                      }}
                    >
                      Remove
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
          {tab === 'experience' && (
            <div className="admin-overview-grid">
              <div className="admin-panel">
                <h2>360° / 3D / AR experience</h2>
                <p className="admin-help">Manage existing product visual assets and save updates.</p>
                <select
                  className="select-field"
                  value={experienceProductId ?? ''}
                  onChange={(e) => loadExperienceProduct(Number(e.target.value) || null)}
                >
                  <option value="">Select product</option>
                  {adminProducts.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
                <form
                  onSubmit={async (e) => {
                    e.preventDefault();
                    if (!experienceProductId) return;
                    const fd = new FormData(e.currentTarget);
                    const saved = await api.admin.visualAsset(experienceProductId, {
                      modelUrl: String(fd.get('modelUrl') || ''),
                      arUrl: String(fd.get('arUrl') || ''),
                      posterUrl: String(fd.get('posterUrl') || ''),
                      active: String(fd.get('active')) === 'true',
                    });
                    setExperienceAsset(saved);
                    setError('Visual asset saved.');
                  }}
                >
                  <input name="modelUrl" className="field" placeholder="3D model URL" defaultValue={experienceAsset?.modelUrl || ''} key={`model-${experienceAsset?.id || 'new'}`} />
                  <input name="arUrl" className="field" placeholder="AR asset URL" defaultValue={experienceAsset?.arUrl || ''} key={`ar-${experienceAsset?.id || 'new'}`} />
                  <input name="posterUrl" className="field" placeholder="Poster URL" defaultValue={experienceAsset?.posterUrl || ''} key={`poster-${experienceAsset?.id || 'new'}`} />
                  <select name="active" className="select-field" defaultValue={experienceAsset?.active === false ? 'false' : 'true'} key={`active-${experienceAsset?.id || 'new'}`}>
                    <option value="true">Active</option>
                    <option value="false">Inactive</option>
                  </select>
                  <div className="admin-actions">
                    <button className="btn btn-orange" type="submit" disabled={!experienceProductId}>Save / update visual asset</button>
                    {experienceAsset && experienceProductId && (
                      <button
                        className="btn btn-danger"
                        type="button"
                        onClick={async () => {
                          if (!window.confirm('Delete this product visual asset?')) return;
                          await api.admin.deleteVisualAsset(experienceProductId);
                          setExperienceAsset(null);
                          setError('Visual asset deleted.');
                        }}
                      >
                        Delete asset
                      </button>
                    )}
                  </div>
                </form>
                {experienceAsset && (
                  <p className="admin-help">Asset #{experienceAsset.id} · Product #{experienceAsset.productId} · {experienceAsset.active ? 'Active' : 'Inactive'}</p>
                )}
              </div>

              <div className="admin-panel">
                <h2>360° spin frames</h2>
                <p className="admin-help">Existing frames are listed below. Add, edit, reorder or delete them.</p>
                <select
                  className="select-field"
                  value={experienceProductId ?? ''}
                  onChange={(e) => loadExperienceProduct(Number(e.target.value) || null)}
                >
                  <option value="">Select product</option>
                  {adminProducts.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
                <form
                  onSubmit={async (e) => {
                    e.preventDefault();
                    if (!experienceProductId || !spinDraft.imageUrl.trim()) return;
                    const body = { imageUrl: spinDraft.imageUrl.trim(), sortOrder: Number(spinDraft.sortOrder) || 0 };
                    if (editingSpinId) {
                      await api.admin.updateSpin(editingSpinId, body);
                      setError('Spin frame updated.');
                    } else {
                      await api.admin.spin(experienceProductId, body);
                      setError('Spin frame added.');
                    }
                    setEditingSpinId(null);
                    setSpinDraft({ imageUrl: '', sortOrder: 0 });
                    await loadExperienceProduct(experienceProductId);
                  }}
                >
                  <input className="field" value={spinDraft.imageUrl} onChange={(e) => setSpinDraft({ ...spinDraft, imageUrl: e.target.value })} placeholder="Frame image URL" required />
                  <input className="field" type="number" min="0" value={spinDraft.sortOrder} onChange={(e) => setSpinDraft({ ...spinDraft, sortOrder: e.target.value })} placeholder="Frame order" />
                  <button className="btn" type="submit" disabled={!experienceProductId}>{editingSpinId ? 'Update spin frame' : 'Add spin frame'}</button>
                  {editingSpinId && <button className="btn" type="button" onClick={() => { setEditingSpinId(null); setSpinDraft({ imageUrl: '', sortOrder: 0 }); }}>Cancel edit</button>}
                </form>
                <div className="admin-list">
                  {spinItems.length === 0 ? <p className="admin-help">No spin frames for this product.</p> : spinItems.map((frame) => (
                    <div key={frame.id} className="admin-list-row">
                      <div>
                        <strong>#{frame.sortOrder}</strong>
                        <div style={{ fontSize: 12, wordBreak: 'break-all' }}>{frame.imageUrl}</div>
                      </div>
                      <div className="admin-actions">
                        <button className="btn" type="button" onClick={() => { setEditingSpinId(frame.id); setSpinDraft({ imageUrl: frame.imageUrl, sortOrder: frame.sortOrder }); }}>Edit</button>
                        <button className="btn btn-danger" type="button" onClick={async () => { if (!window.confirm('Delete this spin frame?')) return; await api.admin.deleteSpin(frame.id); await loadExperienceProduct(experienceProductId); setError('Spin frame deleted.'); }}>Delete</button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              <div className="admin-panel">
                <h2>Visual hotspots</h2>
                <p className="admin-help">Select an existing visual-content record to manage its hotspots.</p>
                <select
                  className="select-field"
                  value={experienceVisualId ?? ''}
                  onChange={(e) => loadExperienceVisual(Number(e.target.value) || null)}
                >
                  <option value="">Select visual content</option>
                  {visualItems.map((v) => <option key={v.id} value={v.id}>#{v.id} — {v.title}</option>)}
                </select>
                <form
                  onSubmit={async (e) => {
                    e.preventDefault();
                    if (!experienceVisualId || !hotspotDraft.label.trim() || !hotspotDraft.targetSlug.trim()) return;
                    const body = {
                      label: hotspotDraft.label.trim(),
                      targetSlug: hotspotDraft.targetSlug.trim(),
                      x: Number(hotspotDraft.x),
                      y: Number(hotspotDraft.y),
                      active: !!hotspotDraft.active,
                    };
                    if (editingHotspotId) {
                      await api.admin.updateHotspot(editingHotspotId, body);
                      setError('Hotspot updated.');
                    } else {
                      await api.admin.hotspot(experienceVisualId, body);
                      setError('Hotspot added.');
                    }
                    setEditingHotspotId(null);
                    setHotspotDraft({ label: '', targetSlug: '', x: 50, y: 50, active: true });
                    await loadExperienceVisual(experienceVisualId);
                  }}
                >
                  <input className="field" value={hotspotDraft.label} onChange={(e) => setHotspotDraft({ ...hotspotDraft, label: e.target.value })} placeholder="Hotspot label" required />
                  <input className="field" value={hotspotDraft.targetSlug} onChange={(e) => setHotspotDraft({ ...hotspotDraft, targetSlug: e.target.value })} placeholder="Target product slug" required />
                  <div className="admin-variant-add">
                    <input className="field" type="number" min="0" max="100" value={hotspotDraft.x} onChange={(e) => setHotspotDraft({ ...hotspotDraft, x: e.target.value })} placeholder="X %" />
                    <input className="field" type="number" min="0" max="100" value={hotspotDraft.y} onChange={(e) => setHotspotDraft({ ...hotspotDraft, y: e.target.value })} placeholder="Y %" />
                  </div>
                  <select className="select-field" value={hotspotDraft.active ? 'true' : 'false'} onChange={(e) => setHotspotDraft({ ...hotspotDraft, active: e.target.value === 'true' })}>
                    <option value="true">Active</option>
                    <option value="false">Inactive</option>
                  </select>
                  <button className="btn" type="submit" disabled={!experienceVisualId}>{editingHotspotId ? 'Update hotspot' : 'Add hotspot'}</button>
                  {editingHotspotId && <button className="btn" type="button" onClick={() => { setEditingHotspotId(null); setHotspotDraft({ label: '', targetSlug: '', x: 50, y: 50, active: true }); }}>Cancel edit</button>}
                </form>
                <div className="admin-list">
                  {hotspotItems.length === 0 ? <p className="admin-help">No hotspots for this visual.</p> : hotspotItems.map((h) => (
                    <div key={h.id} className="admin-list-row">
                      <div>
                        <strong>{h.label}</strong>
                        <div style={{ fontSize: 12 }}>Target: {h.targetSlug} · X {h.x}% · Y {h.y}% · {h.active ? 'Active' : 'Inactive'}</div>
                      </div>
                      <div className="admin-actions">
                        <button className="btn" type="button" onClick={() => { setEditingHotspotId(h.id); setHotspotDraft({ label: h.label, targetSlug: h.targetSlug, x: h.x, y: h.y, active: h.active }); }}>Edit</button>
                        <button className="btn btn-danger" type="button" onClick={async () => { if (!window.confirm('Delete this hotspot?')) return; await api.admin.deleteHotspot(h.id); await loadExperienceVisual(experienceVisualId); setError('Hotspot deleted.'); }}>Delete</button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}
          {tab === 'commerce' && (
            <div className="admin-overview-grid">
              <div className="admin-panel">
                <h2>Coupons</h2>
                <form
                  className="checkout-form"
                  onSubmit={async (e) => {
                    e.preventDefault();
                    const fd = new FormData(e.currentTarget);
                    const code = String(fd.get('code') || '').trim();
                    const type = String(fd.get('discountType') || 'PERCENT');
                    const value = Number(fd.get('value') || 0);
                     const minimumSubtotalRupees = Number(fd.get('minimumSubtotal') || 0);
                     const maximumDiscountRupees = Number(fd.get('maximumDiscount') || 0);
                     const usageLimitRaw = String(fd.get('usageLimit') || '').trim();
                    if (!code || !value) return;
                    try {
                      if (type === 'PERCENT' && (!Number.isInteger(value) || value > 100)) {
                        setError('Percentage coupon value must be a whole number from 1 to 100.');
                        return;
                      }
                      await api.admin.createCoupon({
                        code,
                        discountType: type,
                        value: type === 'FIXED' ? Math.round(value * 100) : value,
                        minimumSubtotal: Math.round(minimumSubtotalRupees * 100),
                        maximumDiscount: Math.round(maximumDiscountRupees * 100),
                        usageLimit: usageLimitRaw ? Math.max(1, Number(usageLimitRaw)) : null,
                        perCustomerUsageLimit: (() => { const raw = String(fd.get('perCustomerUsageLimit') || '').trim(); return raw ? Math.max(1, Number(raw)) : null; })(),
                        active: true,
                      });
                      setCouponItems(await api.admin.coupons());
                      (e.currentTarget as HTMLFormElement).reset();
                    } catch (err: any) {
                      setError(err.message);
                    }
                  }}
                >
                  <input
                    name="code"
                    className="field"
                    placeholder="Code"
                    required
                  />
                  <select name="discountType" className="select-field">
                    <option>PERCENT</option>
                    <option>FIXED</option>
                  </select>
                  <input
                    name="value"
                    className="field"
                    type="number"
                    min="0"
                    step="0.01"
                    placeholder="Value (₹ for fixed, % for percent)"
                    required
                  />
                  <input name="minimumSubtotal" className="field" type="number" min="0" step="0.01" placeholder="Min order ₹ (optional)" />
                  <input name="maximumDiscount" className="field" type="number" min="0" step="0.01" placeholder="Max discount ₹ (optional)" />
                  <input name="usageLimit" className="field" type="number" min="1" step="1" placeholder="Usage limit (optional)" />
                  <input name="perCustomerUsageLimit" className="field" type="number" min="1" step="1" placeholder="Per-customer limit (optional)" />
                  <button className="btn btn-orange" type="submit">
                    Add coupon
                  </button>
                </form>
                {couponItems.map((c) => (
                  <div className="admin-row" key={c.id}>
                    <span>
                      <strong>{c.code}</strong>
                      <small>
                        {c.discountType} {c.value} · used {c.usedCount || 0}/
                        {c.usageLimit ?? '∞'} · {c.perCustomerUsageLimit ? `${c.perCustomerUsageLimit}/customer` : 'no customer cap'} · {c.active ? 'Active' : 'Hidden'}
                      </small>
                    </span>
                    <button
                      onClick={async () => {
                        await api.admin.deleteCoupon(c.id);
                        setCouponItems(await api.admin.coupons());
                      }}
                    >
                      Disable
                    </button>
                  </div>
                ))}
              </div>
              <div className="admin-panel">
                <h2>Returns</h2>
                {returnItems.map((r) => (
                  <div className="admin-row" key={r.id}>
                    <span>
                      Return #{r.id}
                      <small>
                        Order {r.orderId} · {r.reason}
                      </small>
                    </span>
                    <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                      <select
                        value={r.status}
                        onChange={async (e) => {
                          const nextStatus = e.target.value;
                          await api.admin.updateReturn(r.id, {
                            status: nextStatus,
                            refundAmount: Number(r.refundAmount || 0),
                            refundStatus: r.refundStatus || 'PENDING',
                            adminNote: r.adminNote || '',
                          });
                          setReturnItems(await api.admin.returns());
                        }}
                      >
                        <option>PENDING</option>
                        <option>APPROVED</option>
                        <option>REJECTED</option>
                        <option>RECEIVED</option>
                        <option>COMPLETED</option>
                      </select>
                      <select
                        aria-label={`Refund status for return ${r.id}`}
                        value={r.refundStatus || 'PENDING'}
                        onChange={async (e) => {
                          await api.admin.updateReturn(r.id, {
                            status: r.status,
                            refundAmount: Number(r.refundAmount || 0),
                            refundStatus: e.target.value,
                            adminNote: r.adminNote || '',
                          });
                          setReturnItems(await api.admin.returns());
                        }}
                      >
                        <option value="NOT_REQUESTED">NOT_REQUESTED</option>
                        <option value="PENDING">PENDING</option>
                        <option value="PROCESSING">PROCESSING</option>
                        <option value="REFUNDED">REFUNDED</option>
                        <option value="FAILED">FAILED</option>
                      </select>
                      <input
                        className="field"
                        style={{ width: 120 }}
                        aria-label={`Refund amount in rupees for return ${r.id}`}
                        placeholder="₹0.00"
                        type="number"
                        min={0}
                        step={0.01}
                        value={(Number(r.refundAmount || 0) / 100).toFixed(2)}
                        onChange={e => setReturnItems(items => items.map(x => x.id === r.id ? { ...x, refundAmount: Math.round((Number(e.target.value) || 0) * 100) } : x))}
                        onBlur={async () => {
                          await api.admin.updateReturn(r.id, {
                            status: r.status,
                            refundAmount: Number(r.refundAmount || 0),
                            refundStatus: r.refundStatus || 'PENDING',
                            adminNote: r.adminNote || '',
                          });
                          setReturnItems(await api.admin.returns());
                        }}
                      />
                    </div>
                  </div>
                ))}
                {!returnItems.length && (
                  <p className="empty-state">No return requests.</p>
                )}
              </div>
              <div className="admin-panel">
                <h2>Stock alerts</h2>
                {stockAlerts.map((a) => (
                  <div className="admin-row" key={a.productId}>
                    <span>
                      {a.name}
                      <small>Available {a.available}</small>
                    </span>
                    <strong>LOW</strong>
                  </div>
                ))}
              </div>
              <div className="admin-panel">
                <h2>Back-in-stock subscriptions</h2>
                {stockSubscriptions.map((x) => (
                  <div className="admin-row" key={x.id}>
                    <span>
                      Customer {x.customerId}
                      <small>
                        Product {x.productId} · {x.active ? 'Active' : 'Not active'}
                      </small>
                    </span>
                  </div>
                ))}
                {!stockSubscriptions.length && (
                  <p className="empty-state">No subscriptions.</p>
                )}
              </div>
              <div className="admin-panel">
                <h2>Cart recovery</h2>
                {cartRecovery.map((x) => (
                  <div className="admin-row" key={x.customerId}>
                    <span>
                      Customer {x.customerId}
                      <small>
                        Last cart activity:{' '}
                        {x.lastTouchedAt
                          ? new Date(x.lastTouchedAt).toLocaleString('en-IN')
                          : '—'}
                      </small>
                    </span>
                    <strong>{x.recovered ? 'Recovered' : 'Pending'}</strong>
                  </div>
                ))}
                {!cartRecovery.length && (
                  <p className="empty-state">No recovery records.</p>
                )}
              </div>
            </div>
          )}
          {tab === 'custom' && (
            <div className="admin-list">
              {adminCustom.map((d) => (
                <div key={d.id}>
                  <span>
                    Design #{d.id} — {d.projectName}
                    <small>
                      Customer {d.customerId} · {d.requirements || 'No requirements'}{' '}
                      {d.referenceImageUrl && ' · ' + d.referenceImageUrl}
                    </small>
                  </span>
                  <select
                    value={d.status}
                    onChange={async (e) => {
                      await api.admin.customDesignStatus(d.id, e.target.value);
                      setAdminCustom(await api.admin.customDesign());
                    }}
                  >
                    <option>NEW</option>
                    <option>CONTACTED</option>
                    <option>IN_PROGRESS</option>
                    <option>COMPLETED</option>
                    <option>CLOSED</option>
                  </select>
                </div>
              ))}
            </div>
          )}
        </>
      )}
      <ProductModal
        isOpen={showForm}
        editing={editing}
        categories={categories}
        brands={brands}
        subcategories={subcategories}
        adminProductsLength={adminProducts.length}
        onClose={() => setShowForm(false)}
        onSaved={load}
        onError={(msg) => setError(msg)}
      />
    </main>
  );
}
