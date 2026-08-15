type EventCallback = (...args: unknown[]) => void;

interface EventEntry {
    fn: EventCallback & { __callback?: EventCallback };
    ctx?: unknown;
}

export class EventBus {
    private static __stack: Record<string, EventEntry[]> = {};

    static on<T extends unknown[] = []>(name: string, callback: (...args: T) => void, ctx?: unknown): () => void {
        if (!EventBus.__stack[name]) {
            EventBus.__stack[name] = [];
        }
        EventBus.__stack[name].push({ fn: callback, ctx });
        return () => EventBus.off(name, callback);
    }

    static once<T extends unknown[] = []>(name: string, callback: (...args: T) => void, ctx?: unknown): () => void {
        const unsubscribe = () => EventBus.off(name, callback);
        const listener: EventEntry['fn'] = (...args: unknown[]) => {
            EventBus.off(name, listener);
            callback.apply(ctx, args);
        };
        listener.__callback = callback;
        EventBus.__stack[name] = EventBus.__stack[name] || [];
        EventBus.__stack[name].push({ fn: listener, ctx });
        return unsubscribe;
    }

    static emit<T extends unknown[] = []>(name: string, ...args: T): void {
        const list = EventBus.__stack[name];
        if (list !== undefined) {
            list.slice(0).forEach(entry => {
                entry.fn.apply(entry.ctx, args);
            });
        }
    }

    static off<T extends unknown[] = []>(name: string, callback?: (...args: T) => void): void {
        const list = EventBus.__stack[name];
        if (list === undefined) return;
        if (callback === undefined) {
            delete EventBus.__stack[name];
            return;
        }
        const liveEvents = list.filter(
            entry =>
                entry.fn !== callback &&
                entry.fn.__callback !== callback
        );
        if (liveEvents.length !== 0) {
            EventBus.__stack[name] = liveEvents;
        } else {
            delete EventBus.__stack[name];
        }
    }
}
