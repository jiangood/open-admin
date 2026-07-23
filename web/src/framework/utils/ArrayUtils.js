export class ArrayUtils {
    static contains(arr, item) {
        return arr.indexOf(item) !== -1;
    }

    static containsAny(arr, ...items) {
        for (const item of items) {
            if (ArrayUtils.contains(arr, item)) return true;
        }
        return false;
    }

    static maxBy(arr, key) {
        if (arr == null || arr.length === 0) return undefined;
        let maxElement = undefined;
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

    static unique(arr) {
        return [...new Set(arr)];
    }
}
