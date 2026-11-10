import {Modal} from 'antd';
import type {ModalStaticFunctions} from 'antd/es/modal/confirm';

/** 框架对外暴露的 Modal 能力，与静态 Modal / App.useApp().modal 保持一致 */
export type ModalApi = Pick<ModalStaticFunctions, 'info' | 'success' | 'error' | 'warning' | 'confirm'>;

let modalInstance: ModalApi = Modal;

/**
 * 注入 App 上下文中的 modal 实例；不传则恢复为静态 Modal。
 * 静态 Modal 无法消费 ConfigProvider 主题（暗色模式下仍是浅色），
 * 由布局内的 AppBridge 通过 App.useApp() 注入跟随主题的实例。
 */
export function setModalApi(instance?: ModalApi) {
    modalInstance = instance ?? Modal;
}

export function getModalApi(): ModalApi {
    return modalInstance;
}
