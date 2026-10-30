import React from "react";
import {Avatar, Divider, Tooltip} from "antd";
import {
    AvatarUtils,
    FALLBACK_AVATAR_PRESETS,
    FieldUploadImage,
    getToken,
    HttpClient,
} from "../../framework";

/**
 * 头像选择：内置头像 + 自定义上传
 * <p>
 * 内置头像列表优先从后端 admin/public/avatar-presets 获取，移动端也可复用同一个接口。
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

        return <div>
            <div style={{display: 'flex', flexWrap: 'wrap', gap: 10}}>
                {presets.map(preset => {
                    const selected = (value || '') === preset.key;
                    return <Tooltip key={preset.key} title={preset.label}>
                        <button type="button"
                                aria-label={preset.label}
                                onClick={() => onChange?.(preset.key)}
                                style={{
                                    padding: 2,
                                    border: `2px solid ${selected ? primary : 'transparent'}`,
                                    borderRadius: '50%',
                                    background: 'transparent',
                                    cursor: 'pointer',
                                    lineHeight: 0,
                                }}>
                            <Avatar size={44} src={preset.url}/>
                        </button>
                    </Tooltip>;
                })}
            </div>

            <Divider style={{margin: '16px 0 12px'}}>或上传自定义头像</Divider>

            <FieldUploadImage value={uploaded}
                              onChange={(v) => onChange?.(v || '')}
                              maxCount={1}
                              defaultCropRatio={{width: 1, height: 1}}/>
        </div>;
    }
}
