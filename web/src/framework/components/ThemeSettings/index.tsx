import React from 'react';
import { Button, Drawer, Segmented } from 'antd';
import { CheckOutlined } from '@ant-design/icons';
import {
    THEME_PRESETS,
    DEFAULT_COLORS,
    applyThemePreset,
    getActivePresetKey,
    getThemeMode,
    resetAllSettings,
    setThemeMode,
    DENSITY_PRESETS,
    RADIUS_PRESETS,
    applyDensityPreset,
    getActiveDensityKey,
    applyRadiusPreset,
    getActiveRadiusKey,
    getLayoutMode,
    setLayoutMode,
} from '../../config';
import type { ThemeMode, ThemeDensity, ThemeRadius, LayoutMode } from '../../config';
import { EventBus } from '../../utils/EventBus';

export interface ThemeSettingsProps {
    open: boolean;
    onClose: () => void;
}

function SectionTitle({ children }: { children: React.ReactNode }) {
    return <div style={{ margin: '20px 0 12px', fontWeight: 600 }}>{children}</div>;
}

/**
 * 界面设置面板：外观模式、主题色、界面密度、圆角、界面布局。
 * 所有变更即时生效并持久化到 localStorage。
 */
export function ThemeSettings({ open, onClose }: ThemeSettingsProps) {
    const [activeKey, setActiveKey] = React.useState(getActivePresetKey());
    const [mode, setMode] = React.useState<ThemeMode>(getThemeMode());
    const [density, setDensity] = React.useState<ThemeDensity>(getActiveDensityKey());
    const [radius, setRadius] = React.useState<ThemeRadius>(getActiveRadiusKey());
    const [layout, setLayout] = React.useState<LayoutMode>(getLayoutMode());

    React.useEffect(() => {
        const offTheme = EventBus.on('themeChange', () => {
            setActiveKey(getActivePresetKey());
            setMode(getThemeMode());
            setDensity(getActiveDensityKey());
            setRadius(getActiveRadiusKey());
        });
        const offLayout = EventBus.on('layoutChange', () => {
            setLayout(getLayoutMode());
        });
        return () => {
            offTheme();
            offLayout();
        };
    }, []);

    const radiusValue = (key: ThemeRadius) =>
        (RADIUS_PRESETS.find(p => p.key === key)?.token.borderRadius as number) ?? 6;

    return (
        <Drawer title="界面设置" open={open} onClose={onClose} width={340}>
            <div style={{ fontWeight: 600 }}>外观模式</div>
            <Segmented
                block
                style={{ marginTop: 12 }}
                value={mode}
                onChange={value => setThemeMode(value as ThemeMode)}
                options={[
                    { label: '浅色', value: 'light' },
                    { label: '深色', value: 'dark' },
                ]}
            />

            <SectionTitle>主题色</SectionTitle>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 12 }}>
                {THEME_PRESETS.map(preset => {
                    const primary = preset.colors.colorPrimary || DEFAULT_COLORS.colorPrimary || '#1677ff';
                    const sider = preset.colors.siderBg || '#001529';
                    const active = preset.key === activeKey;
                    return (
                        <button
                            key={preset.key}
                            type="button"
                            aria-pressed={active}
                            onClick={() => applyThemePreset(preset.key)}
                            style={{
                                cursor: 'pointer',
                                padding: 0,
                                overflow: 'hidden',
                                borderRadius: 8,
                                textAlign: 'left',
                                background: 'transparent',
                                border: active
                                    ? `2px solid ${primary}`
                                    : '1px solid var(--oa-color-border, #e8e8e8)',
                            }}
                        >
                            <div style={{ display: 'flex', height: 44 }}>
                                <div
                                    style={{
                                        width: '50%',
                                        background: primary,
                                        display: 'flex',
                                        alignItems: 'center',
                                        justifyContent: 'center',
                                        color: '#fff',
                                    }}
                                >
                                    {active && <CheckOutlined />}
                                </div>
                                <div style={{ width: '50%', background: sider }} />
                            </div>
                            <div style={{ padding: '6px 8px', fontSize: 12 }}>{preset.name}</div>
                        </button>
                    );
                })}
            </div>

            <SectionTitle>界面密度</SectionTitle>
            <Segmented
                block
                value={density}
                onChange={value => applyDensityPreset(value as ThemeDensity)}
                options={DENSITY_PRESETS.map(p => ({ label: p.name, value: p.key }))}
            />

            <SectionTitle>圆角</SectionTitle>
            <Segmented
                block
                value={radius}
                onChange={value => applyRadiusPreset(value as ThemeRadius)}
                options={RADIUS_PRESETS.map(p => ({
                    value: p.key,
                    label: (
                        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                            <span
                                style={{
                                    display: 'inline-block',
                                    width: 14,
                                    height: 14,
                                    border: '1.5px solid currentColor',
                                    borderRadius: radiusValue(p.key),
                                }}
                            />
                            {p.name}
                        </span>
                    ),
                }))}
            />

            <SectionTitle>界面布局</SectionTitle>
            <Segmented
                block
                value={layout}
                onChange={value => setLayoutMode(value as LayoutMode)}
                options={[
                    { label: '多标签页', value: 'tabs' },
                    { label: '单页', value: 'single' },
                ]}
            />

            <Button block style={{ marginTop: 20 }} onClick={() => resetAllSettings()}>
                恢复默认
            </Button>
        </Drawer>
    );
}

export default ThemeSettings;
