export class ObjectUtils {

    static copyPropertyIfPresent<TSource extends object, TTarget extends object>(
        source: TSource | null | undefined,
        target: TTarget | null | undefined
    ): void {
        if (!source || !target || typeof source !== 'object' || typeof target !== 'object') return;
        const keys = Object.keys(target);
        for (const key of keys) {
            if (Object.hasOwn(source, key)) {
                (target as Record<string, unknown>)[key] = (source as Record<string, unknown>)[key];
            }
        }
    }
}
