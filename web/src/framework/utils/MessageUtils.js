import {message, Modal} from 'antd';

export class MessageUtils {

    static alert(content, config) {
        return new Promise(resolve => {
            Modal.info({
                title: '提示',
                content,
                okText: '确定',
                onOk: () => resolve(),
                icon: null,
                ...config,
            });
        });
    }

    static confirm(content, config) {
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
        });
    }

    static success(content, duration = 3) {
        message.success(content, duration);
    }

    static error(content, duration = 3) {
        message.error(content, duration);
    }

    static destroy() {
        message.destroy();
    }
}
