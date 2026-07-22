import {Form, Input, InputNumber, message, Modal} from 'antd';
import type {ModalFuncProps} from 'antd/es/modal/interface';
import React from 'react';

/**
 * 消息工具类
 * 直接使用 Ant Design 6 的静态方法，无需 hook 初始化
 */
export class MessageUtils {

    static alert(content: any, config?: Omit<ModalFuncProps, 'content' | 'icon' | 'onOk' | 'onCancel'>) {
        return new Promise(resolve => {
            Modal.info({
                title: '提示',
                content,
                okText: '确定',
                onOk: () => resolve(),
                icon: null,
                ...config,
            });
        })
    }

    static confirm(content: React.ReactNode, config?: Omit<ModalFuncProps, 'content' | 'icon' | 'onOk' | 'onCancel'>) {
        return new Promise((resolve) => {
            Modal.confirm({
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

    static prompt(msg: React.ReactNode, initialValue?: string | number, placeholder?: string, config?: Omit<ModalFuncProps, 'content' | 'title' | 'icon' | 'onOk'>) {
        const isNumber = typeof initialValue === 'number';
        return new Promise((resolve) => {
            const ref = React.createRef()
            let element: any = isNumber ? <InputNumber placeholder={placeholder}/> : <Input placeholder={placeholder}/>;
            const content: any = <div>
                <div style={{marginBottom: 4}}>{msg}</div>
                <Form ref={ref}>
                    <Form.Item name='inputValue' initialValue={initialValue}>
                        {element}
                    </Form.Item>
                </Form>
            </div>;
            Modal.confirm({
                icon: null,
                title: '提示',
                content,
                okText: '确定',
                cancelText: '取消',
                onOk: () => {
                    const form = ref.current;
                    const values = form.getFieldsValue()
                    resolve(values.inputValue)
                },
                onCancel: () => {
                    resolve()
                },
                ...config,
            });
        })
    }

    static success(content: string, duration: number = 3) {
        message.success(content, duration);
    }

    static error(content: string, duration: number = 3) {
        message.error(content, duration);
    }

    static warning(content: string, duration: number = 3) {
        message.warning(content, duration);
    }

    static info(content: React.ReactNode, duration: number = 3) {
        message.info(content, duration);
    }

    static loading(content: string = '正在加载...', duration?: number) {
        duration = duration === undefined ? 0 : duration;
        return message.loading(content, duration);
    }
}