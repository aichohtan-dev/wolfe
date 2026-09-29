export const read = <T = any>(k: string, f: T): T => {
    try {
        return JSON.parse(localStorage.getItem(k) || JSON.stringify(f));
    } catch {
        return f;
    }
};

export const write = (k: string, v: any) => localStorage.setItem(k, JSON.stringify(v));
