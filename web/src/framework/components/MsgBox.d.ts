// 请注意：在实际项目中，你可能还需要声明 `_ref.current` 所指向的实例的类型
// 以及 `message` 对象的类型（如果它们来自外部库，如 Ant Design）
// 假设 'react' 和 'antd' 或类似的库已被正确安装和配置。

import * as React from 'react';

/**
 * 静态消息框工具类
 */
export declare class MsgBox {
    /**
     * 用于引用实际的消息框组件实例的 React Ref。
     */
    static _ref: React.RefObject<any>;

    /**
     * 显示一个提示消息 (Toast)。
     * * @param msg 要显示的消息内容。
     * @param success 消息是否为成功类型。默认为 true (成功)。
     */
    static toast(msg: string, success?: boolean): void;

    /**
     * 显示一个警告框 (Alert)，并等待用户确认。
     * * @param msg 警告消息的内容。
     * @param msg
     * @param title 警告框的标题。默认为 '提示'。
     * @param modalProps
     * @returns 一个 Promise，在警告框关闭时 resolve (通常是 true 或 void，取决于组件实现)。
     */
    static alert(msg: string, title?: string, modalProps?:any): Promise<boolean | void>;

    /**
     * 显示一个确认框 (Confirm)，并等待用户操作。
     * * @param msg 确认消息的内容。
     * @param title 确认框的标题。默认为 '确认'。
     * @returns 一个 Promise，当用户点击确认时 resolve 为 true，点击取消时 resolve 为 false。
     */
    static confirm(msg: string, title?: string): Promise<boolean>;

    /**
     * 显示一个输入框 (Prompt)，允许用户输入文本。
     * * @param msg 提示用户输入的消息内容。
     * @param defaultValue 输入框的默认值。默认为 ''。
     * @param title 输入框的标题。默认为 '输入'。
     * @returns 一个 Promise，resolve 为用户输入的值 (string)；如果组件不存在或被取消，可能 resolve 为 null 或 string。
     */
    static prompt(msg: string, defaultValue?: string, title?: string): Promise<string | null>;
}

export declare class MsgBoxComponent extends React.Component<any, any> {
}
