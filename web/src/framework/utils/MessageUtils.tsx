import {Input, message, Modal, notification} from 'antd';
import type {ModalFuncProps} from 'antd/es/modal/interface';
import type {MessageType} from 'antd/es/message/index';
import type {ArgsProps, NotificationPlacement} from 'antd/es/notification/interface';
import React, {Component} from 'react';
import {ThemeUtils} from "./system";

// --- 辅助组件：Prompt 输入组件 (Class Component 模式) ---
interface PromptContentProps {
    initialValue?: string;
    placeholder?: string;
    onChange: (value: string) => void;
}

interface PromptContentState {
    value: string;
}

class PromptContent extends Component<PromptContentProps, PromptContentState> {
    constructor(props: PromptContentProps) {
        super(props);
        // 使用 state 来管理输入框的值
        this.state = {
            value: props.initialValue || '',
        };
        this.handleChange = this.handleChange.bind(this);
    }

    handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        const newValue = e.target.value;
        this.setState({value: newValue});
        // 通知外部组件（Modal）值已更改
        this.props.onChange(newValue);
    }

    // 组件挂载后，确保外部接收到初始值
    componentDidMount() {
        this.props.onChange(this.state.value);
    }

    render() {
        const {placeholder} = this.props;
        const {value} = this.state;
        return (
            <Input
                value={value}
                onChange={this.handleChange}
                placeholder={placeholder || '请输入内容'}
                style={{marginTop: 16}}
            />
        );
    }
}

// --- MsgUtils 类 (包含静态方法的工具类) ---


const buttonStyles = {
    root: {
        backgroundColor: ThemeUtils.getColor('primary-color')
    }
}

/**
 * 消息工具类 (MsgUtils)
 * 封装了 Ant Design 的 Modal, message, 和 notification 静态方法
 */
export class MessageUtils {
    // --- Antd Modal 封装 (Alert/Confirm/Prompt) ---

    /**
     * 弹出 Alert 提示框
     */
    static alert(
        content: React.ReactNode,
        title?: React.ReactNode,
        config?: Omit<ModalFuncProps, 'content' | 'title' | 'icon' | 'onOk' | 'onCancel'>,
    ) {
        return Modal.info({
            title: title || '提示',
            content,
            okText: '确定',
            icon: null,
            okButtonProps: {
                styles: buttonStyles
            },
            ...config,

        });
    }

    /**
     * 弹出 Confirm 确认框
     */
    static confirm(
        content: React.ReactNode,
        onOk: () => void | Promise<any>,
        title: React.ReactNode = '确认操作',
        onCancel?: () => void,
        config?: Omit<ModalFuncProps, 'content' | 'title' | 'icon' | 'onOk' | 'onCancel'>,
    ) {
        return Modal.confirm({
            title,
            content,
            okText: '确定',
            cancelText: '取消',
            onOk,
            onCancel,
            okButtonProps: {
                styles: buttonStyles
            },
            ...config,
        });
    }

    /**
     * 弹出 Prompt 输入框对话框
     */
    static prompt(
        message: React.ReactNode,
        onOk: (inputValue: string) => void | Promise<any>,
        title: React.ReactNode = '请输入',
        initialValue: string = '',
        onCancel?: () => void,
        config?: Omit<ModalFuncProps, 'content' | 'title' | 'icon' | 'onOk' | 'onCancel'>,
    ) {
        let inputValue = initialValue; // 用于存储输入框的实时值

        const handleInputChange = (value: string) => {
            inputValue = value; // 更新存储的值
        };

        const content = (
            <div>
                {message}
                <PromptContent
                    initialValue={initialValue}
                    onChange={handleInputChange}
                    placeholder={config?.placeholder as string} // 传递 placeholder
                />
            </div>
        );

        const handleOk = () => {
            // 在点击确定时，将当前最新的 inputValue 传递给 onOk 回调
            return onOk(inputValue);
        };

        return Modal.confirm({
            title,
            content,
            okText: '确定',
            cancelText: '取消',
            onOk: handleOk, // 绑定包含输入值传递的函数

            onCancel,
            okButtonProps: {
                styles: buttonStyles
            },
            ...config,
        });
    }

    // --- Antd message 封装 (全局通知/Loading) ---

    /**
     * 成功消息
     */
    static success(content: React.ReactNode, duration: number = 3): MessageType {
        return message.success(content, duration);
    }

    /**
     * 错误消息
     */
    static error(content: React.ReactNode, duration: number = 3): MessageType {
        return message.error(content, duration);
    }

    /**
     * 警告消息
     */
    static warning(content: React.ReactNode, duration: number = 3): MessageType {
        return message.warning(content, duration);
    }

    /**
     * 通用消息
     */
    static info(content: React.ReactNode, duration: number = 3): MessageType {
        return message.info(content, duration);
    }

    /**
     * 弹出 Loading 提示
     */
    static loading(content: React.ReactNode = '正在加载...', duration?: number): MessageType {
        return message.loading({content, duration: duration === undefined ? 0 : duration});
    }

    /**
     * 立即关闭所有 message 提示
     */
    static hideAll() {
        message.destroy();
    }

    // --- Antd Notification 封装 (通知提醒框) ---

    /**
     * 弹出右上角通知提醒框
     */
    static notify(
        message: React.ReactNode,
        description: React.ReactNode,
        type: 'success' | 'error' | 'info' | 'warning' | 'open' | 'config' = 'open',
        placement: NotificationPlacement = 'topRight',
        config?: Omit<ArgsProps, 'message' | 'description' | 'placement'>,
    ) {
        const notifyFunc = notification[type];
        notifyFunc({
            message,
            description,
            placement,
            ...config,
        });
    }

    /**
     * 弹出成功通知
     */
    static notifySuccess(
        message: React.ReactNode,
        description: React.ReactNode,
        placement?: NotificationPlacement,
        config?: Omit<ArgsProps, 'message' | 'description' | 'placement'>,
    ) {
        MessageUtils.notify(message, description, 'success', placement, config);
    }

    /**
     * 弹出失败通知
     */
    static notifyError(
        message: React.ReactNode,
        description: React.ReactNode,
        placement?: NotificationPlacement,
        config?: Omit<ArgsProps, 'message' | 'description' | 'placement'>,
    ) {
        MessageUtils.notify(message, description, 'error', placement, config);
    }

    /**
     * 弹出警告通知
     */
    static notifyWarning(
        message: React.ReactNode,
        description: React.ReactNode,
        placement?: NotificationPlacement,
        config?: Omit<ArgsProps, 'message' | 'description' | 'placement'>,
    ) {
        MessageUtils.notify(message, description, 'warning', placement, config);
    }
}

