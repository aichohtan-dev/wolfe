export type Product = {
    id: string;
    serverId?: number;
    name: string;
    category: string;
    price: number;
    image: string;
    description: string;
    finish: string;
    material: string;
    color: string;
    style: string;
    featured: boolean;
    media: string[];
};
// Launch catalog: imagery is sourced from the supplied BRASS catalog reference.
// Prices remain editable by Admin and are initially seeded in the ₹4,000–₹6,000 range.
export const products: Product[] = [
    { id: 'w-01', name: 'Brass Collection Handle 01', category: 'Handles', price: 4200, image: '/catalog/brass-01.jpg', description: 'A classic brass lever profile selected for the Wolfe launch collection.', finish: 'Brushed Brass', material: 'Brass', color: 'Brass', style: 'Classic', featured: false, media: [] },
    { id: 'w-02', name: 'Brass Collection Handle 02', category: 'Handles', price: 4600, image: '/catalog/brass-02.jpg', description: 'A refined tapered profile with a warm architectural presence.', finish: 'Brushed Brass', material: 'Brass', color: 'Brass', style: 'Classic', featured: false, media: [] },
    { id: 'w-03', name: 'Brass Collection Handle 03', category: 'Handles', price: 5100, image: '/catalog/brass-03.jpg', description: 'A faceted statement handle for doors and considered interiors.', finish: 'Brushed Brass', material: 'Brass', color: 'Brass', style: 'Classic', featured: false, media: [] },
    { id: 'w-04', name: 'Brass Collection Handle 04', category: 'Handles', price: 4400, image: '/catalog/brass-04.jpg', description: 'A clean contemporary profile with a softly sculptural silhouette.', finish: 'Brushed Brass', material: 'Brass', color: 'Brass', style: 'Classic', featured: false, media: [] },
    { id: 'w-05', name: 'Brass Collection Handle 05', category: 'Handles', price: 5600, image: '/catalog/brass-05.jpg', description: 'A compact architectural form designed for coordinated hardware schemes.', finish: 'Brushed Brass', material: 'Brass', color: 'Brass', style: 'Classic', featured: false, media: [] },
    { id: 'w-06', name: 'Brass Collection Handle 06', category: 'Handles', price: 4900, image: '/catalog/brass-06.jpg', description: 'A versatile brass form for residential and furniture applications.', finish: 'Brushed Brass', material: 'Brass', color: 'Brass', style: 'Classic', featured: false, media: [] }
];
