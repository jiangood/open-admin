export class StorageUtils {
    static get<T>(key: string, defaultValue?: T | null): T | null;
    static set<T>(key: string, value: T | null | undefined): void;
}
