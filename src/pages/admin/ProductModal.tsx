import React, { useState, useEffect, useRef } from 'react';
import { api, type Brand, type Subcategory } from '../../api';
import { money } from '../../utils/format';
import { X, Trash2, Plus, Layers } from 'lucide-react';

interface ProductModalProps {
  isOpen: boolean;
  editing: any | null;
  categories: any[];
  brands: Brand[];
  subcategories: Subcategory[];
  adminProductsLength: number;
  onClose: () => void;
  onSaved: () => Promise<void> | void;
  onError: (msg: string) => void;
}

const blankProduct = {
  slug: '',
  name: '',
  price: '',
  category: 'Hardware',
  subcategory: 'Handles',
  brandId: '',
  brandName: 'Wolfe Heritage',
  finish: 'Brushed Brass',
  material: 'Brass',
  color: 'Brass',
  style: 'Classic',
  dimensions: '',
  modelNumber: '',
  description: '',
  imageUrl: '',
  mediaUrls: '',
  attributesJson: '{}',
  active: true,
  featured: false,
  sortOrder: 10,
};

export default function ProductModal({
  isOpen,
  editing,
  categories,
  brands,
  subcategories,
  adminProductsLength,
  onClose,
  onSaved,
  onError,
}: ProductModalProps) {
  const [form, setForm] = useState<any>(blankProduct);
  const [variants, setVariants] = useState<any[]>([]);
  const [media, setMedia] = useState<any[]>([]);
  const [accessories, setAccessories] = useState<any[]>([]);
  const [mediaDraft, setMediaDraft] = useState<any>({ type: 'IMAGE', url: '', altText: '', sortOrder: 0 });
  const [accessoryDraft, setAccessoryDraft] = useState<any>({ name: '', type: 'HANDLE', overlayUrl: '', sku: '', price: '', x: 50, y: 50, scale: 1 });
  const [saving, setSaving] = useState(false);
  const [attributes, setAttributes] = useState<Record<string, string>>({});

  const [editingVariantId, setEditingVariantId] = useState<number | null>(null);
  const [variantDraft, setVariantDraft] = useState<any>({
    title: '',
    sku: '',
    color: 'Brass',
    material: 'Brass',
    size: 'Standard',
    finish: 'Brushed Brass',
    dimensions: '',
    priceOverride: '',
    stockQuantity: 50,
    imageUrl: '',
  });

  const modalRef = useRef<HTMLDivElement>(null);

  // Escape key handler
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  useEffect(() => {
    if (!isOpen) return;
    if (editing) {
      let parsedAttrs = {};
      try {
        if (editing.attributesJson) parsedAttrs = JSON.parse(editing.attributesJson);
      } catch {}
      setAttributes(parsedAttrs);

      setForm({
        slug: editing.slug,
        name: editing.name,
        price: editing.price,
        category: editing.category || 'Hardware',
        subcategory: editing.subcategory || 'Handles',
        brandId: editing.brandId || '',
        brandName: editing.brandName || 'Wolfe Heritage',
        finish: editing.finish || 'Brushed Brass',
        material: editing.material || 'Metal',
        color: editing.color || 'Brass',
        style: editing.style || 'Modern',
        dimensions: editing.dimensions || '',
        modelNumber: editing.modelNumber || '',
        description: editing.description || '',
        imageUrl: editing.imageUrl || '',
        mediaUrls: editing.mediaUrls || '',
        attributesJson: editing.attributesJson || '{}',
        active: editing.active !== false,
        featured: editing.featured === true,
        sortOrder: editing.sortOrder ?? 0,
      });

      api.admin
        .variants(editing.id)
        .then(setVariants)
        .catch(() => setVariants([]));
      api.admin.media(editing.id).then(setMedia).catch(() => setMedia([]));
      api.admin.accessories(editing.id).then(setAccessories).catch(() => setAccessories([]));
    } else {
      setAttributes({});
      setForm({
        ...blankProduct,
        sortOrder: (adminProductsLength + 1) * 10,
      });
      setVariants([]);
      setMedia([]);
      setAccessories([]);
    }
  }, [isOpen, editing, adminProductsLength]);

  if (!isOpen) return null;

  const handleAttrChange = (key: string, val: string) => {
    const next = { ...attributes, [key]: val };
    setAttributes(next);
    setForm((f: any) => ({ ...f, attributesJson: JSON.stringify(next) }));
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...form,
        price: Number(form.price),
        sortOrder: Number(form.sortOrder || 0),
        brandId: form.brandId ? Number(form.brandId) : null,
        attributesJson: JSON.stringify(attributes),
      };

      if (editing) {
        await api.admin.updateProduct(editing.id, payload);
      } else {
        await api.admin.createProduct(payload);
      }

      await onSaved();
      onClose();
    } catch (err: any) {
      onError(err.message || 'Could not save product');
    } finally {
      setSaving(false);
    }
  };

  const handleAddVariant = async () => {
    if (!editing) {
      onError('Please save the product first before adding variants');
      return;
    }
    if (!variantDraft.sku.trim()) {
      onError('Variant SKU is required');
      return;
    }
    try {
      const payload = {
        optionName: variantDraft.color ? 'Color / Size' : 'Variant',
        optionValue: variantDraft.title || `${variantDraft.color} ${variantDraft.size}`,
        title: variantDraft.title || `${variantDraft.color} ${variantDraft.size}`,
        sku: variantDraft.sku.trim().toUpperCase(),
        color: variantDraft.color,
        material: variantDraft.material,
        size: variantDraft.size,
        finish: variantDraft.finish,
        dimensions: variantDraft.dimensions,
        priceOverride: variantDraft.priceOverride ? Number(variantDraft.priceOverride) : null,
        stockQuantity: Number(variantDraft.stockQuantity || 50),
        imageUrl: variantDraft.imageUrl || form.imageUrl,
        active: true,
      };
      const created = await api.admin.createVariant(editing.id, payload);
      setVariants([...variants, created]);
      setVariantDraft({
        title: '',
        sku: '',
        color: form.color,
        material: form.material,
        size: 'Standard',
        finish: form.finish,
        dimensions: form.dimensions,
        priceOverride: '',
        stockQuantity: 50,
        imageUrl: '',
      });
    } catch (err: any) {
      onError(err.message || 'Could not create variant');
    }
  };

  const handleEditVariant = (v: any) => {
    setEditingVariantId(v.id);
    setVariantDraft({ title:v.title || v.optionValue || '', sku:v.sku || '', color:v.color || '', material:v.material || '', size:v.size || '', finish:v.finish || '', dimensions:v.dimensions || '', priceOverride:v.priceOverride ?? '', stockQuantity:v.stockQuantity ?? 0, imageUrl:v.imageUrl || '' });
  };

  const handleSaveVariant = async () => {
    if (!editing || !editingVariantId || !variantDraft.sku.trim()) { onError('Variant SKU is required'); return; }
    const payload = { optionName:variantDraft.color ? 'Color / Size' : 'Variant', optionValue:variantDraft.title || `${variantDraft.color} ${variantDraft.size}`, title:variantDraft.title || `${variantDraft.color} ${variantDraft.size}`, sku:variantDraft.sku.trim().toUpperCase(), color:variantDraft.color, material:variantDraft.material, size:variantDraft.size, finish:variantDraft.finish, dimensions:variantDraft.dimensions, priceOverride:variantDraft.priceOverride === '' ? null : Number(variantDraft.priceOverride), stockQuantity:Number(variantDraft.stockQuantity || 0), imageUrl:variantDraft.imageUrl || form.imageUrl, active:true };
    try { const updated=await api.admin.updateVariant(editing.id,editingVariantId,payload); setVariants(variants.map(v=>v.id===editingVariantId?updated:v)); setEditingVariantId(null); } catch(err:any){ onError(err.message || 'Could not update variant'); }
  };

  const saveMedia = async () => {
    if (!editing || !mediaDraft.url.trim()) return;
    try {
      const created = await api.admin.createMedia(editing.id, { ...mediaDraft, url: mediaDraft.url.trim(), sortOrder: Number(mediaDraft.sortOrder || 0), active: true });
      setMedia([...media, created]); setMediaDraft({ type: 'IMAGE', url: '', altText: '', sortOrder: media.length });
    } catch (err:any) { onError(err.message || 'Could not create media'); }
  };
  const editMedia = async (item:any) => {
    if (!editing) return;
    const url=window.prompt('Media URL', item.url); if (!url?.trim()) return;
    const type=window.prompt('Type (IMAGE/VIDEO/360)', item.type) || item.type;
    const altText=(window.prompt('Alt text', item.altText || '') ?? item.altText) || '';
    try { const updated=await api.admin.updateMedia(editing.id,item.id,{type,url:url.trim(),altText,sortOrder:item.sortOrder,active:item.active}); setMedia(media.map(m=>m.id===item.id?updated:m)); }
    catch(err:any){ onError(err.message || 'Could not update media'); }
  };
  const deleteMedia = async (mediaId:number) => {
    if (!editing) return;
    try { await api.admin.deleteMedia(editing.id, mediaId); setMedia(media.filter(m=>m.id!==mediaId)); }
    catch(err:any){ onError(err.message || 'Could not delete media'); }
  };
  const saveAccessory = async () => {
    if (!editing || !accessoryDraft.name.trim() || !accessoryDraft.overlayUrl.trim()) return;
    const body={...accessoryDraft,name:accessoryDraft.name.trim(),overlayUrl:accessoryDraft.overlayUrl.trim(),price:accessoryDraft.price===''?null:Number(accessoryDraft.price),x:Number(accessoryDraft.x||50),y:Number(accessoryDraft.y||50),scale:Number(accessoryDraft.scale||1),active:true};
    try { const created=await api.admin.createAccessory(editing.id,body); setAccessories([...accessories,created]); setAccessoryDraft({name:'',type:'HANDLE',overlayUrl:'',sku:'',price:'',x:50,y:50,scale:1}); }
    catch(err:any){ onError(err.message || 'Could not create accessory'); }
  };
  const editAccessory = async (item:any) => {
    if (!editing) return;
    const name=window.prompt('Accessory name',item.name); if(!name?.trim()) return;
    const overlayUrl=window.prompt('Overlay URL',item.overlayUrl); if(!overlayUrl?.trim()) return;
    try { const updated=await api.admin.updateAccessory(editing.id,item.id,{name:name.trim(),type:item.type,overlayUrl:overlayUrl.trim(),sku:item.sku||'',price:item.price,x:item.x,y:item.y,scale:item.scale,active:item.active}); setAccessories(accessories.map(a=>a.id===item.id?updated:a)); }
    catch(err:any){ onError(err.message || 'Could not update accessory'); }
  };
  const deleteAccessory = async (id:number) => {
    if (!editing) return;
    try { await api.admin.deleteAccessory(editing.id,id); setAccessories(accessories.filter(a=>a.id!==id)); }
    catch(err:any){ onError(err.message || 'Could not delete accessory'); }
  };

  const handleDeleteVariant = async (variantId: number) => {
    if (!editing) return;
    try {
      await api.admin.deleteVariant(editing.id, variantId);
      setVariants(variants.filter((v) => v.id !== variantId));
    } catch (err: any) {
      onError(err.message || 'Could not delete variant');
    }
  };

  const currentSubcats = subcategories.filter(
    (sc) => !form.category || sc.categoryName.toLowerCase() === form.category.toLowerCase()
  );

  return (
    <div className="cart-drawer-backdrop" onClick={onClose} style={{ zIndex: 1100 }}>
      <div
        ref={modalRef}
        className="modal-content"
        onClick={(e) => e.stopPropagation()}
        style={{
          background: 'var(--color-bg, #fff)',
          padding: '28px',
          borderRadius: '8px',
          maxWidth: '850px',
          width: '95%',
          maxHeight: '90vh',
          overflowY: 'auto',
          boxShadow: '0 20px 40px rgba(0,0,0,0.2)',
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <div>
            <p className="eyebrow">Catalog Management</p>
            <h2>{editing ? `Edit Product: ${editing.name}` : 'New Catalog Product'}</h2>
          </div>
          <button type="button" onClick={onClose} className="icon-btn" aria-label="Close modal">
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSave}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '16px' }}>
            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Product Name *</label>
              <input
                className="field"
                required
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                placeholder="e.g. Arc Pull Handle"
              />
            </div>

            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Slug *</label>
              <input
                className="field"
                required
                value={form.slug}
                onChange={(e) => setForm({ ...form, slug: e.target.value })}
                placeholder="e.g. arc-pull-handle"
              />
            </div>

            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Category *</label>
              <select
                className="field"
                value={form.category}
                onChange={(e) => setForm({ ...form, category: e.target.value })}
              >
                {categories.map((c: any) => (
                  <option key={c.name || c} value={c.name || c}>
                    {c.name || c}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Subcategory</label>
              <select
                className="field"
                value={form.subcategory}
                onChange={(e) => setForm({ ...form, subcategory: e.target.value })}
              >
                <option value="">Select Subcategory</option>
                {currentSubcats.map((sc) => (
                  <option key={sc.id} value={sc.name}>
                    {sc.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Brand / Manufacturer</label>
              <select
                className="field"
                value={form.brandId}
                onChange={(e) => {
                  const b = brands.find((x) => String(x.id) === e.target.value);
                  setForm({ ...form, brandId: e.target.value, brandName: b ? b.name : '' });
                }}
              >
                <option value="">Select Brand</option>
                {brands.map((b) => (
                  <option key={b.id} value={b.id}>
                    {b.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Base Price (₹) *</label>
              <input
                className="field"
                type="number"
                step="0.01"
                required
                value={form.price}
                onChange={(e) => setForm({ ...form, price: e.target.value })}
                placeholder="4200"
              />
            </div>

            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Finish</label>
              <input
                className="field"
                value={form.finish}
                onChange={(e) => setForm({ ...form, finish: e.target.value })}
                placeholder="Brushed Brass"
              />
            </div>

            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Material</label>
              <input
                className="field"
                value={form.material}
                onChange={(e) => setForm({ ...form, material: e.target.value })}
                placeholder="Brass / Hardwood / HPL / Steel"
              />
            </div>

            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Color</label>
              <input
                className="field"
                value={form.color}
                onChange={(e) => setForm({ ...form, color: e.target.value })}
                placeholder="Brass / Black / Silver"
              />
            </div>

            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Dimensions / Size</label>
              <input
                className="field"
                value={form.dimensions}
                onChange={(e) => setForm({ ...form, dimensions: e.target.value })}
                placeholder="160mm x 35mm / 8ft x 4ft"
              />
            </div>

            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Main Image URL</label>
              <input
                className="field"
                value={form.imageUrl}
                onChange={(e) => setForm({ ...form, imageUrl: e.target.value })}
                placeholder="/catalog/brass-01.jpg"
              />
            </div>

            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Sort Order</label>
              <input
                className="field"
                type="number"
                value={form.sortOrder}
                onChange={(e) => setForm({ ...form, sortOrder: Number(e.target.value) })}
              />
            </div>
          </div>

          <div style={{ marginTop: '16px' }}>
            <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>Description</label>
            <textarea
              className="field textarea"
              rows={3}
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              placeholder="Architectural product description..."
            />
          </div>

          {/* Category-specific specifications */}
          <div
            style={{
              margin: '20px 0',
              padding: '16px',
              background: 'var(--color-bg-subtle, #f9f8f6)',
              borderRadius: '6px',
              border: '1px solid var(--color-border, #eee)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 600, marginBottom: '12px' }}>
              <Layers size={16} /> Category Specifications ({form.category})
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px' }}>
              {form.category === 'Plywood' && (
                <>
                  <div>
                    <label style={{ fontSize: '0.75rem' }}>Grade</label>
                    <input
                      className="field"
                      value={attributes.grade || ''}
                      onChange={(e) => handleAttrChange('grade', e.target.value)}
                      placeholder="e.g. BWP IS:710 / Calibrated"
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.75rem' }}>Thickness</label>
                    <input
                      className="field"
                      value={attributes.thickness || ''}
                      onChange={(e) => handleAttrChange('thickness', e.target.value)}
                      placeholder="e.g. 18mm / 16mm / 12mm"
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.75rem' }}>Sheet Size</label>
                    <input
                      className="field"
                      value={attributes.sheetSize || ''}
                      onChange={(e) => handleAttrChange('sheetSize', e.target.value)}
                      placeholder="e.g. 8x4 ft"
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.75rem' }}>Core Wood</label>
                    <input
                      className="field"
                      value={attributes.core || ''}
                      onChange={(e) => handleAttrChange('core', e.target.value)}
                      placeholder="e.g. 100% Gurjan Hardwood"
                    />
                  </div>
                </>
              )}

              {form.category === 'Laminates' && (
                <>
                  <div>
                    <label style={{ fontSize: '0.75rem' }}>Design / Pattern</label>
                    <input
                      className="field"
                      value={attributes.pattern || ''}
                      onChange={(e) => handleAttrChange('pattern', e.target.value)}
                      placeholder="e.g. Fluted Oak / Metallic Foil"
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.75rem' }}>Texture</label>
                    <input
                      className="field"
                      value={attributes.texture || ''}
                      onChange={(e) => handleAttrChange('texture', e.target.value)}
                      placeholder="e.g. High Gloss / Matte Suede / Fluted"
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.75rem' }}>Sheet Thickness</label>
                    <input
                      className="field"
                      value={attributes.thickness || ''}
                      onChange={(e) => handleAttrChange('thickness', e.target.value)}
                      placeholder="e.g. 1.0mm / 0.8mm"
                    />
                  </div>
                </>
              )}

              {form.category === 'Kitchen Accessories' && (
                <>
                  <div>
                    <label style={{ fontSize: '0.75rem' }}>Load Capacity</label>
                    <input
                      className="field"
                      value={attributes.loadCapacity || ''}
                      onChange={(e) => handleAttrChange('loadCapacity', e.target.value)}
                      placeholder="e.g. 40kg / 65kg"
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.75rem' }}>Carcass / Drawer Length</label>
                    <input
                      className="field"
                      value={attributes.nominalLength || ''}
                      onChange={(e) => handleAttrChange('nominalLength', e.target.value)}
                      placeholder="e.g. 500mm / 900mm carcass"
                    />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.75rem' }}>Runner / Hinge Type</label>
                    <input
                      className="field"
                      value={attributes.runnerType || ''}
                      onChange={(e) => handleAttrChange('runnerType', e.target.value)}
                      placeholder="e.g. Soft-Close Synchronized"
                    />
                  </div>
                </>
              )}

              <div>
                <label style={{ fontSize: '0.75rem' }}>Warranty</label>
                <input
                  className="field"
                  value={attributes.warranty || ''}
                  onChange={(e) => handleAttrChange('warranty', e.target.value)}
                  placeholder="e.g. 10 Years / Lifetime"
                />
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '20px', margin: '16px 0' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={form.active}
                onChange={(e) => setForm({ ...form, active: e.target.checked })}
              />
              Active on Storefront
            </label>
            <label style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={form.featured}
                onChange={(e) => setForm({ ...form, featured: e.target.checked })}
              />
              Featured Piece
            </label>
          </div>

          <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end', marginTop: '24px' }}>
            <button type="button" onClick={onClose} className="btn btn-light">
              Cancel
            </button>
            <button type="submit" disabled={saving} className="btn btn-orange">
              {saving ? 'Saving…' : editing ? 'Update Product' : 'Create Product'}
            </button>
          </div>
        </form>

        {/* Product Media & Accessories */}
        {editing && (
          <div style={{ marginTop: '36px', borderTop: '1px solid var(--color-border, #eee)', paddingTop: '24px' }}>
            <h3>Product Media</h3>
            <div className="admin-list" style={{ marginBottom: 14 }}>
              {media.map((m) => <div className="admin-row" key={m.id}><span><strong>{m.type}</strong><small>{m.url}{m.altText ? ` · ${m.altText}` : ''}</small></span><button type="button" className="btn btn-light" onClick={()=>editMedia(m)}>Edit</button><button type="button" className="icon-btn" onClick={()=>deleteMedia(m.id)} aria-label={`Delete media ${m.id}`}><Trash2 size={14} color="#c62828"/></button></div>)}
              {!media.length && <p className="empty-state">No managed media yet.</p>}
            </div>
            <div style={{display:'grid',gridTemplateColumns:'120px 1fr 1fr 90px auto',gap:8}}>
              <select className="select-field" value={mediaDraft.type} onChange={e=>setMediaDraft({...mediaDraft,type:e.target.value})}><option>IMAGE</option><option>VIDEO</option><option>360</option></select>
              <input className="field" placeholder="Media URL" value={mediaDraft.url} onChange={e=>setMediaDraft({...mediaDraft,url:e.target.value})}/>
              <input className="field" placeholder="Alt text" value={mediaDraft.altText} onChange={e=>setMediaDraft({...mediaDraft,altText:e.target.value})}/>
              <input className="field" type="number" min="0" value={mediaDraft.sortOrder} onChange={e=>setMediaDraft({...mediaDraft,sortOrder:e.target.value})}/>
              <button type="button" className="btn btn-orange" onClick={saveMedia}>Add media</button>
            </div>

            <h3 style={{marginTop:28}}>Configurator Accessories</h3>
            <div className="admin-list" style={{ marginBottom: 14 }}>
              {accessories.map((a)=><div className="admin-row" key={a.id}><span><strong>{a.name}</strong><small>{a.type} · {a.sku || 'No SKU'} · {a.overlayUrl}</small></span><button type="button" className="btn btn-light" onClick={()=>editAccessory(a)}>Edit</button><button type="button" className="icon-btn" onClick={()=>deleteAccessory(a.id)} aria-label={`Delete accessory ${a.id}`}><Trash2 size={14} color="#c62828"/></button></div>)}
              {!accessories.length && <p className="empty-state">No configurator accessories yet.</p>}
            </div>
            <div style={{display:'grid',gridTemplateColumns:'1fr 120px 1fr 120px 90px 90px 90px auto',gap:8}}>
              <input className="field" placeholder="Name" value={accessoryDraft.name} onChange={e=>setAccessoryDraft({...accessoryDraft,name:e.target.value})}/>
              <input className="field" placeholder="Type" value={accessoryDraft.type} onChange={e=>setAccessoryDraft({...accessoryDraft,type:e.target.value})}/>
              <input className="field" placeholder="Overlay URL" value={accessoryDraft.overlayUrl} onChange={e=>setAccessoryDraft({...accessoryDraft,overlayUrl:e.target.value})}/>
              <input className="field" placeholder="SKU" value={accessoryDraft.sku} onChange={e=>setAccessoryDraft({...accessoryDraft,sku:e.target.value})}/>
              <input className="field" type="number" placeholder="Price" value={accessoryDraft.price} onChange={e=>setAccessoryDraft({...accessoryDraft,price:e.target.value})}/>
              <input className="field" type="number" placeholder="X" value={accessoryDraft.x} onChange={e=>setAccessoryDraft({...accessoryDraft,x:e.target.value})}/>
              <input className="field" type="number" placeholder="Y" value={accessoryDraft.y} onChange={e=>setAccessoryDraft({...accessoryDraft,y:e.target.value})}/>
              <button type="button" className="btn btn-orange" onClick={saveAccessory}>Add</button>
            </div>
          </div>
        )}

        {/* Product Variants Matrix Section */}
        {editing && (
          <div style={{ marginTop: '36px', borderTop: '1px solid var(--color-border, #eee)', paddingTop: '24px' }}>
            <h3>Product Variants Matrix</h3>
            <p style={{ color: 'var(--color-muted)', fontSize: '0.85rem', marginBottom: '16px' }}>
              Manage multi-attribute SKU variations (Color, Size, Finish, Price Overrides, Stock).
            </p>

            {variants.length > 0 ? (
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem', marginBottom: '20px' }}>
                <thead>
                  <tr style={{ background: 'var(--color-bg-subtle, #f9f8f6)', textAlign: 'left' }}>
                    <th style={{ padding: '8px' }}>SKU</th>
                    <th style={{ padding: '8px' }}>Title / Spec</th>
                    <th style={{ padding: '8px' }}>Color</th>
                    <th style={{ padding: '8px' }}>Size</th>
                    <th style={{ padding: '8px' }}>Finish</th>
                    <th style={{ padding: '8px' }}>Price</th>
                    <th style={{ padding: '8px' }}>Stock</th>
                    <th style={{ padding: '8px' }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {variants.map((v) => (
                    <tr key={v.id} style={{ borderBottom: '1px solid #eee' }}>
                      <td style={{ padding: '8px', fontWeight: 600 }}>{v.sku}</td>
                      <td style={{ padding: '8px' }}>{v.title || v.optionValue}</td>
                      <td style={{ padding: '8px' }}>{v.color || '—'}</td>
                      <td style={{ padding: '8px' }}>{v.size || '—'}</td>
                      <td style={{ padding: '8px' }}>{v.finish || '—'}</td>
                      <td style={{ padding: '8px' }}>{money(v.price || v.priceOverride || form.price)}</td>
                      <td style={{ padding: '8px' }}>{v.stockQuantity ?? 50}</td>
                      <td style={{ padding: '8px' }}>
                        <button type="button" onClick={() => handleEditVariant(v)} className="icon-btn" title="Edit variant">
                          <Layers size={14} />
                        </button>
                        <button
                          type="button"
                          onClick={() => handleDeleteVariant(v.id)}
                          className="icon-btn"
                          title="Delete variant"
                        >
                          <Trash2 size={14} color="#c62828" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p style={{ color: 'var(--color-muted)', fontSize: '0.85rem' }}>No custom variants defined yet.</p>
            )}

            {/* Add Variant Form */}
            <div style={{ background: 'var(--color-bg-subtle, #fafafa)', padding: '16px', borderRadius: '6px' }}>
              <strong style={{ fontSize: '0.85rem' }}>Add Variant</strong>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '10px', marginTop: '10px' }}>
                <input
                  className="field"
                  value={variantDraft.sku}
                  onChange={(e) => setVariantDraft({ ...variantDraft, sku: e.target.value })}
                  placeholder="SKU *"
                />
                <input
                  className="field"
                  value={variantDraft.title}
                  onChange={(e) => setVariantDraft({ ...variantDraft, title: e.target.value })}
                  placeholder="Title / Option Value"
                />
                <input
                  className="field"
                  value={variantDraft.color}
                  onChange={(e) => setVariantDraft({ ...variantDraft, color: e.target.value })}
                  placeholder="Color"
                />
                <input
                  className="field"
                  value={variantDraft.size}
                  onChange={(e) => setVariantDraft({ ...variantDraft, size: e.target.value })}
                  placeholder="Size / Thickness"
                />
                <input
                  className="field"
                  value={variantDraft.finish}
                  onChange={(e) => setVariantDraft({ ...variantDraft, finish: e.target.value })}
                  placeholder="Finish"
                />
                <input
                  className="field"
                  type="number"
                  value={variantDraft.priceOverride}
                  onChange={(e) => setVariantDraft({ ...variantDraft, priceOverride: e.target.value })}
                  placeholder="Price (₹)"
                />
                <input
                  className="field"
                  type="number"
                  value={variantDraft.stockQuantity}
                  onChange={(e) => setVariantDraft({ ...variantDraft, stockQuantity: Number(e.target.value) })}
                  placeholder="Stock"
                />
                {editingVariantId && <button type="button" onClick={() => setEditingVariantId(null)} className="btn btn-light">Cancel Edit</button>}
                <button
                  type="button"
                  onClick={editingVariantId ? handleSaveVariant : handleAddVariant}
                  className="btn btn-orange"
                  style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '4px' }}
                >
                  {editingVariantId ? <Layers size={16} /> : <Plus size={16} />} {editingVariantId ? 'Save Changes' : 'Add'}
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
