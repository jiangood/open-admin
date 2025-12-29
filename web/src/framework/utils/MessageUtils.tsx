import {Input, InputNumber, message, Modal} from 'antd';
import type {ModalFuncProps} from 'antd/es/modal/interface';
import React from 'react';
import {MessageInstance} from "antd/lib/message/interface";
import {HookAPI} from "antd/lib/modal/useModal";



/**
 * 消息工具类 (MsgUtils)
 * 封装了 Ant Design 的 Modal, message, 和 notification 静态方法
 */
export class MessageUtils {

    /**
     * 弹出 Alert 提示框
     */
    static alert(content: React.ReactNode, config?: Omit<ModalFuncProps, 'content' | 'icon' | 'onOk' | 'onCancel'>) {
        this.modalApi.info({
            title: '提示',
            content,
            okText: '确定',
            icon: null,
            ...config,
        });
    }

    /**
     * 弹出 Confirm 确认框
     */
    static confirm(content: React.ReactNode, config?: Omit<ModalFuncProps, 'content' | 'icon' | 'onOk' | 'onCancel'>) {
        return new Promise((resolve) => {
            this.modalApi.confirm({
                title: '确认操作',
                content,
                okText: '确定',
                cancelText: '取消',
                onOk: () => resolve(true),
                onCancel: () => resolve(false),
                ...config,
            });
        })

    }

    /**
     * 弹出 Prompt 输入框对话框, 如果默认值是数字, 则使用 InputNumber 输入框
     */
    static prompt(message: React.ReactNode, initialValue?: string|number, placeholder?: string, config?: Omit<ModalFuncProps, 'content' | 'title' | 'icon' | 'onOk'>) {
        const isNumber = typeof initialValue === 'number';
        return new Promise((resolve) => {
            const ref = React.createRef()
            this.modalApi.confirm({
                icon: null,
                title: '提示',
                content: <div>
                    <div style={{marginBottom: 4}}>{message}</div>
                    {isNumber ? <InputNumber value={initialValue} placeholder={placeholder}/> : <Input ref={ref} placeholder={placeholder}/>}
                </div>,
                okText: '确定',
                cancelText: '取消',
                onOk: () => {
                    const inputInstance = ref.current;
                    const inputElement = inputInstance.input;
                    const inputValue = inputElement.value;
                    resolve(inputValue)
                },
                onCancel: () => {
                    resolve()
                },
                ...config,
            });
        })
    }

    // --- Antd message 封装 (全局通知/Loading) ---

    /**
     * 成功消息
     */
    static success(content: React.ReactNode, duration: number = 3) {
        this.messageApi.success(content, duration);
    }

    /**
     * 错误消息
     */
    static error(content: React.ReactNode, duration: number = 3) {
        this.messageApi.error(content, duration);
    }

    /**
     * 警告消息
     */
    static warning(content: React.ReactNode, duration: number = 3) {
        this.messageApi.warning(content, duration);
    }

    /**
     * 通用消息
     */
    static info(content: React.ReactNode, duration: number = 3) {
        this.messageApi.info(content, duration);
    }

    /**
     * 弹出 Loading 提示
     */
    static loading(content: React.ReactNode = '正在加载...', duration?: number) {
        return this.messageApi.loading({content, duration: duration === undefined ? 0 : duration});
    }

    static config(messageApi: MessageInstance, modalApi: HookAPI) {
        this.messageApi = messageApi;
        this.modalApi = modalApi;
    }

    private static modalApi:HookAPI = null;
    private static messageApi:MessageInstance = null;
}

/**
 * antd6 推荐使用这个hooks，这里统一设置， 供公共layout使用
 * @constructor
 */
export function MessageHolder(){
    const [modalApi, modalContextHolder] = Modal.useModal();
    const [messageApi, messageContextHolder] = message.useMessage();
    MessageUtils.config(messageApi,modalApi);
    return <>
        {modalContextHolder} {messageContextHolder}
    </>
}
