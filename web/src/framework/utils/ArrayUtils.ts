/**
 * 数组工具类
 * 提供了一系列对数组进行操作的静态方法。
 */
export class ArrayUtils {

    /**
     * 检查数组是否包含某个元素。
     * @param arr 目标数组。
     * @param item 要检查的元素。
     * @returns 如果包含则返回 true，否则返回 false。
     */
    static contains<T>(arr: T[], item: T): boolean {
        return arr.indexOf(item) !== -1;
    }

    /**
     * 检查数组是否包含至少一个指定的元素。
     *
     * @param arr 目标数组。
     * @param items 要检查的一个或多个元素。
     * @returns 如果包含任意一个元素则返回 true，否则返回 false。
     */
    static containsAny<T>(arr: T[], ...items: T[]): boolean {
        for (const item of items) {
            if (ArrayUtils.contains(arr, item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在数组末尾添加一个元素。
     * @param arr 目标数组。
     * @param item 要添加的元素。
     */
    static add<T>(arr: T[], item: T): void {
        arr.push(item);
    }

    /**
     * 在数组的指定索引处添加一个元素。
     * @param arr 目标数组。
     * @param index 插入位置的索引。
     * @param item 要添加的元素。
     */
    static addAt<T>(arr: T[], index: number, item: T): void {
        arr.splice(index, 0, item);
    }

    /**
     * 将另一个数组的所有元素追加到目标数组的尾部。
     * @param arr 目标数组。
     * @param items 要追加的元素数组。
     */
    static addAll<T>(arr: T[], items: T[]): void {
        arr.push(...items);
    }

    /**
     * 移除数组指定索引处的元素。
     * @param arr 目标数组。
     * @param index 要移除元素的索引。
     */
    static removeAt<T>(arr: T[], index: number): void {
        if (index >= 0 && index < arr.length) {
            arr.splice(index, 1);
        }
    }

    /**
     * 移除数组中第一个匹配的元素。
     * @param arr 目标数组。
     * @param item 要移除的元素。
     */
    static remove<T>(arr: T[], item: T): void {
        const index = arr.indexOf(item);
        if (index !== -1) {
            ArrayUtils.removeAt(arr, index);
        }
    }

    /**
     * 清空数组。
     * @param arr 目标数组。
     */
    static clear<T>(arr: T[]): void {
        arr.length = 0;
    }

    /**
     * 截取数组的一个子集。
     * @param arr 目标数组。
     * @param fromIndex 开始索引（包含）。
     * @param toIndex 结束索引（不包含）。
     * @returns 截取后的新数组。
     */
    static sub<T>(arr: T[], fromIndex?: number, toIndex?: number): T[] {
        return arr.slice(fromIndex, toIndex);
    }

    /**
     * 交换数组中两个元素的位置。
     * @param arr 目标数组。
     * @param item1 元素1。
     * @param item2 元素2。
     */
    static swap<T>(arr: T[], item1: T, item2: T): void {
        const index1 = arr.indexOf(item1);
        const index2 = arr.indexOf(item2);

        if (index1 !== -1 && index2 !== -1) {
            [arr[index1], arr[index2]] = [arr[index2], arr[index1]];
        }
    }

    /**
     * 在数组的指定索引处插入一个元素（与 addAt 相同，保留以兼容原 API）。
     * @param arr 目标数组。
     * @param index 插入位置的索引。
     * @param item 要插入的元素。
     */
    static insert<T>(arr: T[], index: number, item: T): void {
        ArrayUtils.addAt(arr, index, item);
    }

    /**
     * 如果元素不存在于数组中，则将其添加到数组末尾。
     * @param arr 目标数组。
     * @param item 要添加的元素。
     */
    static pushIfNotExist<T>(arr: T[], item: T): void {
        if (!ArrayUtils.contains(arr, item)) {
            arr.push(item);
        }
    }

    /**
     * 将新数组中的所有元素添加到目标数组的末尾。
     * @param arr 目标数组。
     * @param newArr 要添加的元素数组。
     */
    static pushAll<T>(arr: T[], newArr: T[]): void {
        ArrayUtils.addAll(arr, newArr);
    }

    /**
     * 获取对象数组中某一属性值最大的对象。
     *
     * @param arr 对象数组。
     * @param key 用于比较的属性名。
     * @returns 属性值最大的对象，如果数组为空则返回 undefined。
     */
    static maxBy<T extends Record<K, any>, K extends keyof T>(
        arr: T[],
        key: K
    ): T | undefined {
        if (arr == null || arr.length === 0) {
            return undefined;
        }

        let maxElement: T | undefined = undefined;
        let maxValue: number = -Infinity;

        for (const element of arr) {
            const value = element[key];

            if (typeof value === 'number' && value > maxValue) {
                maxValue = value;
                maxElement = element;
            }
        }

        return maxElement;
    }

    /**
     * 对数组进行去重。
     * @param arr 目标数组。
     * @returns 去重后的新数组。
     */
    static unique<T>(arr: T[]): T[] {
        return [...new Set(arr)];
    }
}
