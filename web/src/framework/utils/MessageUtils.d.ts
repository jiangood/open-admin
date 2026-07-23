import React from 'react';
import type {ModalFuncProps} from 'antd/es/modal/interface';

export class MessageUtils {
    static alert(content: any, config?: Omit<ModalFuncProps, 'content' | 'icon' | 'onOk' | 'onCancel'>): Promise<void>;
    static confirm(content: React.ReactNode, config?: Omit<ModalFuncProps, 'content' | 'icon' | 'onOk' | 'onCancel'>): Promise<boolean>;
    static success(content: string, duration?: number): void;
    static error(content: string, duration?: number): void;
    static destroy(): void;
}
