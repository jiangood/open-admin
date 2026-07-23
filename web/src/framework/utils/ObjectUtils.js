export class ObjectUtils {

    static copyPropertyIfPresent(source, target) {
        if (!source || !target || typeof source !== 'object' || typeof target !== 'object') return;
        const keys = Object.keys(target);
        for (const key of keys) {
            if (Object.hasOwn(source, key)) {
                target[key] = source[key];
            }
        }
    }
}
