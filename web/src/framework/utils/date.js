import {StrUtil} from "./str";


export const DateUtil = {

     year(date) {
        return date.getFullYear();
    },

    /**
     *  获取月份， 自动补0
     * @param date
     * @returns {*}
     */
     month(date) {
        const n = date.getMonth() + 1; // （注意月份从0开始，所以要加1）
        return StrUtil.pad(n, 2)
    },

    /**
     * 获取日期，
     * @param date
     */
     date(date) {
        return StrUtil.pad(date.getDate(), 2)
    },

    /**
     * 小时， 24进制
     * @param date
     * @returns {string}
     */
     hour(date) {
        return StrUtil.pad(date.getHours(), 2);
    },

     minute(date) {
        return StrUtil.pad(date.getMinutes(), 2);
    },

     second(date) {
        return StrUtil.pad(date.getSeconds(), 2);
    },

     formatDate(d) {
        return this.year(d) + '-' + this.month(d) + "-" + this.date(d)
    },

     formatTime(d) {
        return this.hour(d) + ':' + this.minute(d) + ":" + this.second(d)
    },

     formatDateTime(d) {
        return this.formatDate(d) + " " + this.formatTime(d)
    },

    /**
     *
     * @param d
     * @returns {string} 2020年1月30日
     */
     formatDateCn(d) {
        return this.year(d) + '年' + (d.getMonth() + 1) + '月' + d.getDate() + '日'
    },

    /***
     当前时间, 如 2022-01-23 11:59:59
     */
     now() {
        return this.formatDateTime(new Date());
    },

    /**
     * 当前日期 ，如 2022-01-23
     *
     */
     today() {
        return this.formatDate(new Date());
    },

     thisYear() {
        return this.year(new Date())
    },

     thisMonth() {
        return this.month(new Date())
    },


    /**
     * 显示友好时间， 如 2小时前， 1周前
     * @param pastDate 日期, 支持Date， String， Number
     */
     friendlyTime(pastDate) {
        if (pastDate == null) {
            return
        }
        if (!(pastDate instanceof Date)) {
            pastDate = new Date(pastDate)
        }

        const currentDate = new Date();
        let elapsedMilliseconds = currentDate - pastDate;
        const suffix = elapsedMilliseconds > 0 ? "前" : "后";
        elapsedMilliseconds = Math.abs(elapsedMilliseconds)

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
    },

    /**
     * 总共耗时, 如 3分5秒
     * @param time 数字 （Date.getTime）
     * @returns {string|null}
     */
     friendlyTotalTime(time) {
        if (time == null || time === '-') {
            return null
        }
        let seconds = time / 1000;

        seconds = Math.floor(seconds)

        if (seconds < 60) {
            return seconds + '秒';
        }

        let min = seconds / 60;
        seconds = seconds % 60;

        min = Math.floor(min);
        seconds = Math.floor(seconds)

        return min + '分' + seconds + '秒'
    },

    beginOfMonth(){
        const d = new Date();
        d.setMonth(0)
        d.setDate(1)
        return this.formatDate(d)
    }


}
