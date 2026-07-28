export class ArrayUtils {
    static contains<T>(arr: T[], item: T): boolean {
        return arr.indexOf(item) !== -1;
    }

    static containsAny<T>(arr: T[], ...items: T[]): boolean {
        for (const item of items) {
            if (ArrayUtils.contains(arr, item)) return true;
        }
        return false;
    }

    static maxBy<T extends Record<K, any>, K extends keyof T>(arr: T[], key: K): T | undefined {
        if (arr == null || arr.length === 0) return undefined;
        let maxElement: T | undefined = undefined;
        let maxValue = -Infinity;
        for (const element of arr) {
            const value = element[key];
            if (typeof value === 'number' && value > maxValue) {
                maxValue = value;
                maxElement = element;
            }
        }
        return maxElement;
    }

    static unique<T>(arr: T[]): T[] {
        return [...new Set(arr)];
    }
}
