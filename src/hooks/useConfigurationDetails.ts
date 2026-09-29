import { useEffect, useMemo, useState } from 'react';
import { api } from '../api';
import type { CartItem } from '../types/cart';

export function useConfigurationDetails(items: CartItem[]) {
    const [details, setDetails] = useState<Record<string, any>>({});
    const tokens = useMemo(
        () => Array.from(new Set(items.map(i => i.configurationToken).filter(Boolean) as string[])).sort().join('|'),
        [items]
    );

    useEffect(() => {
        let live = true;
        const list = tokens ? tokens.split('|') : [];
        if (!list.length) {
            setDetails({});
            return;
        }

        Promise.all(list.map(async (token) => {
            try {
                return [token, await api.experience.getConfiguration(token)] as const;
            } catch {
                return [token, null] as const;
            }
        })).then(entries => {
            if (live) setDetails(Object.fromEntries(entries.filter(([, v]) => v)));
        });

        return () => {
            live = false;
        };
    }, [tokens]);

    return details;
}
