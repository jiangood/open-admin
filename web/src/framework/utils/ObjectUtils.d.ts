import {ObjectUtils} from './ObjectUtils';

declare module './ObjectUtils' {
    interface ObjectUtils {
        // Empty - no public methods to declare
    }
}

export class ObjectUtils {
    static copyPropertyIfPresent<TSource extends object, TTarget extends object>(
        source: TSource | null | undefined,
        target: TTarget | null | undefined
    ): void;
}
