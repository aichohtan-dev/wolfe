export type Product = {
    id: string;
    serverId?: number;
    name: string;
    category: string;
    subcategory?: string;
    brandName?: string;
    price: number;
    image: string;
    description: string;
    finish: string;
    material: string;
    color: string;
    style: string;
    dimensions?: string;
    attributesJson?: string;
    featured: boolean;
    media: string[];
    variants?: any[];
};

export const products: Product[] = [
    // 1. HARDWARE
    {
        id: 'w-01',
        name: 'Arc Pull Handle',
        category: 'Hardware',
        subcategory: 'Handles',
        brandName: 'Wolfe Heritage',
        price: 4200,
        image: '/catalog/brass-01.jpg',
        description: 'A quiet architectural pull with a softly considered profile.',
        finish: 'Brushed Brass',
        material: 'Brass',
        color: 'Brass',
        style: 'Classic',
        dimensions: '160mm x 35mm',
        attributesJson: '{"mounting":"Rear Bolt","centerToCenter":"128mm / 160mm / 224mm"}',
        featured: true,
        media: ['/catalog/brass-01.jpg']
    },
    {
        id: 'w-02',
        name: 'Noir Round Knob',
        category: 'Hardware',
        subcategory: 'Knobs',
        brandName: 'Wolfe Heritage',
        price: 4600,
        image: '/catalog/brass-02.jpg',
        description: 'Compact, tactile and understated for modern cabinetry.',
        finish: 'Matte Black',
        material: 'Brass',
        color: 'Black',
        style: 'Modern',
        dimensions: '32mm x 28mm',
        attributesJson: '{"diameter":"32mm","projection":"28mm"}',
        featured: false,
        media: ['/catalog/brass-02.jpg']
    },
    {
        id: 'w-03',
        name: 'Linear Cabinet Pull',
        category: 'Hardware',
        subcategory: 'Handles',
        brandName: 'Wolfe Heritage',
        price: 5100,
        image: '/catalog/brass-03.jpg',
        description: 'Minimal geometry designed for contemporary interiors.',
        finish: 'Satin Nickel',
        material: 'Brass',
        color: 'Silver',
        style: 'Contemporary',
        dimensions: '192mm x 30mm',
        attributesJson: '{"centerToCenter":"192mm","weight":"340g"}',
        featured: false,
        media: ['/catalog/brass-03.jpg']
    },
    {
        id: 'w-04',
        name: 'Atelier Sculptural Hook',
        category: 'Hardware',
        subcategory: 'Hooks',
        brandName: 'Wolfe Heritage',
        price: 4400,
        image: '/catalog/brass-04.jpg',
        description: 'A sculptural wall hook that works beautifully in multiples.',
        finish: 'Antique Brass',
        material: 'Brass',
        color: 'Bronze',
        style: 'Sculptural',
        dimensions: '120mm x 45mm',
        attributesJson: '{"weightCapacity":"15kg"}',
        featured: false,
        media: ['/catalog/brass-04.jpg']
    },
    {
        id: 'w-05',
        name: 'Edge Architectural Cup Pull',
        category: 'Hardware',
        subcategory: 'Handles',
        brandName: 'Wolfe Heritage',
        price: 5600,
        image: '/catalog/brass-05.jpg',
        description: 'A refined cup pull with a strong but restrained silhouette.',
        finish: 'Bronze',
        material: 'Brass',
        color: 'Bronze',
        style: 'Refined',
        dimensions: '96mm x 40mm',
        attributesJson: '{"gripDepth":"20mm"}',
        featured: false,
        media: ['/catalog/brass-05.jpg']
    },
    {
        id: 'w-06',
        name: 'Studio Minimalist Knob',
        category: 'Hardware',
        subcategory: 'Knobs',
        brandName: 'Wolfe Heritage',
        price: 4900,
        image: '/catalog/brass-06.jpg',
        description: 'A versatile circular form for kitchens, wardrobes and furniture.',
        finish: 'Polished Chrome',
        material: 'Brass',
        color: 'Silver',
        style: 'Modern',
        dimensions: '30mm x 25mm',
        attributesJson: '{"diameter":"30mm"}',
        featured: false,
        media: ['/catalog/brass-06.jpg']
    },
    {
        id: 'w-07',
        name: 'Mortise Door Lock Set',
        category: 'Hardware',
        subcategory: 'Locks',
        brandName: 'Godrej',
        price: 5800,
        image: '/catalog/brass-01.jpg',
        description: 'High-security European standard mortise lock body with brass double cylinder and keys.',
        finish: 'Satin Stainless',
        material: 'Stainless Steel',
        color: 'Silver',
        style: 'Contemporary',
        dimensions: '60mm Backset',
        attributesJson: '{"backset":"60mm","centerDistance":"85mm"}',
        featured: false,
        media: ['/catalog/brass-01.jpg']
    },
    {
        id: 'w-08',
        name: 'Concealed Soft-Close Cabinet Hinge',
        category: 'Hardware',
        subcategory: 'Hinges',
        brandName: 'Blum',
        price: 2800,
        image: '/catalog/brass-02.jpg',
        description: 'Clip-on 3D adjustable concealed hydraulic hinge for silent cabinet door operation.',
        finish: 'Nickel Plated',
        material: 'Steel',
        color: 'Silver',
        style: 'Precision',
        dimensions: '110 Degree Opening',
        attributesJson: '{"openingAngle":"110°","overlay":"Full Overlay"}',
        featured: false,
        media: ['/catalog/brass-02.jpg']
    },

    // 2. PLYWOOD
    {
        id: 'w-09',
        name: 'Marine Gold BWP Waterproof Plywood',
        category: 'Plywood',
        subcategory: 'Marine / BWP Ply',
        brandName: 'Greenply',
        price: 4800,
        image: '/catalog/brass-03.jpg',
        description: '100% Boiling Water Proof (IS:710) marine plywood bonded with unextended synthetic resin.',
        finish: 'Calibrated Sanded',
        material: 'Hardwood',
        color: 'Wood',
        style: 'Structural',
        dimensions: '8ft x 4ft',
        attributesJson: '{"grade":"BWP IS:710","thickness":"18mm","sheetSize":"8x4 ft","core":"100% Gurjan Hardwood","warranty":"25 Years"}',
        featured: true,
        media: ['/catalog/brass-03.jpg']
    },
    {
        id: 'w-10',
        name: 'Calibrated Club Prime Hardwood Plywood',
        category: 'Plywood',
        subcategory: 'Calibrated Ply',
        brandName: 'CenturyPly',
        price: 4500,
        image: '/catalog/brass-04.jpg',
        description: 'Zero-gap quad-press calibrated ply for precision CNC routing and luxury modular furniture.',
        finish: 'Quad-Press Calibrated',
        material: 'Hardwood',
        color: 'Wood',
        style: 'Architectural',
        dimensions: '8ft x 4ft',
        attributesJson: '{"grade":"BWR / Calibrated","thickness":"16mm","sheetSize":"8x4 ft","tolerance":"+/- 0.2mm","warranty":"20 Years"}',
        featured: false,
        media: ['/catalog/brass-04.jpg']
    },
    {
        id: 'w-11',
        name: 'FlexiPly Flexible Curvature Plywood',
        category: 'Plywood',
        subcategory: 'Flexible Ply',
        brandName: 'Greenply',
        price: 3200,
        image: '/catalog/brass-05.jpg',
        description: 'Engineered flexible structural plywood for seamless rounded columns, arches, and organic contours.',
        finish: 'Smooth Raw',
        material: 'Hardwood',
        color: 'Wood',
        style: 'Curved Form',
        dimensions: '8ft x 4ft',
        attributesJson: '{"grade":"Flexible MR","thickness":"6mm / 8mm","bendingRadius":"25mm"}',
        featured: false,
        media: ['/catalog/brass-05.jpg']
    },

    // 3. LAMINATES
    {
        id: 'w-12',
        name: 'Architectural Fluted Oak Decorative Laminate 1mm',
        category: 'Laminates',
        subcategory: 'Textured & Fluted',
        brandName: 'Merino',
        price: 3900,
        image: '/catalog/brass-06.jpg',
        description: 'Tactile fluted linear oak architectural laminate sheet for wall paneling and cabinetry.',
        finish: 'Fluted 3D Texture',
        material: 'High Pressure Laminate',
        color: 'Wood',
        style: 'Architectural',
        dimensions: '8ft x 4ft x 1mm',
        attributesJson: '{"pattern":"Fluted Oak","thickness":"1.0mm","sheetSize":"8x4 ft","texture":"Embossed Flute"}',
        featured: true,
        media: ['/catalog/brass-06.jpg']
    },
    {
        id: 'w-13',
        name: 'High Gloss Brushed Metallic Brass Laminate 1mm',
        category: 'Laminates',
        subcategory: 'Metallic & Acrylic',
        brandName: 'Greenlam',
        price: 4600,
        image: '/catalog/brass-01.jpg',
        description: 'Genuine metallic foil high-pressure decorative laminate sheet with scratch-resistant coating.',
        finish: 'Mirror High Gloss',
        material: 'Metallic HPL',
        color: 'Brass',
        style: 'Glamour',
        dimensions: '8ft x 4ft x 1mm',
        attributesJson: '{"pattern":"Brushed Metallic Foil","thickness":"1.0mm","sheetSize":"8x4 ft"}',
        featured: false,
        media: ['/catalog/brass-01.jpg']
    },
    {
        id: 'w-14',
        name: 'Suede Anti-Fingerprint Charcoal Laminate 0.8mm',
        category: 'Laminates',
        subcategory: 'Matte / Suede',
        brandName: 'Merino',
        price: 3400,
        image: '/catalog/brass-02.jpg',
        description: 'Super matte velvety surface featuring thermal healing of micro-scratches and zero fingerprint marks.',
        finish: 'Anti-Fingerprint Suede',
        material: 'High Pressure Laminate',
        color: 'Black',
        style: 'Minimalist',
        dimensions: '8ft x 4ft x 0.8mm',
        attributesJson: '{"finish":"Super Matte","thickness":"0.8mm","sheetSize":"8x4 ft"}',
        featured: false,
        media: ['/catalog/brass-02.jpg']
    },

    // 4. KITCHEN ACCESSORIES
    {
        id: 'w-15',
        name: 'Soft-Close Slim Tandem Drawer System 500mm',
        category: 'Kitchen Accessories',
        subcategory: 'Drawer & Sliding Systems',
        brandName: 'Hettich',
        price: 5400,
        image: '/catalog/brass-03.jpg',
        description: 'Ultra-slim 13mm straight inner wall tandem box drawer system with synchronized silent soft-close runners.',
        finish: 'Anthracite Matte',
        material: 'Steel',
        color: 'Grey',
        style: 'Modern',
        dimensions: '500mm Length x 120mm Height',
        attributesJson: '{"loadCapacity":"40kg","nominalLength":"500mm","runnerType":"Synchronized Soft-Close Undermount"}',
        featured: true,
        media: ['/catalog/brass-03.jpg']
    },
    {
        id: 'w-16',
        name: 'Stainless Steel Magic Corner Kitchen Storage Pull-out',
        category: 'Kitchen Accessories',
        subcategory: 'Corner & Storage Pull-outs',
        brandName: 'Hafele',
        price: 5900,
        image: '/catalog/brass-04.jpg',
        description: 'Universal blind corner swing-out mechanism bringing 4 heavy-gauge wire baskets smoothly out into the kitchen.',
        finish: 'Polished Chrome',
        material: 'Stainless Steel',
        color: 'Silver',
        style: 'Modular',
        dimensions: '900mm Carcass Width',
        attributesJson: '{"carcassWidth":"900mm","doorOpening":"Universal Left/Right","trayCapacity":"40kg total (10kg per basket)"}',
        featured: false,
        media: ['/catalog/brass-04.jpg']
    }
];
