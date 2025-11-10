/**
 *  于安全地获取深度嵌套的对象属性的值
 *  如果属性链中的任何一级为 undefined 或 null，get 函数会返回一个默认值，而不是抛出错误。
 *
 *  const obj = { 'a': [{ 'b': { 'c': 3 } }] };
 *
 * const value = get(obj, 'a[0].b.c');
 *
 * @param obj
 * @param path
 * @param defaultValue
 */
export function get(obj, path, defaultValue = undefined){

    const pathArray = Array.isArray(path) ? path : path.split('.');
    let result = obj;

    // 遍历路径
    for (const segment of pathArray) {
        if (result == null) {
            return defaultValue;
        }

        // 尝试访问属性
        result = result[segment];
    }

    // 如果结果还是 null 或 undefined，则返回默认值
    return result != null ? result : defaultValue;

}


export class ObjUtil {

    /**
     * 复制对象属性
     * 如果目标对象的属性存在才复制
     * @param source
     * @param target
     */
   static copyPropertyIfPresent(source, target){
        if (!source || !target || typeof source !== 'object' || typeof target !== 'object') {
            return;
        }

       const keys = Object.keys(target)
        for (let key of keys) {
            if(Object.hasOwn(source,key)){
                let value = source[key];
                target[key] = value
            }
        }
    }


    static copyProperty(source, target){
        if (!source || !target || typeof source !== 'object' || typeof target !== 'object') {
            return;
        }

        const keys = Object.keys(target)
        for (let key of keys) {
                let value = source[key];
                if(value !== undefined){
                    target[key] = value
                }

        }
    }

}
