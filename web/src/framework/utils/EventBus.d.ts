export class EventBus {
    static on<T extends any[] = []>(name: string, callback: (...args: T) => void, ctx?: any): void;
    static once<T extends any[] = []>(name: string, callback: (...args: T) => void, ctx?: any): void;
    static emit<T extends any[] = []>(name: string, ...args: T): void;
    static off<T extends any[] = []>(name: string, callback?: (...args: T) => void): void;
}
