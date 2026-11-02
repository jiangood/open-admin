import React from "react";
import {Avatar, Tooltip} from "antd";
import {
    AvatarUtils,
    FALLBACK_AVATAR_PRESETS,
    FieldUploadImage,
    getToken,
    HttpClient,
} from "../../framework";

const ITEM_SIZE = 44;

/**
 * 头像选择：内置头像 + 自定义上传
 * <p>
 * 内置头像从后端 admin/public/avatar-presets 获取，移动端可复用同一接口；
 * 上传入口以虚线圆形按钮排在列表末尾，选中/上传后直接在同一排展示结果。
 */
export default class AvatarPicker extends React.Component {

    state = {
        presets: FALLBACK_AVATAR_PRESETS,
    }

    componentDidMount() {
        HttpClient.get('admin/public/avatar-presets', null, {toastError: false}).then(rs => {
            const presets = rs.data?.presets;
            if (Array.isArray(presets) && presets.length > 0) {
                this.setState({presets});
            }
        }).catch(() => {
            // 接口不可用时保留前端兜底列表
        })
    }

    render() {
        const {value, onChange} = this.props;
        const {presets} = this.state;
        const uploaded = value && !AvatarUtils.isPreset(value) ? value : '';
        const primary = getToken().colorPrimary;

        return <div style={{display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: 10}}>
            {presets.map(preset => {
                const selected = (value || '') === preset.key;
                return <Tooltip key={preset.key} title={preset.label}>
                    <button type="button"
                            aria-label={preset.label}
                            aria-pressed={selected}
                            onClick={() => onChange?.(preset.key)}
                            style={{
                                width: ITEM_SIZE,
                                height: ITEM_SIZE,
                                padding: 0,
                                border: 'none',
                                borderRadius: '50%',
                                background: 'transparent',
                                cursor: 'pointer',
                                lineHeight: 0,
                                boxShadow: selected ? `0 0 0 2px ${primary}` : 'none',
                                transition: 'box-shadow .2s',
                            }}>
                        <Avatar size={ITEM_SIZE} src={preset.url}/>
                    </button>
                </Tooltip>;
            })}

            <FieldUploadImage value={uploaded}
                              onChange={(v) => onChange?.(v || '')}
                              maxCount={1}
                              shape="circle"
                              size={ITEM_SIZE}
                              triggerTitle="上传自定义头像"
                              defaultCropRatio={{width: 1, height: 1}}/>
        </div>;
    }
}
