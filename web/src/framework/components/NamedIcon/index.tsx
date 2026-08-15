import {WarningOutlined} from '@ant-design/icons';
import React, {useEffect, useState} from 'react';

type IconComponent = React.ComponentType<React.ComponentProps<typeof WarningOutlined>>;

/**
 * 构建期生成 "图标名 -> 按需加载函数" 的映射。
 * 避免 import * as Icons 全量打包 @ant-design/icons（800+ 图标），
 * 每个图标编译为独立 chunk，仅在菜单/数据引用时按需加载。
 */
const iconLoaders: Record<string, () => Promise<{ default: IconComponent }>> = {};
for (const [path, loader] of Object.entries(
    import.meta.glob('/node_modules/@ant-design/icons/es/icons/*.js')
)) {
    const name = /([^/]+)\.js$/.exec(path)?.[1]; // NOSONAR: 提取文件名，已最简
    if (name) {
        iconLoaders[name] = loader as () => Promise<{ default: IconComponent }>;
    }
}

/** 已加载图标缓存，命中后渲染为同步 */
const iconCache = new Map<string, IconComponent>();

type NamedIconProps = {
    name: string;
} & React.ComponentProps<typeof WarningOutlined>;

export function NamedIcon(props: NamedIconProps): React.ReactElement {
    const {name, ...rest} = props;
    const [loaded, setLoaded] = useState<{ name: string; comp: IconComponent } | null>(null);

    useEffect(() => {
        if (iconCache.has(name)) {
            return;
        }
        const loader = iconLoaders[name];
        if (!loader) {
            console.warn(`NamedIcon: icon "${name}" not found, using fallback`);
            return;
        }
        let cancelled = false;
        loader().then(mod => {
            iconCache.set(name, mod.default);
            if (!cancelled) {
                setLoaded({name, comp: mod.default});
            }
        }).catch(e => {
            console.warn(`NamedIcon: failed to load icon "${name}"`, e);
        });
        return () => {
            cancelled = true;
        };
    }, [name]);

    const Icon = (loaded?.name === name) ? loaded.comp : iconCache.get(name);

    if (Icon) {
        return <Icon {...rest} />;
    }
    if (!iconLoaders[name]) {
        return <WarningOutlined {...rest} />;
    }
    // 加载中：1em 占位，避免菜单布局抖动
    return <span style={{display: 'inline-block', width: '1em', height: '1em', verticalAlign: '-0.125em'}}/>;
}
