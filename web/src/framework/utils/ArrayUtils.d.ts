export class ArrayUtils {
    static contains<T>(arr: T[], item: T): boolean;
    static containsAny<T>(arr: T[], ...items: T[]): boolean;
    static maxBy<T extends Record<K, any>, K extends keyof T>(arr: T[], key: K): T | undefined;
    static unique<T>(arr: T[]): T[];
}
