import React from 'react';
import { Button, Drawer, Segmented } from 'antd';
import { CheckOutlined } from '@ant-design/icons';
import {
    THEME_PRESETS,
    DEFAULT_COLORS,
    DEFAULT_SIDER_BG,
    applyThemePreset,
    getActivePresetKey,
    resetAllSettings,
    DENSITY_PRESETS,
    RADIUS_PRESETS,
    applyDensityPreset,
    getActiveDensityKey,
    applyRadiusPreset,
    getActiveRadiusKey,
    getLayoutMode,
    setLayoutMode,
    getMenuMode,
    setMenuMode,
} from '../../config';
import type { ThemeDensity, ThemeRadius, LayoutMode, MenuMode } from '../../config';
import { EventBus } from '../../utils/EventBus';

export interface ThemeSettingsProps {
    open: boolean;
    onClose: () => void;
}

function SectionTitle({ children }: { children: React.ReactNode }) {
    return <div style={{ margin: '20px 0 12px', fontWeight: 600 }}>{children}</div>;
}

/**
 * 界面设置面板：布局模式、菜单模式、主题色、界面密度、圆角。
 * 明暗模式由顶栏的太阳/月亮按钮切换，此处不再重复提供。
 * 所有变更即时生效并持久化到 localStorage。
 */
export function ThemeSettings({ open, onClose }: ThemeSettingsProps) {
    const [activeKey, setActiveKey] = React.useState(getActivePresetKey());
    const [density, setDensity] = React.useState<ThemeDensity>(getActiveDensityKey());
    const [radius, setRadius] = React.useState<ThemeRadius>(getActiveRadiusKey());
    const [layoutMode, setLayoutModeState] = React.useState<LayoutMode>(getLayoutMode());
    const [menuMode, setMenuModeState] = React.useState<MenuMode>(getMenuMode());

    React.useEffect(() => {
        const offTheme = EventBus.on('themeChange', () => {
            setActiveKey(getActivePresetKey());
            setDensity(getActiveDensityKey());
            setRadius(getActiveRadiusKey());
        });
        const offLayout = EventBus.on('layoutChange', () => {
            setLayoutModeState(getLayoutMode());
        });
        const offMenu = EventBus.on('menuModeChange', () => {
            setMenuModeState(getMenuMode());
        });
        return () => {
            offTheme();
            offLayout();
            offMenu();
        };
    }, []);

    const radiusValue = (key: ThemeRadius) =>
        (RADIUS_PRESETS.find(p => p.key === key)?.token.borderRadius as number) ?? 6;

    return (
        <Drawer title="界面设置" open={open} onClose={onClose} size="min(340px, 100vw)">
            <div style={{ fontWeight: 600 }}>布局模式</div>
            <Segmented
                block
                style={{ marginTop: 12 }}
                value={layoutMode}
                onChange={value => setLayoutMode(value as LayoutMode)}
                options={[
                    { label: '页签模式', value: 'tabs' },
                    { label: '单页模式', value: 'single' },
                ]}
            />
            <div style={{ marginTop: 8, fontSize: 12, color: 'var(--oa-color-text-secondary, rgba(0, 0, 0, 0.45))' }}>
                页签模式可同时打开多个页面；单页模式仅显示当前页面。
            </div>

            <SectionTitle>菜单模式</SectionTitle>
            <Segmented
                block
                value={menuMode}
                onChange={value => setMenuMode(value as MenuMode)}
                options={[
                    { label: '顶部菜单', value: 'top' },
                    { label: '侧边菜单', value: 'side' },
                ]}
            />
            <div style={{ marginTop: 8, fontSize: 12, color: 'var(--oa-color-text-secondary, rgba(0, 0, 0, 0.45))' }}>
                顶部菜单会将一级目录显示在顶栏，根级无目录菜单归入「业务模块」；移动端固定使用抽屉导航，此设置影响桌面端。
            </div>

            <SectionTitle>主题色</SectionTitle>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 12, marginTop: 12 }}>
                {THEME_PRESETS.map(preset => {
                    const primary = preset.colors.colorPrimary || DEFAULT_COLORS.colorPrimary || '#1677ff';
                    const sider = preset.colors.siderBg || DEFAULT_SIDER_BG;
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

            <Button block style={{ marginTop: 20 }} onClick={() => resetAllSettings()}>
                恢复默认
            </Button>
        </Drawer>
    );
}

export default ThemeSettings;
