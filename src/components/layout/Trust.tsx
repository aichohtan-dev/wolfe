import React from 'react';

export function Trust({ icon, title }: {
    icon: React.ReactNode;
    title: string;
}) {
    return <div className="trust-item">{icon}<span>{title}</span></div>;
}
