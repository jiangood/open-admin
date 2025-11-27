/**
 * ObjectUtils 提供了对对象进行操作的静态方法，例如安全地获取嵌套属性和复制属性。
 */
export class ObjectUtils {

    /**
     * 🎯 安全地获取深度嵌套的对象属性的值。
     * 如果属性链中的任何一级为 undefined 或 null，getDefinition 函数会返回一个默认值，而不是抛出错误。
     *
     * @template TObj 目标对象的类型。
     * @template TDefault 默认值的类型。
     * @param obj 要查找属性的对象。
     * @param path 属性路径，可以是点分隔的字符串（如 'a.b.c'）或字符串数组（如 ['a', '0', 'b', 'c']）。
     * @param defaultValue 如果属性不存在或路径中有 null/undefined 值时返回的默认值。
     * @returns 属性的值，如果找不到则返回 defaultValue。
     *
     * @example
     * const obj = { 'a': [{ 'b': { 'c': 3 } }] };
     * const value = ObjectUtils.getDefinition(obj, 'a[0].b.c', 0); // 3
     * const missing = ObjectUtils.getDefinition(obj, 'a[1].d', 'default'); // 'default'
     */
    static get<TObj extends object, TDefault = unknown>(
        obj: TObj | null | undefined,
    path: string | (keyof TObj)[],
    defaultValue: TDefault | undefined = undefined
): unknown | TDefault {

    // 路径处理：将 'a[0].b.c' 转换为 ['a', '0', 'b', 'c'] 以支持数组索引
    // 注意：这里简化处理，只处理点分隔符，如果需要完整的 lodash getDefinition 行为，需要更复杂的正则解析。
    const pathArray: string[] = Array.isArray(path)
        ? path.map(String) // 确保路径段都是字符串
        : path.split('.');

    let result: any = obj;

    // 遍历路径
    for (const segment of pathArray) {
    // 如果当前结果是 null 或 undefined，则后续路径无法访问，返回默认值
    if (result == null) {
    return defaultValue;
}

// 尝试访问属性
// 使用 segment 作为索引，TypeScript 默认这里是合法的
result = result[segment];
}

// 如果最终结果是 null 或 undefined，则返回默认值；否则返回结果
return result !== null && result !== undefined ? result : defaultValue;
}


/**
 * 📋 复制对象属性，仅复制源对象 (source) 中 **存在** 且目标对象 (target) 中 **也有** 对应属性的那些值。
 * 主要用于根据目标对象的结构来过滤和填充数据。
 *
 * @param source 源对象。
 * @param target 目标对象。
 * @returns void
 */
static copyPropertyIfPresent<TSource extends object, TTarget extends object>(
    source: TSource | null | undefined,
    target: TTarget | null | undefined
): void {
    if (!source || !target || typeof source !== 'object' || typeof target !== 'object') {
    return;
}

// 遍历目标对象的键，确保我们只复制目标对象上已有的属性
const keys = Object.keys(target) as (keyof TTarget)[];

for (const key of keys) {
    // 检查源对象是否有这个属性
    if (Object.hasOwn(source, key)) {
        // 因为目标对象和源对象都被约束为 object，所以这里的类型转换是相对安全的
        const value = (source as any)[key];
        (target as any)[key] = value;
    }
}
}


/**
 * 📝 复制对象属性，将源对象 (source) 中 **非 undefined** 的属性值复制到目标对象 (target) 对应的属性上。
 * 仅复制目标对象 (target) 中 **已有** 的属性，如果源对象中的值是 undefined 则不复制。
 *
 * @param source 源对象。
 * @param target 目标对象。
 * @returns void
 */
static copyProperty<TSource extends object, TTarget extends object>(
    source: TSource | null | undefined,
    target: TTarget | null | undefined
): void {
    if (!source || !target || typeof source !== 'object' || typeof target !== 'object') {
    return;
}

// 遍历目标对象的键
const keys = Object.keys(target) as (keyof TTarget)[];

for (const key of keys) {
    // 尝试从源对象获取值
    const value = (source as any)[key];

    // 只有当值明确不是 undefined 时才复制（即允许复制 null 或其他 falsy 值）
    if (value !== undefined) {
        (target as any)[key] = value;
    }
}
}

}
