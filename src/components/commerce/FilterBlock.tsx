import React from 'react';

export function FilterBlock({ title, children }: {
    title: string;
    children: React.ReactNode;
}) {
    return (
        <div className="filter-block">
            <h3>{title}</h3>
            {children}
        </div>
    );
}
