import React, { useEffect, useState } from 'react';
import { api, type Retailer, type RetailerInventory, type InventoryMovement, type RetailerSettlement } from '../../api';
import { Package, Truck, CheckCircle, AlertTriangle, RefreshCw, DollarSign, Clock, MapPin, Box, Search, ShieldCheck } from 'lucide-react';
import { inr } from '../../utils/format';

export default function RetailerPortal() {
    const [tab, setTab] = useState<'orders' | 'inventory' | 'movements' | 'settlements'>('orders');
    const [profile, setProfile] = useState<Retailer | null>(null);
    const [stats, setStats] = useState<any>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Orders state
    const [orders, setOrders] = useState<any[]>([]);
    const [packModalOrder, setPackModalOrder] = useState<string | null>(null);
    const [trackingInput, setTrackingInput] = useState('');
    const [courierInput, setCourierInput] = useState('Wolfe Local Express');
    const [rejectModalOrder, setRejectModalOrder] = useState<string | null>(null);
    const [rejectReason, setRejectReason] = useState('');

    // Inventory state
    const [inventory, setInventory] = useState<RetailerInventory[]>([]);
    const [invSearch, setInvSearch] = useState('');
    const [invPage, setInvPage] = useState(0);
    const [invTotal, setInvTotal] = useState(0);
    const [adjustModalSku, setAdjustModalSku] = useState<RetailerInventory | null>(null);
    const [adjustQtyInput, setAdjustQtyInput] = useState<number>(0);
    const [adjustReasonInput, setAdjustReasonInput] = useState('Stock replenishment');

    // Movements state
    const [movements, setMovements] = useState<InventoryMovement[]>([]);

    // Settlements state
    const [settlements, setSettlements] = useState<RetailerSettlement[]>([]);

    const loadProfileAndStats = async () => {
        try {
            const data = await api.retailer.me();
            setProfile(data.retailer);
            setStats(data.stats);
            setError(null);
        } catch (err: any) {
            setError(err.message || 'Failed to load retailer profile. Make sure you are logged in with partner credentials.');
        }
    };

    const loadOrders = async () => {
        try {
            const data = await api.retailer.orders();
            setOrders(data);
        } catch {}
    };

    const loadInventory = async (page = 0, query = invSearch) => {
        try {
            const res = await api.retailer.inventory(page, 20, query);
            setInventory(res.content || []);
            setInvTotal(res.totalElements || 0);
            setInvPage(page);
        } catch {}
    };

    const loadMovements = async () => {
        try {
            const res = await api.retailer.movements(0, 50);
            setMovements(res.content || []);
        } catch {}
    };

    const loadSettlements = async () => {
        try {
            const res = await api.retailer.settlements(0, 50);
            setSettlements(res.content || []);
        } catch {}
    };

    useEffect(() => {
        setLoading(true);
        loadProfileAndStats().finally(() => setLoading(false));
    }, []);

    useEffect(() => {
        if (tab === 'orders') loadOrders();
        else if (tab === 'inventory') loadInventory(0, invSearch);
        else if (tab === 'movements') loadMovements();
        else if (tab === 'settlements') loadSettlements();
    }, [tab]);

    const handleAccept = async (orderId: string) => {
        await api.retailer.accept(orderId);
        loadOrders();
        loadProfileAndStats();
    };

    const handlePack = async () => {
        if (!packModalOrder) return;
        await api.retailer.pack(packModalOrder, trackingInput, courierInput);
        setPackModalOrder(null);
        setTrackingInput('');
        loadOrders();
    };

    const handleReady = async (orderId: string) => {
        await api.retailer.ready(orderId);
        loadOrders();
    };

    const handleOutForDelivery = async (orderId: string) => {
        await api.retailer.outForDelivery(orderId);
        loadOrders();
    };

    const handleDeliver = async (orderId: string) => {
        await api.retailer.deliver(orderId);
        loadOrders();
        loadProfileAndStats();
    };

    const handleReject = async () => {
        if (!rejectModalOrder || !rejectReason.trim()) return;
        await api.retailer.reject(rejectModalOrder, rejectReason);
        setRejectModalOrder(null);
        setRejectReason('');
        loadOrders();
        loadProfileAndStats();
    };

    const handleAdjustStock = async () => {
        if (!adjustModalSku) return;
        await api.retailer.adjustStock(adjustModalSku.sku, adjustQtyInput, adjustReasonInput);
        setAdjustModalSku(null);
        loadInventory(invPage, invSearch);
        loadProfileAndStats();
    };

    if (loading) {
        return (
            <div className="container-w section text-center py-20">
                <RefreshCw className="w-8 h-8 animate-spin mx-auto text-amber-500 mb-4" />
                <p className="text-stone-400">Connecting to Wolfe Local Fulfillment Network...</p>
            </div>
        );
    }

    if (error || !profile) {
        return (
            <div className="container-w section max-w-xl mx-auto py-20 text-center">
                <ShieldCheck className="w-12 h-12 text-amber-500 mx-auto mb-4" />
                <h1 className="text-2xl font-serif text-stone-100 mb-2">Partner Portal Authentication</h1>
                <p className="text-stone-400 text-sm mb-6">{error || 'Please login with an authorized Wolfe Retailer Partner account.'}</p>
                <a href="/account" className="btn-solid inline-block">Go to Account Login</a>
            </div>
        );
    }

    return (
        <main className="container-w section py-8 min-h-screen">
            {/* Header / Partner Profile Banner */}
            <header className="bg-stone-900/90 border border-stone-800 rounded-xl p-6 mb-8 backdrop-blur">
                <div className="flex flex-wrap items-center justify-between gap-4">
                    <div>
                        <div className="flex items-center gap-3 mb-1">
                            <h1 className="text-2xl font-serif text-stone-100">{profile.name}</h1>
                            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-950/80 text-emerald-400 border border-emerald-800">
                                {profile.status}
                            </span>
                            <span className="px-2 py-0.5 rounded text-xs bg-stone-800 text-stone-300 border border-stone-700">
                                Partner #{profile.id}
                            </span>
                        </div>
                        <p className="text-xs text-stone-400 flex items-center gap-2">
                            <MapPin className="w-3.5 h-3.5 text-stone-500" />
                            {profile.address}, {profile.city}, {profile.pincode} • Service Radius: {profile.deliveryRadiusKm} km
                        </p>
                    </div>

                    <div className="flex items-center gap-4">
                        <div className="bg-stone-950/80 border border-stone-800 px-4 py-2 rounded-lg text-right">
                            <div className="text-[10px] uppercase tracking-wider text-stone-500">Total Net Payout</div>
                            <div className="text-base font-medium text-amber-400">{inr(stats?.totalEarningsPaise || 0)}</div>
                        </div>
                        <div className="bg-stone-950/80 border border-stone-800 px-4 py-2 rounded-lg text-right">
                            <div className="text-[10px] uppercase tracking-wider text-stone-500">Pending Orders</div>
                            <div className="text-base font-medium text-stone-200">{stats?.pendingOrdersCount || 0}</div>
                        </div>
                    </div>
                </div>
            </header>

            {/* Navigation Tabs */}
            <nav className="flex items-center gap-2 border-b border-stone-800 mb-6 pb-2">
                <button
                    onClick={() => setTab('orders')}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-2 ${
                        tab === 'orders' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-stone-400 hover:text-stone-200'
                    }`}
                >
                    <Package className="w-4 h-4" />
                    Assigned Orders
                    {orders.length > 0 && <span className="px-1.5 py-0.2 text-[10px] bg-amber-500 text-stone-950 font-bold rounded-full">{orders.length}</span>}
                </button>
                <button
                    onClick={() => setTab('inventory')}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-2 ${
                        tab === 'inventory' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-stone-400 hover:text-stone-200'
                    }`}
                >
                    <Box className="w-4 h-4" />
                    Local Inventory
                </button>
                <button
                    onClick={() => setTab('movements')}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-2 ${
                        tab === 'movements' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-stone-400 hover:text-stone-200'
                    }`}
                >
                    <Clock className="w-4 h-4" />
                    Audit Movement Ledger
                </button>
                <button
                    onClick={() => setTab('settlements')}
                    className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-2 ${
                        tab === 'settlements' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-stone-400 hover:text-stone-200'
                    }`}
                >
                    <DollarSign className="w-4 h-4" />
                    Settlements & Payouts
                </button>
            </nav>

            {/* TAB 1: ASSIGNED ORDERS */}
            {tab === 'orders' && (
                <section className="space-y-4">
                    {orders.length === 0 ? (
                        <div className="bg-stone-900/40 border border-stone-800 rounded-xl p-12 text-center text-stone-400">
                            <Package className="w-10 h-10 mx-auto text-stone-600 mb-3" />
                            <p className="font-medium text-stone-300">No active orders assigned currently.</p>
                            <p className="text-xs text-stone-500 mt-1">New customer orders in your delivery radius will automatically appear here with stock reserved.</p>
                        </div>
                    ) : (
                        orders.map(o => {
                            const fStatus = o.fulfillment?.status || o.assignment.status;
                            return (
                                <article key={o.orderId} className="bg-stone-900/70 border border-stone-800 rounded-xl p-6 hover:border-stone-700 transition">
                                    <div className="flex flex-wrap items-start justify-between gap-4 pb-4 border-b border-stone-800">
                                        <div>
                                            <div className="flex items-center gap-3">
                                                <h3 className="text-lg font-semibold text-stone-100">{o.orderId}</h3>
                                                <span className={`px-2.5 py-0.5 rounded text-xs font-medium border ${
                                                    fStatus === 'DELIVERED' ? 'bg-emerald-950 text-emerald-400 border-emerald-800' :
                                                    fStatus === 'OUT_FOR_DELIVERY' ? 'bg-blue-950 text-blue-400 border-blue-800' :
                                                    fStatus === 'PACKED' ? 'bg-purple-950 text-purple-400 border-purple-800' :
                                                    fStatus === 'ACCEPTED' ? 'bg-amber-950 text-amber-400 border-amber-800' :
                                                    'bg-stone-800 text-stone-300 border-stone-700'
                                                }`}>
                                                    {fStatus}
                                                </span>
                                            </div>
                                            <p className="text-xs text-stone-400 mt-1">
                                                Assigned on: {new Date(o.assignment.assignedAt).toLocaleString()} • Method: {o.shippingMethod}
                                            </p>
                                        </div>

                                        {o.settlement?.retailerPayableAmount && (
                                            <div className="text-right">
                                                <span className="text-xs text-stone-400">Your Payable: </span>
                                                <span className="text-base font-bold text-amber-400">{inr(o.settlement.retailerPayableAmount)}</span>
                                            </div>
                                        )}
                                    </div>

                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6 my-4">
                                        {/* Customer Delivery Details (Least-privilege) */}
                                        <div className="bg-stone-950/60 p-4 rounded-lg border border-stone-800/80">
                                            <h4 className="text-xs font-semibold uppercase tracking-wider text-stone-400 mb-2 flex items-center gap-1.5">
                                                <Truck className="w-3.5 h-3.5 text-amber-500" />
                                                Local Delivery Address
                                            </h4>
                                            <p className="text-sm text-stone-200 font-medium">{o.customerDelivery.name}</p>
                                            <p className="text-xs text-stone-400 mt-0.5">{o.customerDelivery.address}</p>
                                            <p className="text-xs text-stone-400">{o.customerDelivery.city} - {o.customerDelivery.pincode}</p>
                                            <p className="text-xs text-stone-300 mt-2 font-mono">Contact: {o.customerDelivery.phone}</p>
                                        </div>

                                        {/* Order Items & Variants */}
                                        <div className="bg-stone-950/60 p-4 rounded-lg border border-stone-800/80">
                                            <h4 className="text-xs font-semibold uppercase tracking-wider text-stone-400 mb-2">
                                                Items to Pack ({o.items?.length || 0})
                                            </h4>
                                            <ul className="space-y-2 text-xs">
                                                {o.items?.map((it: any) => (
                                                    <li key={it.id} className="flex items-center justify-between pb-1 border-b border-stone-800/50">
                                                        <div>
                                                            <span className="text-stone-200 font-medium">{it.productName}</span>
                                                            {it.variantTitle && <span className="text-stone-400 ml-1.5">({it.variantTitle})</span>}
                                                            <div className="text-[11px] text-stone-500 font-mono">SKU: {it.variantSku || 'BASE-SKU'}</div>
                                                        </div>
                                                        <span className="font-bold text-stone-200">x{it.quantity}</span>
                                                    </li>
                                                ))}
                                            </ul>
                                        </div>
                                    </div>

                                    {/* Action Workflow Buttons */}
                                    <div className="flex flex-wrap items-center justify-end gap-2 pt-3 border-t border-stone-800">
                                        {fStatus === 'ASSIGNED' && (
                                            <>
                                                <button
                                                    onClick={() => setRejectModalOrder(o.orderId)}
                                                    className="px-3 py-1.5 text-xs text-rose-400 border border-rose-900/60 hover:bg-rose-950/30 rounded"
                                                >
                                                    Reject Order
                                                </button>
                                                <button
                                                    onClick={() => handleAccept(o.orderId)}
                                                    className="btn-solid text-xs py-1.5 px-4"
                                                >
                                                    Accept & Start Fulfillment
                                                </button>
                                            </>
                                        )}
                                        {fStatus === 'ACCEPTED' && (
                                            <button
                                                onClick={() => { setPackModalOrder(o.orderId); setTrackingInput(`WLF-LOC-${Math.floor(100000 + Math.random() * 900000)}`); }}
                                                className="btn-solid text-xs py-1.5 px-4"
                                            >
                                                Mark Packed
                                            </button>
                                        )}
                                        {fStatus === 'PACKED' && (
                                            <button
                                                onClick={() => handleReady(o.orderId)}
                                                className="btn-solid text-xs py-1.5 px-4"
                                            >
                                                Ready for Dispatch
                                            </button>
                                        )}
                                        {fStatus === 'READY_FOR_DELIVERY' && (
                                            <button
                                                onClick={() => handleOutForDelivery(o.orderId)}
                                                className="btn-solid text-xs py-1.5 px-4 flex items-center gap-1.5"
                                            >
                                                <Truck className="w-3.5 h-3.5" />
                                                Out for Delivery
                                            </button>
                                        )}
                                        {fStatus === 'OUT_FOR_DELIVERY' && (
                                            <button
                                                onClick={() => handleDeliver(o.orderId)}
                                                className="px-4 py-1.5 text-xs bg-emerald-600 hover:bg-emerald-500 text-stone-950 font-bold rounded flex items-center gap-1.5"
                                            >
                                                <CheckCircle className="w-3.5 h-3.5" />
                                                Confirm Delivered
                                            </button>
                                        )}
                                        {fStatus === 'DELIVERED' && (
                                            <span className="text-xs text-emerald-400 font-medium flex items-center gap-1">
                                                <CheckCircle className="w-4 h-4" /> Delivered & Finalized
                                            </span>
                                        )}
                                    </div>
                                </article>
                            );
                        })
                    )}
                </section>
            )}

            {/* TAB 2: LOCAL INVENTORY */}
            {tab === 'inventory' && (
                <section className="bg-stone-900/70 border border-stone-800 rounded-xl p-6">
                    <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
                        <div>
                            <h2 className="text-lg font-serif text-stone-100">Partner Stock Matrix</h2>
                            <p className="text-xs text-stone-400">Manage your local branch inventory. Available Stock = Physical - Reserved.</p>
                        </div>

                        <div className="relative w-72">
                            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-stone-500" />
                            <input
                                type="text"
                                placeholder="Search SKU or Product..."
                                value={invSearch}
                                onChange={e => { setInvSearch(e.target.value); loadInventory(0, e.target.value); }}
                                className="w-full bg-stone-950 border border-stone-800 rounded-lg pl-9 pr-3 py-1.5 text-xs text-stone-200 focus:border-amber-500"
                            />
                        </div>
                    </div>

                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-xs text-stone-300">
                            <thead className="bg-stone-950/80 uppercase text-[10px] text-stone-400 border-b border-stone-800">
                                <tr>
                                    <th className="py-3 px-4">SKU / Item</th>
                                    <th className="py-3 px-4">Product ID</th>
                                    <th className="py-3 px-4">Physical Stock</th>
                                    <th className="py-3 px-4">Reserved Stock</th>
                                    <th className="py-3 px-4 font-bold text-amber-400">Available Stock</th>
                                    <th className="py-3 px-4">Status</th>
                                    <th className="py-3 px-4 text-right">Action</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-stone-800">
                                {inventory.map(item => (
                                    <tr key={item.id} className="hover:bg-stone-800/40 transition">
                                        <td className="py-3 px-4 font-mono font-medium text-stone-200">{item.sku}</td>
                                        <td className="py-3 px-4 text-stone-400">{item.productId} {item.variantId ? `(Var #${item.variantId})` : ''}</td>
                                        <td className="py-3 px-4 text-stone-300 font-semibold">{item.physicalStock}</td>
                                        <td className="py-3 px-4 text-amber-500/80">{item.reservedStock}</td>
                                        <td className="py-3 px-4 font-bold text-emerald-400 text-sm">{item.availableStock}</td>
                                        <td className="py-3 px-4">
                                            {item.availableStock <= item.lowStockThreshold ? (
                                                <span className="inline-flex items-center gap-1 text-[11px] text-amber-400 bg-amber-950/50 px-2 py-0.5 rounded border border-amber-800/60">
                                                    <AlertTriangle className="w-3 h-3" /> Low Stock
                                                </span>
                                            ) : (
                                                <span className="text-[11px] text-emerald-400 bg-emerald-950/50 px-2 py-0.5 rounded border border-emerald-800/60">
                                                    In Stock
                                                </span>
                                            )}
                                        </td>
                                        <td className="py-3 px-4 text-right">
                                            <button
                                                onClick={() => { setAdjustModalSku(item); setAdjustQtyInput(item.physicalStock); }}
                                                className="px-2.5 py-1 text-xs bg-stone-800 hover:bg-stone-700 text-stone-200 rounded border border-stone-700"
                                            >
                                                Adjust Stock
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </section>
            )}

            {/* TAB 3: AUDIT MOVEMENTS */}
            {tab === 'movements' && (
                <section className="bg-stone-900/70 border border-stone-800 rounded-xl p-6">
                    <h2 className="text-lg font-serif text-stone-100 mb-1">Traceable Stock Ledger</h2>
                    <p className="text-xs text-stone-400 mb-6">Every inventory delta (reservation, release, adjustment, fulfillment) is immutably logged.</p>

                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-xs text-stone-300">
                            <thead className="bg-stone-950/80 uppercase text-[10px] text-stone-400 border-b border-stone-800">
                                <tr>
                                    <th className="py-3 px-4">Timestamp</th>
                                    <th className="py-3 px-4">Type</th>
                                    <th className="py-3 px-4">SKU</th>
                                    <th className="py-3 px-4">Previous</th>
                                    <th className="py-3 px-4">Change</th>
                                    <th className="py-3 px-4">New Qty</th>
                                    <th className="py-3 px-4">Order / Reason</th>
                                    <th className="py-3 px-4">Actor</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-stone-800">
                                {movements.map(m => (
                                    <tr key={m.id} className="hover:bg-stone-800/40 transition">
                                        <td className="py-2.5 px-4 text-stone-400 text-[11px]">{new Date(m.createdAt).toLocaleString()}</td>
                                        <td className="py-2.5 px-4 font-semibold text-stone-200">
                                            <span className="px-1.5 py-0.5 rounded bg-stone-800 text-[10px] border border-stone-700">
                                                {m.movementType}
                                            </span>
                                        </td>
                                        <td className="py-2.5 px-4 font-mono">{m.sku}</td>
                                        <td className="py-2.5 px-4 text-stone-400">{m.previousQuantity}</td>
                                        <td className={`py-2.5 px-4 font-bold ${m.quantityChanged >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
                                            {m.quantityChanged >= 0 ? `+${m.quantityChanged}` : m.quantityChanged}
                                        </td>
                                        <td className="py-2.5 px-4 font-semibold text-stone-200">{m.newQuantity}</td>
                                        <td className="py-2.5 px-4 text-stone-300">
                                            {m.orderId && <span className="font-mono text-amber-400 mr-1.5">[{m.orderId}]</span>}
                                            {m.reason}
                                        </td>
                                        <td className="py-2.5 px-4 text-stone-500 font-mono text-[11px]">{m.createdBy}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </section>
            )}

            {/* TAB 4: SETTLEMENTS */}
            {tab === 'settlements' && (
                <section className="bg-stone-900/70 border border-stone-800 rounded-xl p-6">
                    <h2 className="text-lg font-serif text-stone-100 mb-1">Partner Settlements & Statements</h2>
                    <p className="text-xs text-stone-400 mb-6">Historical settlement calculations per order fulfillment.</p>

                    <div className="overflow-x-auto">
                        <table className="w-full text-left text-xs text-stone-300">
                            <thead className="bg-stone-950/80 uppercase text-[10px] text-stone-400 border-b border-stone-800">
                                <tr>
                                    <th className="py-3 px-4">Order ID</th>
                                    <th className="py-3 px-4">Gross Customer Total</th>
                                    <th className="py-3 px-4">Wolfe Margin</th>
                                    <th className="py-3 px-4 font-bold text-amber-400">Net Partner Payable</th>
                                    <th className="py-3 px-4">Status</th>
                                    <th className="py-3 px-4">Settled At / Ref</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-stone-800">
                                {settlements.map(s => (
                                    <tr key={s.id} className="hover:bg-stone-800/40 transition">
                                        <td className="py-3 px-4 font-mono font-medium text-stone-200">{s.orderId}</td>
                                        <td className="py-3 px-4 text-stone-400">{inr(s.grossAmount)}</td>
                                        <td className="py-3 px-4 text-stone-500">{inr(s.wolfeMarginAmount)}</td>
                                        <td className="py-3 px-4 font-bold text-amber-400 text-sm">{inr(s.retailerPayableAmount)}</td>
                                        <td className="py-3 px-4">
                                            <span className={`px-2 py-0.5 rounded text-[11px] font-medium border ${
                                                s.status === 'SETTLED' ? 'bg-emerald-950 text-emerald-400 border-emerald-800' :
                                                s.status === 'ELIGIBLE' ? 'bg-blue-950 text-blue-400 border-blue-800' :
                                                'bg-amber-950 text-amber-400 border-amber-800'
                                            }`}>
                                                {s.status}
                                            </span>
                                        </td>
                                        <td className="py-3 px-4 text-stone-400">
                                            {s.settledAt ? (
                                                <div>
                                                    <div>{new Date(s.settledAt).toLocaleDateString()}</div>
                                                    <span className="text-[10px] font-mono text-stone-500">Ref: {s.referenceNumber || 'N/A'}</span>
                                                </div>
                                            ) : 'Pending Settlement Cycle'}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </section>
            )}

            {/* MODAL: PACK ORDER */}
            {packModalOrder && (
                <div className="fixed inset-0 bg-black/80 flex items-center justify-center p-4 z-50">
                    <div className="bg-stone-900 border border-stone-800 rounded-xl p-6 max-w-md w-full">
                        <h3 className="text-lg font-serif text-stone-100 mb-2">Pack Order {packModalOrder}</h3>
                        <p className="text-xs text-stone-400 mb-4">Attach optional local courier or tracking code for audit trace.</p>

                        <div className="space-y-3 mb-6">
                            <div>
                                <label className="block text-xs text-stone-400 mb-1">Local Courier Service</label>
                                <input
                                    type="text"
                                    value={courierInput}
                                    onChange={e => setCourierInput(e.target.value)}
                                    className="w-full bg-stone-950 border border-stone-800 rounded px-3 py-2 text-xs text-stone-200"
                                />
                            </div>
                            <div>
                                <label className="block text-xs text-stone-400 mb-1">Dispatch / Tracking Reference</label>
                                <input
                                    type="text"
                                    value={trackingInput}
                                    onChange={e => setTrackingInput(e.target.value)}
                                    className="w-full bg-stone-950 border border-stone-800 rounded px-3 py-2 text-xs text-stone-200 font-mono"
                                />
                            </div>
                        </div>

                        <div className="flex justify-end gap-2">
                            <button onClick={() => setPackModalOrder(null)} className="px-4 py-1.5 text-xs text-stone-400">Cancel</button>
                            <button onClick={handlePack} className="btn-solid text-xs py-1.5 px-4">Confirm Packed</button>
                        </div>
                    </div>
                </div>
            )}

            {/* MODAL: REJECT ORDER */}
            {rejectModalOrder && (
                <div className="fixed inset-0 bg-black/80 flex items-center justify-center p-4 z-50">
                    <div className="bg-stone-900 border border-stone-800 rounded-xl p-6 max-w-md w-full">
                        <h3 className="text-lg font-serif text-stone-100 mb-2">Reject Order Assignment</h3>
                        <p className="text-xs text-stone-400 mb-4">Provide a clear reason (e.g. damaged stock, outside radius). Reserved stock will be released.</p>

                        <div className="mb-6">
                            <textarea
                                value={rejectReason}
                                onChange={e => setRejectReason(e.target.value)}
                                placeholder="State reason for rejecting assignment..."
                                rows={3}
                                className="w-full bg-stone-950 border border-stone-800 rounded p-3 text-xs text-stone-200"
                            />
                        </div>

                        <div className="flex justify-end gap-2">
                            <button onClick={() => setRejectModalOrder(null)} className="px-4 py-1.5 text-xs text-stone-400">Cancel</button>
                            <button onClick={handleReject} className="px-4 py-1.5 text-xs bg-rose-700 hover:bg-rose-600 text-white font-bold rounded">
                                Reject & Release Stock
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* MODAL: ADJUST STOCK */}
            {adjustModalSku && (
                <div className="fixed inset-0 bg-black/80 flex items-center justify-center p-4 z-50">
                    <div className="bg-stone-900 border border-stone-800 rounded-xl p-6 max-w-md w-full">
                        <h3 className="text-lg font-serif text-stone-100 mb-1">Adjust Stock for {adjustModalSku.sku}</h3>
                        <p className="text-xs text-stone-400 mb-4">Current Reserved: {adjustModalSku.reservedStock}. Physical stock cannot be below reserved.</p>

                        <div className="space-y-3 mb-6">
                            <div>
                                <label className="block text-xs text-stone-400 mb-1">New Total Physical Stock</label>
                                <input
                                    type="number"
                                    min={adjustModalSku.reservedStock}
                                    value={adjustQtyInput}
                                    onChange={e => setAdjustQtyInput(parseInt(e.target.value, 10) || 0)}
                                    className="w-full bg-stone-950 border border-stone-800 rounded px-3 py-2 text-xs text-stone-200 font-bold"
                                />
                            </div>
                            <div>
                                <label className="block text-xs text-stone-400 mb-1">Adjustment Reason</label>
                                <input
                                    type="text"
                                    value={adjustReasonInput}
                                    onChange={e => setAdjustReasonInput(e.target.value)}
                                    className="w-full bg-stone-950 border border-stone-800 rounded px-3 py-2 text-xs text-stone-200"
                                />
                            </div>
                        </div>

                        <div className="flex justify-end gap-2">
                            <button onClick={() => setAdjustModalSku(null)} className="px-4 py-1.5 text-xs text-stone-400">Cancel</button>
                            <button onClick={handleAdjustStock} className="btn-solid text-xs py-1.5 px-4">Save Adjustment</button>
                        </div>
                    </div>
                </div>
            )}
        </main>
    );
}
