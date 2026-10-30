import {UrlUtils} from './UrlUtils';

/** 内置头像标识前缀，数据库中存 preset:avatar-1 这类值 */
const PRESET_PREFIX = 'preset:';

/** 内置头像文件命名规则，需与后端 AvatarPresets 保持一致 */
const PRESET_PATTERN = /^preset:avatar-(?:[1-9]|1[0-2])$/;

/** 十二生肖头像名称，需与后端 AvatarPresets 保持一致 */
const ZODIAC_LABELS = ['鼠', '牛', '虎', '兔', '龙', '蛇', '马', '羊', '猴', '鸡', '狗', '猪'];

export interface AvatarPreset {
    key: string;
    label: string;
    url: string;
}

/** 默认头像，文件由后端静态目录提供 */
export const DEFAULT_AVATAR_URL = UrlUtils.contextPath('/avatars/default.svg');

function presetUrl(key: string): string | null {
    if (!PRESET_PATTERN.test(key)) {
        return null;
    }
    return UrlUtils.contextPath(`/avatars/${key.substring(PRESET_PREFIX.length)}.svg`);
}

/**
 * 接口不可用时的兜底列表，与后端 AvatarPresets 保持一致。
 * 正常使用时优先读取 admin/public/avatar-presets 返回的数据。
 */
export const FALLBACK_AVATAR_PRESETS: AvatarPreset[] = [
    {key: '', label: '默认头像', url: DEFAULT_AVATAR_URL},
    ...ZODIAC_LABELS.map((label, index) => {
        const key = `${PRESET_PREFIX}avatar-${index + 1}`;
        return {
            key,
            label,
            url: presetUrl(key) as string,
        };
    }),
];

export class AvatarUtils {

    /** 是否为内置头像标识 */
    static isPreset(avatar?: string | null): boolean {
        return !!avatar && avatar.startsWith(PRESET_PREFIX);
    }

    /** 将数据库中的头像值转换为可访问的图片地址；为空时返回默认头像 */
    static resolve(avatar?: string | null): string {
        if (!avatar) {
            return DEFAULT_AVATAR_URL;
        }
        if (AvatarUtils.isPreset(avatar)) {
            return presetUrl(avatar) || DEFAULT_AVATAR_URL;
        }
        return UrlUtils.contextPath(`/file/${avatar}?thumb=1`);
    }
}
