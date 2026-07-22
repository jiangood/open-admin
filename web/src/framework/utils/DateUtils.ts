import {StringUtils} from './StringUtils';

export class DateUtils {

    public static convertTypeToFormat(type) {
        if (type === 'YEAR') {
            type = 'YYYY'
        } else if (type === 'YEAR_MONTH') {
            type = 'YYYY-MM'
        } else if (type === 'YEAR_QUARTER') {
            type = 'YYYY-QQ'
        } else if (type === 'DAY') {
            type = 'YYYY-MM-DD'
        }
        return type;
    }

    public static year(date: Date): number {
        return date.getFullYear();
    }

    /**
     * 获取月份，自动补0
     * @param date
     * @returns {string}
     */
    public static month(date: Date): string {
        const n = date.getMonth() + 1; // （注意月份从0开始，所以要加1）
        return StringUtils.pad(n, 2);
    }

    /**
     * 获取日期，
     * @param date
     */
    public static date(date: Date): string {
        return StringUtils.pad(date.getDate(), 2);
    }

    /**
     * 小时，24进制
     * @param date
     * @returns {string}
     */
    public static hour(date: Date): string {
        return StringUtils.pad(date.getHours(), 2);
    }

    public static minute(date: Date): string {
        return StringUtils.pad(date.getMinutes(), 2);
    }

    public static second(date: Date): string {
        return StringUtils.pad(date.getSeconds(), 2);
    }

    public static formatDate(d: Date): string {
        return this.year(d) + '-' + this.month(d) + '-' + this.date(d);
    }

    public static formatTime(d: Date): string {
        return this.hour(d) + ':' + this.minute(d) + ':' + this.second(d);
    }

    public static formatDateTime(d: Date): string {
        return this.formatDate(d) + ' ' + this.formatTime(d);
    }

    /**
     *
     * @param d
     * @returns {string} 2020年1月30日
     */
    public static formatDateCn(d: Date): string {
        return this.year(d) + '年' + (d.getMonth() + 1) + '月' + d.getDate() + '日';
    }

    /***
     当前时间, 如 2022-01-23 11:59:59
     */
    public static now(): string {
        return this.formatDateTime(new Date());
    }

    /**
     * 当前日期 ，如 2022-01-23
     *
     */
    public static today(): string {
        return this.formatDate(new Date());
    }

    public static thisYear(): number {
        return this.year(new Date());
    }

    public static thisMonth(): string {
        return this.month(new Date());
    }

    /**
     * 显示友好时间，如 2小时前，1周前
     * @param pastDate 日期, 支持Date，String，Number
     */
    public static friendlyTime(pastDate: Date | string | number): string | undefined {
        if (pastDate == null) {
            return undefined;
        }
        if (!(pastDate instanceof Date)) {
            pastDate = new Date(pastDate);
        }

        const currentDate = new Date();
        let elapsedMilliseconds = currentDate.getTime() - pastDate.getTime();
        const suffix = elapsedMilliseconds > 0 ? '前' : '后';
        elapsedMilliseconds = Math.abs(elapsedMilliseconds);

        // 计算年、月、日、小时、分钟和秒的差值
        const elapsedYears = Math.floor(elapsedMilliseconds / (1000 * 60 * 60 * 24 * 365));
        const elapsedMonths = Math.floor(elapsedMilliseconds / (1000 * 60 * 60 * 24 * 30));
        const elapsedDays = Math.floor(elapsedMilliseconds / (1000 * 60 * 60 * 24));
        const elapsedHours = Math.floor(elapsedMilliseconds / (1000 * 60 * 60));
        const elapsedMinutes = Math.floor(elapsedMilliseconds / (1000 * 60));
        const elapsedSeconds = Math.floor(elapsedMilliseconds / 1000);

        // 根据差值选择友好的格式
        if (elapsedYears >= 1) {
            return `${elapsedYears} 年${suffix}`;
        }
        if (elapsedMonths >= 1) {
            return `${elapsedMonths} 个月${suffix}`;
        }
        if (elapsedDays >= 7) {
            const weeks = Math.floor(elapsedDays / 7);
            return `${weeks} 周${suffix}`;
        }
        if (elapsedDays >= 1) {
            const days = elapsedDays;
            return `${days} 天${suffix}`;
        }
        if (elapsedHours >= 1) {
            return `${elapsedHours} 小时${suffix}`;
        }
        if (elapsedMinutes >= 1) {
            return `${elapsedMinutes} 分钟${suffix}`;
        }
        return `${elapsedSeconds} 秒${suffix}`;
    }

    /**
     * 总共耗时, 如 3分5秒
     * @param time 数字 （Date.getTime）
     * @returns {string|null}
     */
    public static friendlyTotalTime(time: number | string | null): string | null {
        if (time == null || time === '-') {
            return null;
        }

        let seconds: number;
        if (typeof time === 'string') {
            seconds = parseInt(time, 10) / 1000;
        } else {
            seconds = time / 1000;
        }

        seconds = Math.floor(seconds);

        if (seconds < 60) {
            return seconds + '秒';
        }

        let min = seconds / 60;
        seconds = seconds % 60;

        min = Math.floor(min);
        seconds = Math.floor(seconds);

        return min + '分' + seconds + '秒';
    }

    public static beginOfMonth(): string {
        const d = new Date();
        d.setDate(1);
        return this.formatDate(d);
    }
}
