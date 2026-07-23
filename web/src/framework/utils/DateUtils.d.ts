export class DateUtils {
    static convertTypeToFormat(type: string): string;
    static year(date: Date): number;
    static month(date: Date): string;
    static date(date: Date): string;
    static hour(date: Date): string;
    static minute(date: Date): string;
    static second(date: Date): string;
    static formatDate(d: Date): string;
    static formatTime(d: Date): string;
    static formatDateTime(d: Date): string;
    static formatDateCn(d: Date): string;
    static now(): string;
    static today(): string;
    static thisYear(): number;
    static thisMonth(): string;
    static friendlyTime(pastDate: Date | string | number): string | undefined;
    static friendlyTotalTime(time: number | string | null): string | null;
    static beginOfMonth(): string;
}
