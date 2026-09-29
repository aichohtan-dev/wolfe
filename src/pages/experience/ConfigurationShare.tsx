import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../../api';

export function ConfigurationShare() {
    const { token } = useParams();
    const [data, setData] = useState<any>(null);

    useEffect(() => {
        if (token) {
            api.experience.getConfiguration(token).then(setData).catch(() => setData(null));
        }
    }, [token]);

    if (!data) {
        return <main className="container-w narrow-page"><p className="empty-state">Configuration not found.</p></main>;
    }

    return (
        <main className="container-w section">
            <p className="eyebrow">Saved Wolfe configuration</p>
            <h1>Build your look.</h1>
            <p>Product ID: {data.productId}</p>
            <pre className="config-share-json">{data.configJson}</pre>
            <Link to="/shop" className="btn btn-orange">Explore the collection</Link>
        </main>
    );
}

export default ConfigurationShare;
