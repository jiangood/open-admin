/**
 * Quartz cron 表达式工具：生成、识别与中文说明。
 *
 * Quartz 格式为 6 或 7 段：秒 分 时 日 月 周 [年]，第一位是秒。
 */

export type CronIntervalUnit = 'second' | 'minute' | 'hour' | 'day';
export type CronPeriod = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'YEARLY';

export interface CronInterval {
    mode: 'interval';
    every: number;
    unit: CronIntervalUnit;
}

export interface CronSchedule {
    mode: 'schedule';
    period: CronPeriod;
    hour: number;
    minute: number;
    second: number;
    weekday?: string;
    dayOfMonth?: number | 'L';
    month?: number;
}

export type CronParsed = CronInterval | CronSchedule;

export interface CronScheduleOptions {
    period: CronPeriod;
    hour: number;
    minute: number;
    second?: number;
    weekday?: string;
    dayOfMonth?: number | 'L';
    month?: number;
}

export const CRON_INTERVAL_UNITS: { value: CronIntervalUnit; label: string }[] = [
    {value: 'second', label: '秒'},
    {value: 'minute', label: '分钟'},
    {value: 'hour', label: '小时'},
    {value: 'day', label: '天'},
];

export const CRON_WEEKDAYS: { value: string; label: string }[] = [
    {value: 'MON', label: '周一'},
    {value: 'TUE', label: '周二'},
    {value: 'WED', label: '周三'},
    {value: 'THU', label: '周四'},
    {value: 'FRI', label: '周五'},
    {value: 'SAT', label: '周六'},
    {value: 'SUN', label: '周日'},
];

const WEEKDAY_CN: Record<string, string> = {
    SUN: '周日',
    MON: '周一',
    TUE: '周二',
    WED: '周三',
    THU: '周四',
    FRI: '周五',
    SAT: '周六',
};

/** Quartz 数字星期：1=周日 ... 7=周六 */
const WEEKDAY_BY_NUMBER = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];

const MONTH_BY_NAME: Record<string, number> = {
    JAN: 1,
    FEB: 2,
    MAR: 3,
    APR: 4,
    MAY: 5,
    JUN: 6,
    JUL: 7,
    AUG: 8,
    SEP: 9,
    OCT: 10,
    NOV: 11,
    DEC: 12,
};

type SimpleField =
    | { kind: 'any' }
    | { kind: 'step'; step: number }
    | { kind: 'number'; value: number }
    | { kind: 'list'; values: number[] }
    | { kind: 'range'; from: number; to: number };

type DomField =
    | { kind: 'any' }
    | { kind: 'day'; value: number }
    | { kind: 'days'; values: number[] }
    | { kind: 'range'; from: number; to: number }
    | { kind: 'step'; step: number }
    | { kind: 'last' }
    | { kind: 'lastOffset'; offset: number }
    | { kind: 'lastWeekday' }
    | { kind: 'nearestWeekday'; day: number };

type MonthField =
    | { kind: 'any' }
    | { kind: 'month'; value: number }
    | { kind: 'months'; values: number[] }
    | { kind: 'range'; from: number; to: number };

type DowField =
    | { kind: 'any' }
    | { kind: 'day'; name: string }
    | { kind: 'days'; names: string[] }
    | { kind: 'range'; from: string; to: string }
    | { kind: 'last'; name: string }
    | { kind: 'nth'; name: string; nth: number };

function toInt(token: string): number | null {
    return /^\d+$/.test(token) ? Number(token) : null;
}

function splitFields(expr: string | null | undefined): string[] | null {
    if (!expr) return null;
    const parts = expr.trim().split(/\s+/);
    if (parts.length === 7 && parts[6] === '*') return parts.slice(0, 6);
    if (parts.length !== 6) return null;
    return parts;
}

function weekdayKey(token: string): string | null {
    const upper = token.toUpperCase();
    if (WEEKDAY_CN[upper]) return upper;
    const n = toInt(token);
    return n != null && n >= 1 && n <= 7 ? WEEKDAY_BY_NUMBER[n - 1] : null;
}

function monthNumber(token: string): number | null {
    const upper = token.toUpperCase();
    if (MONTH_BY_NAME[upper] != null) return MONTH_BY_NAME[upper];
    const n = toInt(token);
    return n != null && n >= 1 && n <= 12 ? n : null;
}

function parseSimpleField(field: string, min: number, max: number): SimpleField | null {
    if (field === '*') return {kind: 'any'};

    // */n 与 0/n 都表示从 0 开始每隔 n
    const step = field.match(/^(?:\*|0)\/(\d+)$/);
    if (step) {
        const n = Number(step[1]);
        return n >= 1 && n <= max ? {kind: 'step', step: n} : null;
    }

    const range = field.match(/^(\d+)-(\d+)$/);
    if (range) {
        const from = Number(range[1]);
        const to = Number(range[2]);
        return from >= min && to <= max && from <= to ? {kind: 'range', from, to} : null;
    }

    if (field.includes(',')) {
        const values = field.split(',').map(toInt);
        if (values.every(v => v != null && v >= min && v <= max)) {
            return {kind: 'list', values: values as number[]};
        }
        return null;
    }

    const n = toInt(field);
    return n != null && n >= min && n <= max ? {kind: 'number', value: n} : null;
}

function parseDom(field: string): DomField | null {
    if (field === '*' || field === '?') return {kind: 'any'};
    if (field === 'L') return {kind: 'last'};
    if (field === 'LW') return {kind: 'lastWeekday'};

    const lastOffset = field.match(/^L-(\d+)$/);
    if (lastOffset) {
        const offset = Number(lastOffset[1]);
        return offset >= 1 && offset <= 30 ? {kind: 'lastOffset', offset} : null;
    }

    const nearest = field.match(/^(\d+)W$/);
    if (nearest) {
        const day = Number(nearest[1]);
        return day >= 1 && day <= 31 ? {kind: 'nearestWeekday', day} : null;
    }

    const step = field.match(/^(?:\*|0)\/(\d+)$/);
    if (step) {
        const n = Number(step[1]);
        return n >= 1 && n <= 31 ? {kind: 'step', step: n} : null;
    }

    if (field.includes(',')) {
        const values = field.split(',').map(toInt);
        if (values.every(v => v != null && v >= 1 && v <= 31)) {
            return {kind: 'days', values: values as number[]};
        }
        return null;
    }

    const range = field.match(/^(\d+)-(\d+)$/);
    if (range) {
        const from = Number(range[1]);
        const to = Number(range[2]);
        return from >= 1 && to <= 31 && from <= to ? {kind: 'range', from, to} : null;
    }

    const day = toInt(field);
    return day != null && day >= 1 && day <= 31 ? {kind: 'day', value: day} : null;
}

function parseMonth(field: string): MonthField | null {
    if (field === '*') return {kind: 'any'};

    const range = field.match(/^([A-Za-z0-9]+)-([A-Za-z0-9]+)$/);
    if (range) {
        const from = monthNumber(range[1]);
        const to = monthNumber(range[2]);
        return from != null && to != null && from <= to ? {kind: 'range', from, to} : null;
    }

    if (field.includes(',')) {
        const values = field.split(',').map(monthNumber);
        if (values.every(v => v != null)) {
            return {kind: 'months', values: values as number[]};
        }
        return null;
    }

    const month = monthNumber(field);
    return month != null ? {kind: 'month', value: month} : null;
}

function parseDow(field: string): DowField | null {
    if (field === '*' || field === '?') return {kind: 'any'};

    const nth = field.match(/^([A-Za-z0-9]+)#(\d+)$/);
    if (nth) {
        const name = weekdayKey(nth[1]);
        const n = Number(nth[2]);
        return name && n >= 1 && n <= 5 ? {kind: 'nth', name, nth: n} : null;
    }

    const last = field.match(/^([A-Za-z0-9]+)L$/);
    if (last) {
        const name = weekdayKey(last[1]);
        return name ? {kind: 'last', name} : null;
    }

    const range = field.match(/^([A-Za-z0-9]+)-([A-Za-z0-9]+)$/);
    if (range) {
        const from = weekdayKey(range[1]);
        const to = weekdayKey(range[2]);
        return from && to ? {kind: 'range', from, to} : null;
    }

    if (field.includes(',')) {
        const names = field.split(',').map(weekdayKey);
        if (names.every(Boolean)) {
            return {kind: 'days', names: names as string[]};
        }
        return null;
    }

    const name = weekdayKey(field);
    return name ? {kind: 'day', name} : null;
}

function pad2(n: number): string {
    return String(n).padStart(2, '0');
}

function formatTime(hour: number, minute: number, second: number): string {
    return `${pad2(hour)}:${pad2(minute)}:${pad2(second)}`;
}

function weekdayCn(name: string): string {
    return WEEKDAY_CN[name] ?? name;
}

function joinPrefix(prefix: string, text: string): string {
    return /^\d/.test(text) ? `${prefix} ${text}` : `${prefix}${text}`;
}

function describeTimePoint(hour: SimpleField, minute: SimpleField, second: SimpleField): string | null {
    if (minute.kind !== 'number' || second.kind !== 'number') return null;
    if (hour.kind === 'number') return formatTime(hour.value, minute.value, second.value);
    if (hour.kind === 'list') {
        return hour.values.map(h => formatTime(h, minute.value, second.value)).join('、');
    }
    return null;
}

function describeFrequency(hour: SimpleField, minute: SimpleField, second: SimpleField): string | null {
    if (hour.kind === 'any') {
        if (minute.kind === 'any') {
            if (second.kind === 'any') return '每秒';
            if (second.kind === 'step') return second.step === 1 ? '每秒' : `每隔 ${second.step} 秒`;
            if (second.kind === 'number') {
                return second.value === 0 ? '每分钟' : `每分钟的第 ${second.value} 秒`;
            }
            if (second.kind === 'list') return `每分钟的第 ${second.values.join('、')} 秒`;
            if (second.kind === 'range') return `每分钟的第 ${second.from}-${second.to} 秒`;
            return null;
        }

        if (minute.kind === 'step') {
            if (second.kind !== 'number') return null;
            return second.value === 0
                ? `每隔 ${minute.step} 分钟`
                : `每隔 ${minute.step} 分钟的第 ${second.value} 秒`;
        }

        if (minute.kind === 'number') {
            if (second.kind === 'number') {
                if (minute.value === 0 && second.value === 0) return '每小时整点';
                if (second.value === 0) return `每小时的第 ${minute.value} 分`;
                return `每小时的第 ${minute.value} 分 ${second.value} 秒`;
            }
            if (second.kind === 'list') {
                return `每小时的第 ${minute.value} 分的第 ${second.values.join('、')} 秒`;
            }
            return null;
        }

        if (minute.kind === 'list' && second.kind === 'number') {
            const minuteText = `每小时的第 ${minute.values.join('、')} 分`;
            return second.value === 0 ? minuteText : `${minuteText}的第 ${second.value} 秒`;
        }
        return null;
    }

    if (hour.kind === 'step') {
        if (minute.kind === 'number' && second.kind === 'number') {
            return `每隔 ${hour.step} 小时的第 ${minute.value} 分 ${second.value} 秒`;
        }
        return null;
    }

    return null;
}

function describeDayOfMonth(dom: Exclude<DomField, { kind: 'any' }>): string | null {
    switch (dom.kind) {
        case 'day':
            return `${dom.value} 号`;
        case 'days':
            return `${dom.values.join('、')} 号`;
        case 'range':
            return `${dom.from}-${dom.to} 号`;
        case 'step':
            return `从 1 号起每隔 ${dom.step} 天`;
        case 'last':
            return '最后一天';
        case 'lastOffset':
            return `倒数第 ${dom.offset} 天`;
        case 'lastWeekday':
            return '最后一个工作日';
        case 'nearestWeekday':
            return `${dom.day} 号最近的工作日`;
        default:
            return null;
    }
}

function describeMonth(month: MonthField): string | null {
    switch (month.kind) {
        case 'month':
            return `${month.value} 月`;
        case 'months':
            return `${month.values.join('、')} 月`;
        case 'range':
            return `${month.from}-${month.to} 月`;
        default:
            return null;
    }
}

function describeDate(dom: DomField, dow: DowField, month: MonthField): string | null {
    const monthAny = month.kind === 'any';
    const monthText = monthAny ? '' : describeMonth(month);
    if (!monthAny && !monthText) return null;

    if (dow.kind !== 'any') {
        // 同时指定日期和星期在 Quartz 中不支持，日期必须是 ?
        if (!monthAny || dom.kind !== 'any') return null;
        switch (dow.kind) {
            case 'day':
                return `每${weekdayCn(dow.name)}`;
            case 'days':
                return `每${dow.names.map(weekdayCn).join('、')}`;
            case 'range':
                return `每${weekdayCn(dow.from)}至${weekdayCn(dow.to)}`;
            case 'last':
                return `每月最后一个${weekdayCn(dow.name)}`;
            case 'nth':
                return `每月第 ${dow.nth} 个${weekdayCn(dow.name)}`;
            default:
                return null;
        }
    }

    if (dom.kind === 'any') {
        return monthAny ? '每天' : `每年 ${monthText}的每天`;
    }

    const dayText = describeDayOfMonth(dom);
    if (!dayText) return null;
    return monthAny ? joinPrefix('每月', dayText) : joinPrefix(`每年 ${monthText}`, dayText);
}

export class CronUtils {

    /**
     * 生成中文说明；表达式非法或暂不支持时返回空字符串。
     */
    static describe(expr: string | null | undefined): string {
        const fields = splitFields(expr);
        if (!fields) return '';

        const [secText, minText, hourText, domText, monthText, dowText] = fields;
        const second = parseSimpleField(secText, 0, 59);
        const minute = parseSimpleField(minText, 0, 59);
        const hour = parseSimpleField(hourText, 0, 23);
        const dom = parseDom(domText);
        const month = parseMonth(monthText);
        const dow = parseDow(dowText);
        if (!second || !minute || !hour || !dom || !month || !dow) return '';

        const dateAny = dom.kind === 'any' && dow.kind === 'any' && month.kind === 'any';
        const timePoint = describeTimePoint(hour, minute, second);

        if (timePoint) {
            if (dateAny) return `每天 ${timePoint}`;
            const dateText = describeDate(dom, dow, month);
            return dateText ? `${dateText} ${timePoint}` : '';
        }

        if (!dateAny) return '';
        return describeFrequency(hour, minute, second) ?? '';
    }

    /** 生成按间隔执行的表达式 */
    static buildInterval(every: number, unit: CronIntervalUnit): string {
        const n = Math.max(1, Math.floor(every || 1));
        switch (unit) {
            case 'second':
                return n === 1 ? '* * * * * ?' : `*/${n} * * * * ?`;
            case 'minute':
                return n === 1 ? '0 * * * * ?' : `0 */${n} * * * ?`;
            case 'hour':
                return n === 1 ? '0 0 * * * ?' : `0 0 */${n} * * ?`;
            case 'day':
                return n === 1 ? '0 0 0 * * ?' : `0 0 0 */${n} * ?`;
            default:
                return '';
        }
    }

    /** 生成按固定时间点执行的表达式 */
    static buildSchedule(options: CronScheduleOptions): string {
        const second = clamp(options.second ?? 0, 0, 59);
        const minute = clamp(options.minute, 0, 59);
        const hour = clamp(options.hour, 0, 23);

        switch (options.period) {
            case 'DAILY':
                return `${second} ${minute} ${hour} * * ?`;
            case 'WEEKLY':
                return `${second} ${minute} ${hour} ? * ${options.weekday || 'MON'}`;
            case 'MONTHLY': {
                const day = options.dayOfMonth === 'L' ? 'L' : clamp(options.dayOfMonth ?? 1, 1, 31);
                return `${second} ${minute} ${hour} ${day} * ?`;
            }
            case 'YEARLY': {
                const day = clamp(options.dayOfMonth === 'L' ? 1 : (options.dayOfMonth ?? 1), 1, 31);
                const month = clamp(options.month ?? 1, 1, 12);
                return `${second} ${minute} ${hour} ${day} ${month} ?`;
            }
            default:
                return '';
        }
    }

    /** 识别编辑器生成的常见表达式，用于打开弹窗时回填 */
    static parse(expr: string | null | undefined): CronParsed | null {
        const fields = splitFields(expr);
        if (!fields) return null;
        const text = fields.join(' ');

        let match = text.match(/^(\*|\*\/(\d+)) \* \* \* \* \?$/);
        if (match) {
            return {mode: 'interval', every: match[1] === '*' ? 1 : Number(match[2]), unit: 'second'};
        }
        match = text.match(/^0 (\*|\*\/(\d+)) \* \* \* \?$/);
        if (match) {
            return {mode: 'interval', every: match[1] === '*' ? 1 : Number(match[2]), unit: 'minute'};
        }
        match = text.match(/^0 0 (\*|\*\/(\d+)) \* \* \?$/);
        if (match) {
            return {mode: 'interval', every: match[1] === '*' ? 1 : Number(match[2]), unit: 'hour'};
        }
        match = text.match(/^0 0 0 (\*|\*\/(\d+)) \* \?$/);
        if (match) {
            return {mode: 'interval', every: match[1] === '*' ? 1 : Number(match[2]), unit: 'day'};
        }

        match = text.match(/^(\d+) (\d+) (\d+) \* \* \?$/);
        if (match) {
            return {
                mode: 'schedule',
                period: 'DAILY',
                second: Number(match[1]),
                minute: Number(match[2]),
                hour: Number(match[3]),
            };
        }
        match = text.match(/^(\d+) (\d+) (\d+) \? \* ([A-Za-z0-9]+)$/);
        if (match) {
            const weekday = weekdayKey(match[4]);
            if (weekday) {
                return {
                    mode: 'schedule',
                    period: 'WEEKLY',
                    second: Number(match[1]),
                    minute: Number(match[2]),
                    hour: Number(match[3]),
                    weekday,
                };
            }
        }
        match = text.match(/^(\d+) (\d+) (\d+) (L|\d+) \* \?$/);
        if (match) {
            return {
                mode: 'schedule',
                period: 'MONTHLY',
                second: Number(match[1]),
                minute: Number(match[2]),
                hour: Number(match[3]),
                dayOfMonth: match[4] === 'L' ? 'L' : Number(match[4]),
            };
        }
        match = text.match(/^(\d+) (\d+) (\d+) (\d+) (\d+) \?$/);
        if (match) {
            return {
                mode: 'schedule',
                period: 'YEARLY',
                second: Number(match[1]),
                minute: Number(match[2]),
                hour: Number(match[3]),
                dayOfMonth: Number(match[4]),
                month: Number(match[5]),
            };
        }
        return null;
    }
}

function clamp(value: number, min: number, max: number): number {
    return Math.min(max, Math.max(min, Math.floor(value)));
}
