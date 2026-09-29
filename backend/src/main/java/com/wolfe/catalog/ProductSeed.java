package com.wolfe.catalog;

import com.wolfe.catalog.brand.Brand;
import com.wolfe.catalog.brand.BrandRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductSeed {

    private static final SeedProduct[] PRODUCTS = {
        // --- 1. HARDWARE ---
        new SeedProduct("w-01", "Arc Pull Handle", "4200", "Hardware", "Handles", "Wolfe Heritage", "Brushed Brass", "Brass", "Brass", "Classic", "160mm x 35mm", "A quiet architectural pull with a softly considered profile.", "/catalog/brass-01.jpg", "{\"mounting\":\"Rear Bolt\",\"centerToCenter\":\"128mm / 160mm / 224mm\",\"projection\":\"35mm\"}", List.of(
            new SeedVariant("Arc Pull - 128mm Brushed Brass", "WLF-ARC-128-BB", "Brass", "Brass", "128mm", "Brushed Brass", "128mm", new BigDecimal("4200.00"), 50, "/catalog/brass-01.jpg"),
            new SeedVariant("Arc Pull - 160mm Brushed Brass", "WLF-ARC-160-BB", "Brass", "Brass", "160mm", "Brushed Brass", "160mm", new BigDecimal("4600.00"), 40, "/catalog/brass-01.jpg"),
            new SeedVariant("Arc Pull - 224mm Matte Black", "WLF-ARC-224-MB", "Matte Black", "Brass", "224mm", "Matte Black", "224mm", new BigDecimal("5100.00"), 35, "/catalog/brass-01.jpg")
        )),
        new SeedProduct("w-02", "Noir Round Knob", "4600", "Hardware", "Knobs", "Wolfe Heritage", "Matte Black", "Brass", "Black", "Modern", "32mm x 28mm", "Compact, tactile and understated for modern cabinetry.", "/catalog/brass-02.jpg", "{\"diameter\":\"32mm\",\"projection\":\"28mm\",\"finishType\":\"PVD Matte\"}", List.of(
            new SeedVariant("Noir Knob - 30mm Matte Black", "WLF-NOIR-30-MB", "Matte Black", "Brass", "30mm", "Matte Black", "30mm", new BigDecimal("4600.00"), 60, "/catalog/brass-02.jpg"),
            new SeedVariant("Noir Knob - 38mm Brushed Brass", "WLF-NOIR-38-BB", "Brass", "Brass", "38mm", "Brushed Brass", "38mm", new BigDecimal("4900.00"), 45, "/catalog/brass-02.jpg")
        )),
        new SeedProduct("w-03", "Linear Cabinet Pull", "5100", "Hardware", "Handles", "Wolfe Heritage", "Satin Nickel", "Brass", "Silver", "Contemporary", "192mm x 30mm", "Minimal geometry designed for contemporary interiors.", "/catalog/brass-03.jpg", "{\"centerToCenter\":\"192mm\",\"weight\":\"340g\",\"material\":\"Solid Forged Brass\"}", List.of(
            new SeedVariant("Linear Pull - 128mm Satin Nickel", "WLF-LIN-128-SN", "Silver", "Brass", "128mm", "Satin Nickel", "128mm", new BigDecimal("5100.00"), 50, "/catalog/brass-03.jpg"),
            new SeedVariant("Linear Pull - 192mm Satin Nickel", "WLF-LIN-192-SN", "Silver", "Brass", "192mm", "Satin Nickel", "192mm", new BigDecimal("5600.00"), 40, "/catalog/brass-03.jpg"),
            new SeedVariant("Linear Pull - 192mm Brushed Brass", "WLF-LIN-192-BB", "Brass", "Brass", "192mm", "Brushed Brass", "192mm", new BigDecimal("5600.00"), 30, "/catalog/brass-03.jpg")
        )),
        new SeedProduct("w-04", "Atelier Sculptural Hook", "4400", "Hardware", "Hooks", "Wolfe Heritage", "Antique Brass", "Brass", "Bronze", "Sculptural", "120mm x 45mm", "A sculptural wall hook that works beautifully in multiples.", "/catalog/brass-04.jpg", "{\"weightCapacity\":\"15kg\",\"fixing\":\"Concealed Wall Anchor\"}", List.of(
            new SeedVariant("Atelier Hook - Single Antique Brass", "WLF-ATL-S-AB", "Bronze", "Brass", "Single", "Antique Brass", "120mm", new BigDecimal("4400.00"), 50, "/catalog/brass-04.jpg"),
            new SeedVariant("Atelier Hook - Double Brushed Brass", "WLF-ATL-D-BB", "Brass", "Brass", "Double", "Brushed Brass", "145mm", new BigDecimal("5200.00"), 40, "/catalog/brass-04.jpg")
        )),
        new SeedProduct("w-05", "Edge Architectural Cup Pull", "5600", "Hardware", "Handles", "Wolfe Heritage", "Bronze", "Brass", "Bronze", "Refined", "96mm x 40mm", "A refined cup pull with a strong but restrained silhouette.", "/catalog/brass-05.jpg", "{\"gripDepth\":\"20mm\",\"installation\":\"Rear Screw\"}", List.of(
            new SeedVariant("Edge Cup Pull - 96mm Bronze", "WLF-EDG-96-BZ", "Bronze", "Brass", "96mm", "Bronze", "96mm", new BigDecimal("5600.00"), 45, "/catalog/brass-05.jpg"),
            new SeedVariant("Edge Cup Pull - 96mm Brushed Brass", "WLF-EDG-96-BB", "Brass", "Brass", "96mm", "Brushed Brass", "96mm", new BigDecimal("5600.00"), 35, "/catalog/brass-05.jpg")
        )),
        new SeedProduct("w-06", "Studio Minimalist Knob", "4900", "Hardware", "Knobs", "Wolfe Heritage", "Polished Chrome", "Brass", "Silver", "Modern", "30mm x 25mm", "A versatile circular form for kitchens, wardrobes and furniture.", "/catalog/brass-06.jpg", "{\"diameter\":\"30mm\",\"finish\":\"Mirror Polished Chrome\"}", List.of(
            new SeedVariant("Studio Knob - 25mm Polished Chrome", "WLF-STU-25-PC", "Silver", "Brass", "25mm", "Polished Chrome", "25mm", new BigDecimal("4900.00"), 50, "/catalog/brass-06.jpg"),
            new SeedVariant("Studio Knob - 32mm Polished Chrome", "WLF-STU-32-PC", "Silver", "Brass", "32mm", "Polished Chrome", "32mm", new BigDecimal("5300.00"), 40, "/catalog/brass-06.jpg")
        )),
        new SeedProduct("w-07", "Mortise Door Lock Set", "5800", "Hardware", "Locks", "Godrej", "Satin Stainless", "Stainless Steel", "Silver", "Contemporary", "60mm Backset", "High-security European standard mortise lock body with brass double cylinder and keys.", "/catalog/brass-01.jpg", "{\"backset\":\"60mm\",\"centerDistance\":\"85mm\",\"warranty\":\"5 Years\"}", List.of(
            new SeedVariant("Mortise Lock - 60mm Satin Stainless", "GDJ-ML-60-SS", "Silver", "Stainless Steel", "60mm", "Satin Stainless", "60mm Backset", new BigDecimal("5800.00"), 50, "/catalog/brass-01.jpg"),
            new SeedVariant("Mortise Lock - 60mm Antique Brass", "GDJ-ML-60-AB", "Bronze", "Brass", "60mm", "Antique Brass", "60mm Backset", new BigDecimal("6400.00"), 30, "/catalog/brass-01.jpg")
        )),
        new SeedProduct("w-08", "Concealed Soft-Close Cabinet Hinge", "2800", "Hardware", "Hinges", "Blum", "Nickel Plated", "Steel", "Silver", "Precision", "110 Degree Opening", "Clip-on 3D adjustable concealed hydraulic hinge for silent cabinet door operation.", "/catalog/brass-02.jpg", "{\"openingAngle\":\"110°\",\"overlay\":\"Full Overlay / Inset\",\"cycles\":\"200,000 Tested\"}", List.of(
            new SeedVariant("Concealed Hinge - Full Overlay (0 Crank)", "BLM-HNG-FO-0", "Silver", "Steel", "Full Overlay", "Nickel Plated", "0 Crank", new BigDecimal("2800.00"), 100, "/catalog/brass-02.jpg"),
            new SeedVariant("Concealed Hinge - Half Overlay (8 Crank)", "BLM-HNG-HO-8", "Silver", "Steel", "Half Overlay", "Nickel Plated", "8 Crank", new BigDecimal("2900.00"), 80, "/catalog/brass-02.jpg"),
            new SeedVariant("Concealed Hinge - Inset (15 Crank)", "BLM-HNG-IN-15", "Silver", "Steel", "Inset", "Nickel Plated", "15 Crank", new BigDecimal("3100.00"), 60, "/catalog/brass-02.jpg")
        )),

        // --- 2. PLYWOOD ---
        new SeedProduct("w-09", "Marine Gold BWP Waterproof Plywood", "4800", "Plywood", "Marine / BWP Ply", "Greenply", "Calibrated Sanded", "Gurjan Hardwood", "Wood", "Structural", "8ft x 4ft", "100% Boiling Water Proof (IS:710) marine plywood bonded with unextended BWP synthetic resin.", "/catalog/brass-03.jpg", "{\"grade\":\"BWP IS:710\",\"thickness\":\"18mm\",\"sheetSize\":\"8x4 ft\",\"core\":\"100% Hardwood Gurjan\",\"warranty\":\"25 Years Guarantee\"}", List.of(
            new SeedVariant("Marine Gold BWP - 12mm 8x4 ft", "GPL-BWP-12MM-8X4", "Wood", "Hardwood", "12mm", "Calibrated Sanded", "8ft x 4ft", new BigDecimal("3800.00"), 40, "/catalog/brass-03.jpg"),
            new SeedVariant("Marine Gold BWP - 16mm 8x4 ft", "GPL-BWP-16MM-8X4", "Wood", "Hardwood", "16mm", "Calibrated Sanded", "8ft x 4ft", new BigDecimal("4400.00"), 50, "/catalog/brass-03.jpg"),
            new SeedVariant("Marine Gold BWP - 18mm 8x4 ft", "GPL-BWP-18MM-8X4", "Wood", "Hardwood", "18mm", "Calibrated Sanded", "8ft x 4ft", new BigDecimal("4800.00"), 60, "/catalog/brass-03.jpg"),
            new SeedVariant("Marine Gold BWP - 19mm 8x4 ft", "GPL-BWP-19MM-8X4", "Wood", "Hardwood", "19mm", "Calibrated Sanded", "8ft x 4ft", new BigDecimal("5100.00"), 35, "/catalog/brass-03.jpg")
        )),
        new SeedProduct("w-10", "Calibrated Club Prime Hardwood Plywood", "4500", "Plywood", "Calibrated Ply", "CenturyPly", "Quad-Press Calibrated", "Eucalyptus & Hardwood", "Wood", "Architectural", "8ft x 4ft", "Zero-gap quad-press calibrated ply for precision CNC routing and luxury furniture.", "/catalog/brass-04.jpg", "{\"grade\":\"BWR / Calibrated\",\"thickness\":\"16mm\",\"sheetSize\":\"8x4 ft\",\"tolerance\":\"+/- 0.2mm\",\"warranty\":\"20 Years\"}", List.of(
            new SeedVariant("Club Prime Calibrated - 12mm 8x4 ft", "CP-CAL-12MM-8X4", "Wood", "Hardwood", "12mm", "Calibrated", "8ft x 4ft", new BigDecimal("3600.00"), 45, "/catalog/brass-04.jpg"),
            new SeedVariant("Club Prime Calibrated - 16mm 8x4 ft", "CP-CAL-16MM-8X4", "Wood", "Hardwood", "16mm", "Calibrated", "8ft x 4ft", new BigDecimal("4500.00"), 50, "/catalog/brass-04.jpg"),
            new SeedVariant("Club Prime Calibrated - 19mm 8x4 ft", "CP-CAL-19MM-8X4", "Wood", "Hardwood", "19mm", "Calibrated", "8ft x 4ft", new BigDecimal("4950.00"), 40, "/catalog/brass-04.jpg")
        )),
        new SeedProduct("w-11", "FlexiPly Flexible Curvature Plywood", "3200", "Plywood", "Flexible Ply", "Greenply", "Smooth Raw", "Tropical Hardwood", "Wood", "Curved Form", "8ft x 4ft", "Engineered flexible structural plywood for seamless rounded columns, arches, and organic contours.", "/catalog/brass-05.jpg", "{\"grade\":\"Flexible MR\",\"thickness\":\"6mm / 8mm\",\"bendingRadius\":\"25mm\",\"sheetSize\":\"8x4 ft\"}", List.of(
            new SeedVariant("FlexiPly - 6mm 8x4 ft Cross Grain", "GPL-FLX-6MM-CG", "Wood", "Hardwood", "6mm", "Smooth Raw", "8ft x 4ft", new BigDecimal("3200.00"), 30, "/catalog/brass-05.jpg"),
            new SeedVariant("FlexiPly - 8mm 8x4 ft Long Grain", "GPL-FLX-8MM-LG", "Wood", "Hardwood", "8mm", "Smooth Raw", "8ft x 4ft", new BigDecimal("3800.00"), 25, "/catalog/brass-05.jpg")
        )),

        // --- 3. LAMINATES ---
        new SeedProduct("w-12", "Architectural Fluted Oak Decorative Laminate 1mm", "3900", "Laminates", "Textured & Fluted", "Merino", "Fluted 3D Texture", "High Pressure Laminate", "Wood", "Architectural", "8ft x 4ft x 1mm", "Tactile fluted linear oak architectural laminate sheet for wall paneling and cabinetry.", "/catalog/brass-06.jpg", "{\"pattern\":\"Fluted Oak\",\"thickness\":\"1.0mm\",\"sheetSize\":\"8x4 ft\",\"texture\":\"Embossed Flute\",\"finish\":\"Matte Suede\"}", List.of(
            new SeedVariant("Fluted Oak - Natural Light Oak 1mm", "MRN-FLT-OAK-NAT", "Wood", "HPL", "1.0mm", "Fluted 3D Texture", "8ft x 4ft", new BigDecimal("3900.00"), 40, "/catalog/brass-06.jpg"),
            new SeedVariant("Fluted Oak - Smoked Dark Walnut 1mm", "MRN-FLT-OAK-SMK", "Bronze", "HPL", "1.0mm", "Fluted 3D Texture", "8ft x 4ft", new BigDecimal("4100.00"), 35, "/catalog/brass-06.jpg")
        )),
        new SeedProduct("w-13", "High Gloss Brushed Metallic Brass Laminate 1mm", "4600", "Laminates", "Metallic & Acrylic", "Greenlam", "Mirror High Gloss", "Metallic HPL", "Brass", "Glamour", "8ft x 4ft x 1mm", "Genuine metallic foil high-pressure decorative laminate sheet with scratch-resistant coating.", "/catalog/brass-01.jpg", "{\"pattern\":\"Brushed Metallic Foil\",\"thickness\":\"1.0mm\",\"sheetSize\":\"8x4 ft\",\"scratchResistance\":\"High\"}", List.of(
            new SeedVariant("Metallic Laminate - Brushed Gold Brass 1mm", "GLM-MET-BRS-1MM", "Brass", "Metallic HPL", "1.0mm", "Mirror High Gloss", "8ft x 4ft", new BigDecimal("4600.00"), 30, "/catalog/brass-01.jpg"),
            new SeedVariant("Metallic Laminate - Brushed Rose Copper 1mm", "GLM-MET-COP-1MM", "Bronze", "Metallic HPL", "1.0mm", "Mirror High Gloss", "8ft x 4ft", new BigDecimal("4800.00"), 25, "/catalog/brass-01.jpg")
        )),
        new SeedProduct("w-14", "Suede Anti-Fingerprint Charcoal Laminate 0.8mm", "3400", "Laminates", "Matte / Suede", "Merino", "Anti-Fingerprint Suede", "High Pressure Laminate", "Black", "Minimalist", "8ft x 4ft x 0.8mm", "Super matte velvety surface featuring thermal healing of micro-scratches and zero fingerprint marks.", "/catalog/brass-02.jpg", "{\"finish\":\"Super Matte Anti-Fingerprint\",\"thickness\":\"0.8mm\",\"sheetSize\":\"8x4 ft\",\"colorCode\":\"Charcoal #1212\"}", List.of(
            new SeedVariant("Suede Laminate - 0.8mm Charcoal Black", "MRN-SUD-08-BLK", "Black", "HPL", "0.8mm", "Anti-Fingerprint Suede", "8ft x 4ft", new BigDecimal("3400.00"), 50, "/catalog/brass-02.jpg"),
            new SeedVariant("Suede Laminate - 1.0mm Charcoal Black", "MRN-SUD-10-BLK", "Black", "HPL", "1.0mm", "Anti-Fingerprint Suede", "8ft x 4ft", new BigDecimal("3950.00"), 40, "/catalog/brass-02.jpg")
        )),

        // --- 4. KITCHEN ACCESSORIES ---
        new SeedProduct("w-15", "Soft-Close Slim Tandem Drawer System 500mm", "5400", "Kitchen Accessories", "Drawer & Sliding Systems", "Hettich", "Anthracite Matte", "Powder Coated Steel", "Grey", "Modern", "500mm Length x 120mm Height", "Ultra-slim 13mm straight inner wall tandem box drawer system with synchronized silent soft-close runners.", "/catalog/brass-03.jpg", "{\"loadCapacity\":\"40kg\",\"nominalLength\":\"500mm\",\"drawerHeight\":\"120mm / 170mm\",\"runnerType\":\"Synchronized Soft-Close Undermount\"}", List.of(
            new SeedVariant("Slim Tandem Drawer - 500mm H86 Silk White", "HET-DRW-500-H86-WHT", "White", "Steel", "500mm x H86mm", "Silk White", "500mm", new BigDecimal("4900.00"), 35, "/catalog/brass-03.jpg"),
            new SeedVariant("Slim Tandem Drawer - 500mm H120 Anthracite", "HET-DRW-500-H120-ANT", "Grey", "Steel", "500mm x H120mm", "Anthracite Matte", "500mm", new BigDecimal("5400.00"), 40, "/catalog/brass-03.jpg"),
            new SeedVariant("Slim Tandem Drawer - 500mm H170 Anthracite", "HET-DRW-500-H170-ANT", "Grey", "Steel", "500mm x H170mm", "Anthracite Matte", "500mm", new BigDecimal("5900.00"), 30, "/catalog/brass-03.jpg")
        )),
        new SeedProduct("w-16", "Stainless Steel Magic Corner Kitchen Storage Pull-out", "5900", "Kitchen Accessories", "Corner & Storage Pull-outs", "Hafele", "Polished Chrome", "SS 304 & Steel", "Silver", "Modular", "900mm Carcass Width", "Universal blind corner swing-out mechanism bringing 4 heavy-gauge wire baskets smoothly out into the kitchen.", "/catalog/brass-04.jpg", "{\"carcassWidth\":\"900mm\",\"doorOpening\":\"Left or Right Hand Universal\",\"trayCapacity\":\"10kg per basket (40kg total)\",\"material\":\"SS 304 Anti-Rust\"}", List.of(
            new SeedVariant("Magic Corner - Left Hand Opening Chrome", "HAF-MGC-900-LH-CHR", "Silver", "SS 304", "Left Hand", "Polished Chrome", "900mm", new BigDecimal("5900.00"), 20, "/catalog/brass-04.jpg"),
            new SeedVariant("Magic Corner - Right Hand Opening Chrome", "HAF-MGC-900-RH-CHR", "Silver", "SS 304", "Right Hand", "Polished Chrome", "900mm", new BigDecimal("5900.00"), 20, "/catalog/brass-04.jpg"),
            new SeedVariant("Magic Corner - Universal Anthracite Solid Base", "HAF-MGC-900-UN-ANT", "Grey", "Steel & Wooden Base", "Universal", "Anthracite Matte", "900mm", new BigDecimal("6800.00"), 15, "/catalog/brass-04.jpg")
        ))
    };

    @Bean
    CommandLineRunner seed(ProductRepository repo, ProductVariantRepository variantRepo,
                          BrandRepository brandRepo, com.wolfe.inventory.InventoryRepository inventoryRepo) {
        return args -> {
            for (SeedProduct seed : PRODUCTS) {
                Brand brand = brandRepo.findByNameIgnoreCase(seed.brand()).orElseGet(() -> {
                    String bslug = seed.brand().toLowerCase().replaceAll("[^a-z0-9]+", "-");
                    return brandRepo.save(new Brand(seed.brand(), bslug, null, null, null, true, 10));
                });

                Product product = repo.findBySlug(seed.slug()).orElseGet(() -> {
                    Product p = new Product(seed.slug(), seed.name(), new BigDecimal(seed.price()), seed.category(), seed.finish(), seed.description());
                    p.setImageUrl(seed.image());
                    p.setBrandId(brand.getId());
                    p.setBrandName(brand.getName());
                    p.setSubcategory(seed.subcategory());
                    p.setMaterial(seed.material());
                    p.setColor(seed.color());
                    p.setStyle(seed.style());
                    p.setDimensions(seed.dimensions());
                    p.setAttributesJson(seed.attributesJson());
                    p.setActive(true);
                    return repo.save(p);
                });

                // Update product brand/subcategory/attributes if already existed
                product.setBrandId(brand.getId());
                product.setBrandName(brand.getName());
                product.setSubcategory(seed.subcategory());
                product.setCategory(seed.category());
                product.setDimensions(seed.dimensions());
                product.setAttributesJson(seed.attributesJson());
                repo.save(product);

                inventoryRepo.insertDefault(product.getId(), 50);

                // Seed rich variants if not present
                if (seed.variants() != null) {
                    int sortIdx = 0;
                    for (SeedVariant sv : seed.variants()) {
                        if (variantRepo.findBySku(sv.sku()).isEmpty()) {
                            ProductVariant v = new ProductVariant(
                                    product, sv.title(), sv.sku(), sv.color(), sv.material(),
                                    sv.size(), sv.finish(), sv.dimensions(), sv.price(),
                                    sv.stock(), sv.image(), "{}", true, sortIdx++
                            );
                            variantRepo.save(v);
                        }
                    }
                }
            }
        };
    }

    private record SeedProduct(
            String slug, String name, String price, String category, String subcategory,
            String brand, String finish, String material, String color, String style,
            String dimensions, String description, String image, String attributesJson,
            List<SeedVariant> variants
    ) {}

    private record SeedVariant(
            String title, String sku, String color, String material, String size,
            String finish, String dimensions, BigDecimal price, int stock, String image
    ) {}
}
