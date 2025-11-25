/**
 * 事件总线工具类 (Static Utility Class 模式) - 所有方法和状态均为静态
 * forked form https://github.com/scottcorgan/tiny-emitter
 */

// --- 内部类型定义 ---

/** 定义事件回调的结构 */
interface StaticEventEntry<T extends any[]> {
    fn: (...args: T) => void;
    ctx?: any;
}

/** 定义用于 once 的特殊回调，用于在 off 时查找原始回调 */
type StaticOnceCallback<T extends any[]> = ((...args: T) => void) & {
    __callback?: ((...args: T) => void);
};

/** 定义事件映射的类型 */
interface StaticEventStack {
    [key: string]: StaticEventEntry<any[]>[];
}

/**
 * 静态事件总线工具类
 * 所有方法和状态均通过静态属性直接访问。
 */
export class EventBusUtils {
    // 静态内部状态
    private static __stack: StaticEventStack = {};

    /**
     * 静态工具类，无需实例化，因此构造函数私有化防止意外创建实例。
     */
    private constructor() {
        // 静态工具类，无须实现
    }

    /**
     * 注册事件监听器
     * @param name 事件名称
     * @param callback 事件回调函数
     * @param ctx 回调函数的 this 上下文
     */
    public static on<T extends any[] = []>(
        name: string,
        callback: (...args: T) => void,
        ctx?: any
    ): void { // 静态方法通常不返回 this
        if (!EventBusUtils.__stack[name]) {
            EventBusUtils.__stack[name] = [];
        }

        (EventBusUtils.__stack[name] as StaticEventEntry<T>[]).push({
            fn: callback as (...args: any[]) => void,
            ctx
        });
    }

    /**
     * 注册只触发一次的事件监听器
     * @param name 事件名称
     * @param callback 事件回调函数
     * @param ctx 回调函数的 this 上下文
     */
    public static once<T extends any[] = []>(
        name: string,
        callback: (...args: T) => void,
        ctx?: any
    ): void {
        const listener: StaticOnceCallback<T> = (...args) => {
            // 触发后立即移除
            EventBusUtils.off(name, listener as unknown as ((...args: T) => void));
            callback.apply(ctx, args);
        };

        // 存储原始回调，方便 off 时查找
        listener.__callback = callback;

        return EventBusUtils.on(name, listener, ctx);
    }

    /**
     * 触发事件
     * @param name 事件名称
     * @param args 传递给回调函数的参数
     */
    public static emit<T extends any[] = []>(
        name: string,
        ...args: T
    ): void {
        const list = EventBusUtils.__stack[name] as StaticEventEntry<T>[];

        if (list !== undefined) {
            // 使用 slice(0) 确保在回调中 off 不会影响当前循环
            list.slice(0).forEach(entry => {
                entry.fn.apply(entry.ctx, args);
            });
        }
    }

    /**
     * 移除事件监听器
     * @param name 事件名称
     * @param callback 可选：要移除的特定回调函数。如果省略，则移除该事件名的所有监听器。
     */
    public static off<T extends any[] = []>(
        name: string,
        callback?: ((...args: T) => void)
    ): void {
        const list = EventBusUtils.__stack[name];

        if (list === undefined) {
            return;
        }

        // 移除所有监听器
        if (callback === undefined) {
            delete EventBusUtils.__stack[name];
            return;
        }

        // 移除特定监听器
        const liveEvents = list.filter(
            entry =>
                // 排除当前回调
                entry.fn !== callback &&
                // 排除 once 包装的回调，通过 __callback 匹配原始回调
                (entry.fn as StaticOnceCallback<T>).__callback !== callback
        );

        if (liveEvents.length !== 0) {
            EventBusUtils.__stack[name] = liveEvents;
        } else {
            delete EventBusUtils.__stack[name];
        }
    }
}

/* 推荐的使用方式：直接通过类名调用静态方法 */
// import { EventBusUtils } from './EventBusUtils';
// EventBusUtils.on('myStaticEvent', (data) => console.log(data));
// EventBusUtils.emit('myStaticEvent', { status: 'ok' });
