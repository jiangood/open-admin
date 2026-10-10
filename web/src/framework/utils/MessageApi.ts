import {message as staticMessage} from 'antd';
import type {MessageInstance} from 'antd/es/message/interface';

/**
 * message 实例持有者。
 * 静态 message 无法消费 ConfigProvider 主题（暗色模式下仍是浅色），
 * 由布局内的 MessageBridge 通过 App.useApp() 注入跟随主题的实例。
 */
let messageInstance: MessageInstance = staticMessage;

/** 注入 App 上下文中的 message 实例；不传则恢复为静态 message */
export function setMessageApi(instance?: MessageInstance) {
    messageInstance = instance ?? staticMessage;
}

export function getMessageApi(): MessageInstance {
    return messageInstance;
}
