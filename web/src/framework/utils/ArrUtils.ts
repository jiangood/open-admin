/**
 * 数组工具类
 * 提供了一系列对数组进行操作的静态方法。
 */
export class ArrUtils {

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
     * 注意：原始实现中的 this.contains(item) 逻辑错误，已修正为检查 arr 是否包含 item。
     *
     * @param arr 目标数组。
     * @param items 要检查的一个或多个元素。
     * @returns 如果包含任意一个元素则返回 true，否则返回 false。
     */
    static containsAny<T>(arr: T[], ...items: T[]): boolean {
        for (const item of items) {
            // 这里调用自身的静态方法
            if (ArrUtils.contains(arr, item)) {
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
        // 使用 ES6 的 spread 运算符或 push.apply 效率更高
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
            // 这里调用自身的静态方法
            ArrUtils.removeAt(arr, index);
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
    // 使用解构赋值进行交换，更简洁
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
    ArrUtils.addAt(arr, index, item);
}

/**
 * 如果元素不存在于数组中，则将其添加到数组末尾。
 * @param arr 目标数组。
 * @param item 要添加的元素。
 */
static pushIfNotExist<T>(arr: T[], item: T): void {
    if (!ArrUtils.contains(arr, item)) {
    arr.push(item);
}
}

/**
 * 将新数组中的所有元素添加到目标数组的末尾。
 * @param arr 目标数组。
 * @param newArr 要添加的元素数组。
 */
static pushAll<T>(arr: T[], newArr: T[]): void {
    ArrUtils.addAll(arr, newArr);
}

/**
 * 获取对象数组中某一属性值最大的对象。
 * 使用泛型 K 约束 key 必须是 T 的属性，并确保属性值可以进行数值比较。
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
        // 运行时类型检查（虽然 TS 层面已约束 K，但值可能不是数字）
        const value = element[key];

        // 确保比较的是数字类型
        if (typeof value === 'number' && value > maxValue) {
            maxValue = value;
            maxElement = element;
        } else if (typeof value !== 'number') {
            // 可选：如果遇到非数字，可以跳过或根据需求抛出错误/记录警告
            // console.warn(`Element with key ${String(key)} has non-numeric value: ${value}`);
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

// 示例用法（可选）
/*
interface User {
    id: number;
    score: number;
    name: string;
}

const users: User[] = [
    { id: 1, score: 90, name: 'Alice' },
    { id: 2, score: 95, name: 'Bob' },
    { id: 3, score: 88, name: 'Charlie' },
];

const maxUser = ArrUtil.maxBy(users, 'score');
console.log(maxUser); // { id: 2, score: 95, name: 'Bob' }

const numbers = [1, 2, 2, 3, 4, 1];
const uniqueNumbers = ArrUtil.unique(numbers);
console.log(uniqueNumbers); // [1, 2, 3, 4]

let list = [10, 20];
ArrUtil.register(list, 30);
console.log(list); // [10, 20, 30]

ArrUtil.remove(list, 20);
console.log(list); // [10, 30]

ArrUtil.addAll(list, [40, 50]);
console.log(list); // [10, 30, 40, 50]
*/
