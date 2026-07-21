import React, { useEffect, useRef } from 'react';

interface ContextMenuItem {
    key: string;
    label: string;
    icon?: React.ReactNode;
    danger?: boolean;
    disabled?: boolean;
    divider?: boolean;
}

interface ContextMenuProps {
    x: number;
    y: number;
    items: ContextMenuItem[];
    onClick: (info: { key: string }) => void;
    onClose: () => void;
}

export const ContextMenu: React.FC<ContextMenuProps> = ({ x, y, items, onClick, onClose }) => {
    const ref = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const handler = (e: MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) {
                onClose();
            }
        };
        const timer = setTimeout(() => {
            document.addEventListener('mousedown', handler);
        }, 0);
        return () => {
            clearTimeout(timer);
            document.removeEventListener('mousedown', handler);
        };
    }, [onClose]);

    return (
        <div
            ref={ref}
            style={{
                position: 'fixed',
                left: x,
                top: y,
                zIndex: 1050,
                background: '#fff',
                borderRadius: 4,
                padding: '4px 0',
                minWidth: 120,
                boxShadow: '0 6px 16px 0 rgba(0,0,0,0.08), 0 3px 6px -4px rgba(0,0,0,0.12), 0 9px 28px 8px rgba(0,0,0,0.05)',
            }}
        >
            {items.map(item => (
                item.divider ? (
                    <div key={item.key} style={{ height: 1, background: '#f0f0f0', margin: '4px 0' }} />
                ) : (
                    <div
                        key={item.key}
                        onClick={() => {
                            if (!item.disabled) {
                                onClick({ key: item.key });
                                onClose();
                            }
                        }}
                        style={{
                            padding: '5px 12px',
                            fontSize: 13,
                            cursor: item.disabled ? 'not-allowed' : 'pointer',
                            color: item.danger ? '#ff4d4f' : item.disabled ? '#rgba(0,0,0,0.25)' : '#333',
                            display: 'flex',
                            alignItems: 'center',
                            gap: 8,
                            background: 'transparent',
                            transition: 'background 0.15s',
                            userSelect: 'none',
                        }}
                        onMouseEnter={(e) => {
                            if (!item.disabled) e.currentTarget.style.background = '#f5f5f5';
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.background = 'transparent';
                        }}
                    >
                        {item.icon && <span style={{ fontSize: 14, lineHeight: 1 }}>{item.icon}</span>}
                        <span>{item.label}</span>
                    </div>
                )
            ))}
        </div>
    );
};
