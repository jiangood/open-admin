export class EventBus {
    static __stack = {};

    static on(name, callback, ctx) {
        if (!EventBus.__stack[name]) {
            EventBus.__stack[name] = [];
        }
        EventBus.__stack[name].push({ fn: callback, ctx });
    }

    static once(name, callback, ctx) {
        const listener = (...args) => {
            EventBus.off(name, listener);
            callback.apply(ctx, args);
        };
        listener.__callback = callback;
        return EventBus.on(name, listener, ctx);
    }

    static emit(name, ...args) {
        const list = EventBus.__stack[name];
        if (list !== undefined) {
            list.slice(0).forEach(entry => {
                entry.fn.apply(entry.ctx, args);
            });
        }
    }

    static off(name, callback) {
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
