/**
 * 事件总线工具类 (Singleton 模式)
 * forked form https://github.com/scottcorgan/tiny-emitter
 */

// --- 内部类型定义 ---
// 定义事件回调的结构
interface EventEntry<T extends any[]> {
    fn: (...args: T) => void;
    ctx?: any;
}

// 定义用于 once 的特殊回调
type OnceCallback<T extends any[]> = ((...args: T) => void) & {
    __callback?: ((...args: T) => void);
};

// 定义事件映射的类型
interface EventStack {
    [key: string]: EventEntry<any[]>[];
}

// --- 内部 EventBus 实现类 ---
// 为了封装内部状态和逻辑
class EventBusCore {
    // 内部私有状态
    private __stack: EventStack = {};

    // on, once, emit, off 的实现与之前相同，但现在它们是实例方法，作用于 this.__stack

    on<T extends any[] = []>(
        name: string,
        callback: (...args: T) => void,
        ctx?: any
    ): this {
        if (!this.__stack[name]) {
            this.__stack[name] = [];
        }

        (this.__stack[name] as EventEntry<T>[]).push({
            fn: callback as (...args: any[]) => void,
            ctx
        });

        return this;
    }

    once<T extends any[] = []>(
        name: string,
        callback: (...args: T) => void,
        ctx?: any
    ): this {
        const listener: OnceCallback<T> = (...args) => {
            this.off(name, listener as unknown as ((...args: T) => void));
            callback.apply(ctx, args);
        };

        listener.__callback = callback;

        return this.on(name, listener, ctx);
    }

    emit<T extends any[] = []>(
        name: string,
        ...args: T
    ): this {
        const list = this.__stack[name] as EventEntry<T>[];

        if (list !== undefined) {
            list.forEach(entry => {
                entry.fn.apply(entry.ctx, args);
            });
        }

        return this;
    }

    off<T extends any[] = []>(
        name: string,
        callback?: ((...args: T) => void)
    ): this {
        const list = this.__stack[name];

        if (list === undefined) {
            return this;
        }

        if (callback === undefined) {
            delete this.__stack[name];
            return this;
        }

        const liveEvents = list.filter(
            entry =>
                entry.fn !== callback && (entry.fn as OnceCallback<T>).__callback !== callback
        );

        if (liveEvents.length !== 0) {
            this.__stack[name] = liveEvents;
        } else {
            delete this.__stack[name];
        }

        return this;
    }
}


// --- 核心导出：单例模式 ---
// 立即创建一个 EventBusCore 的实例并将其命名为 EventBusUtils 导出
export const EventBusUtils = new EventBusCore();

/* 使用方式现在是：
import { EventBusUtils } from './EventBusUtils';
EventBusUtils.on('myEvent', (data) => console.log(data));
EventBusUtils.emit('myEvent', { value: 1 });
*/
