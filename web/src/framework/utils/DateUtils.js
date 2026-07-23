import {StringUtils} from './StringUtils';

export class DateUtils {
    static convertTypeToFormat(type) {
        if (type === 'YEAR') type = 'YYYY';
        else if (type === 'YEAR_MONTH') type = 'YYYY-MM';
        else if (type === 'YEAR_QUARTER') type = 'YYYY-QQ';
        else if (type === 'DAY') type = 'YYYY-MM-DD';
        return type;
    }

    static year(date) { return date.getFullYear(); }
    static month(date) { return StringUtils.pad(date.getMonth() + 1, 2); }
    static date(date) { return StringUtils.pad(date.getDate(), 2); }
    static hour(date) { return StringUtils.pad(date.getHours(), 2); }
    static minute(date) { return StringUtils.pad(date.getMinutes(), 2); }
    static second(date) { return StringUtils.pad(date.getSeconds(), 2); }

    static formatDate(d) {
        return this.year(d) + '-' + this.month(d) + '-' + this.date(d);
    }

    static formatTime(d) {
        return this.hour(d) + ':' + this.minute(d) + ':' + this.second(d);
    }

    static formatDateTime(d) {
        return this.formatDate(d) + ' ' + this.formatTime(d);
    }

    static formatDateCn(d) {
        return this.year(d) + '年' + (d.getMonth() + 1) + '月' + d.getDate() + '日';
    }

    static now() { return this.formatDateTime(new Date()); }
    static today() { return this.formatDate(new Date()); }
    static thisYear() { return this.year(new Date()); }
    static thisMonth() { return this.month(new Date()); }

    static friendlyTime(pastDate) {
        if (pastDate == null) return undefined;
        if (!(pastDate instanceof Date)) pastDate = new Date(pastDate);
        const currentDate = new Date();
        let elapsedMilliseconds = currentDate.getTime() - pastDate.getTime();
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
        if (elapsedDays >= 7) { const weeks = Math.floor(elapsedDays / 7); return `${weeks} 周${suffix}`; }
        if (elapsedDays >= 1) return `${elapsedDays} 天${suffix}`;
        if (elapsedHours >= 1) return `${elapsedHours} 小时${suffix}`;
        if (elapsedMinutes >= 1) return `${elapsedMinutes} 分钟${suffix}`;
        return `${elapsedSeconds} 秒${suffix}`;
    }

    static friendlyTotalTime(time) {
        if (time == null || time === '-') return null;
        let seconds;
        if (typeof time === 'string') seconds = parseInt(time, 10) / 1000;
        else seconds = time / 1000;
        seconds = Math.floor(seconds);
        if (seconds < 60) return seconds + '秒';
        let min = seconds / 60;
        seconds = seconds % 60;
        min = Math.floor(min);
        seconds = Math.floor(seconds);
        return min + '分' + seconds + '秒';
    }

    static beginOfMonth() {
        const d = new Date();
        d.setDate(1);
        return this.formatDate(d);
    }
}
