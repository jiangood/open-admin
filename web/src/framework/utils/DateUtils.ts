import {StringUtils} from './StringUtils';

export interface FriendlyTimeOptions {
    /** 默认 true（保持兼容）；false 时不足 1 分钟显示「刚刚」 */
    showSeconds?: boolean;
    /** 默认 false；true 时不换算成「周」，统一用「天」 */
    daysOnly?: boolean;
    /** 默认 'after'（保持兼容）；'clamp' 时未来时间按「刚刚」处理，不显示「N 分钟后」 */
    future?: 'after' | 'clamp';
    /** 默认 undefined（保持兼容）；传入后无效时间返回该占位文案 */
    empty?: string;
}

/** epoch 秒 / 毫秒分界：1e11 秒约为 5138 年，1e11 毫秒约为 1973 年 */
const EPOCH_MILLISECONDS_THRESHOLD = 1e11;

export class DateUtils {
    static convertTypeToFormat(type: string): string {
        if (type === 'YEAR') type = 'YYYY';
        else if (type === 'YEAR_MONTH') type = 'YYYY-MM';
        else if (type === 'YEAR_QUARTER') type = 'YYYY-QQ';
        else if (type === 'DAY') type = 'YYYY-MM-DD';
        return type;
    }

    static year(date: Date): number { return date.getFullYear(); }
    static month(date: Date): string { return StringUtils.pad(date.getMonth() + 1, 2); }
    static date(date: Date): string { return StringUtils.pad(date.getDate(), 2); }
    static hour(date: Date): string { return StringUtils.pad(date.getHours(), 2); }
    static minute(date: Date): string { return StringUtils.pad(date.getMinutes(), 2); }
    static second(date: Date): string { return StringUtils.pad(date.getSeconds(), 2); }

    /**
     * 统一解析后端时间字段：Date、epoch 秒、epoch 毫秒、ISO 串、
     * `YYYY-MM-DD HH:mm:ss`、纯日期（按本地零点）。无效或空值返回 null。
     */
    static toDate(value: Date | string | number | null | undefined): Date | null {
        if (value == null) return null;
        if (value instanceof Date) return Number.isNaN(value.getTime()) ? null : value;
        if (typeof value === 'number') {
            return Number.isFinite(value) ? this.fromEpoch(value) : null;
        }
        const text = value.trim();
        if (text === '') return null;
        if (/^-?\d+(\.\d+)?$/.test(text)) return this.fromEpoch(Number(text));
        let normalized = text;
        if (!normalized.includes('T')) {
            if (normalized.includes(':')) normalized = normalized.replace(' ', 'T');
            else if (/^\d{4}-\d{2}-\d{2}$/.test(normalized)) normalized += 'T00:00:00';
        }
        // Safari 只接受 3 位毫秒，截断 Spring/Jackson 可能输出的微秒、纳秒
        normalized = normalized.replace(/(\.\d{3})\d+/, '$1');
        const date = new Date(normalized);
        return Number.isNaN(date.getTime()) ? null : date;
    }

    private static fromEpoch(value: number): Date | null {
        const milliseconds = Math.abs(value) < EPOCH_MILLISECONDS_THRESHOLD ? value * 1000 : value;
        const date = new Date(milliseconds);
        return Number.isNaN(date.getTime()) ? null : date;
    }

    static formatDate(d: Date | string | number): string {
        const date = this.toDate(d);
        if (date == null) return '';
        return this.year(date) + '-' + this.month(date) + '-' + this.date(date);
    }

    static formatTime(d: Date | string | number): string {
        const date = this.toDate(d);
        if (date == null) return '';
        return this.hour(date) + ':' + this.minute(date) + ':' + this.second(date);
    }

    static formatDateTime(d: Date | string | number): string {
        const date = this.toDate(d);
        if (date == null) return '';
        return this.formatDate(date) + ' ' + this.formatTime(date);
    }

    static formatDateCn(d: Date | string | number): string {
        const date = this.toDate(d);
        if (date == null) return '';
        return this.year(date) + '年' + (date.getMonth() + 1) + '月' + date.getDate() + '日';
    }

    static now(): string { return this.formatDateTime(new Date()); }
    static today(): string { return this.formatDate(new Date()); }
    static thisYear(): number { return this.year(new Date()); }
    static thisMonth(): string { return this.month(new Date()); }

    static friendlyTime(
        value: Date | string | number | null | undefined,
        options?: FriendlyTimeOptions,
    ): string | undefined {
        const {showSeconds = true, daysOnly = false, future = 'after', empty} = options ?? {};
        const pastDate = this.toDate(value);
        if (pastDate == null) return empty;

        let elapsedMilliseconds = Date.now() - pastDate.getTime();
        const clamped = elapsedMilliseconds < 0 && future === 'clamp';
        if (clamped) elapsedMilliseconds = 0;
        const suffix = elapsedMilliseconds > 0 ? '前' : '后';
        elapsedMilliseconds = Math.abs(elapsedMilliseconds);
        const elapsedYears = Math.floor(elapsedMilliseconds / (1000 * 60 * 60 * 24 * 365));
        const elapsedMonths = Math.floor(elapsedMilliseconds / (1000 * 60 * 60 * 24 * 30));
        const elapsedDays = Math.floor(elapsedMilliseconds / (1000 * 60 * 60 * 24));
        const elapsedHours = Math.floor(elapsedMilliseconds / (1000 * 60 * 60));
        const elapsedMinutes = Math.floor(elapsedMilliseconds / (1000 * 60));
        const elapsedSeconds = Math.floor(elapsedMilliseconds / 1000);
        if (elapsedYears >= 1) return `${elapsedYears} 年${suffix}`;
        if (elapsedMonths >= 1) return `${elapsedMonths} 个月${suffix}`;
        if (!daysOnly && elapsedDays >= 7) { const weeks = Math.floor(elapsedDays / 7); return `${weeks} 周${suffix}`; }
        if (elapsedDays >= 1) return `${elapsedDays} 天${suffix}`;
        if (elapsedHours >= 1) return `${elapsedHours} 小时${suffix}`;
        if (elapsedMinutes >= 1) return `${elapsedMinutes} 分钟${suffix}`;
        if (!showSeconds || clamped) return '刚刚';
        return `${elapsedSeconds} 秒${suffix}`;
    }

    static friendlyTotalTime(time: number | string | null): string | null {
        if (time == null || time === '-') return null;
        let seconds: number;
        if (typeof time === 'string') seconds = Number.parseInt(time, 10) / 1000;
        else seconds = time / 1000;
        seconds = Math.floor(seconds);
        if (seconds < 60) return seconds + '秒';
        let min = seconds / 60;
        seconds = seconds % 60;
        min = Math.floor(min);
        seconds = Math.floor(seconds);
        return min + '分' + seconds + '秒';
    }

    static beginOfMonth(): string {
        const d = new Date();
        d.setDate(1);
        return this.formatDate(d);
    }
}
