import React from 'react';
import { Button, Dropdown, Popconfirm, Space } from 'antd';
import { EllipsisOutlined } from '@ant-design/icons';
import { PermUtils } from '../utils';
import { getModalApi } from '../utils/ModalApi';

export interface PermAction {
    /** 按钮文本 */
    label: React.ReactNode;
    /** 权限码，无权限时隐藏该操作（如 `sys-user:update`） */
    perm?: string;
    /** 点击回调（配合 confirm 时在确认后触发） */
    onClick?: () => void;
    /** 确认提示文案，设置后点击需二次确认 */
    confirm?: React.ReactNode;
    /** 危险操作样式（红色），常与 confirm 配合用于删除 */
    danger?: boolean;
    /** 是否禁用 */
    disabled?: boolean;
    /** 图标 */
    icon?: React.ReactNode;
    /** 按钮类型，仅非下拉模式生效 */
    type?: 'primary' | 'dashed' | 'link' | 'text' | 'default';
}

export type ButtonSize = 'small' | 'middle' | 'large';

export interface PermActionsProps {
    /** 是否折叠为「前 N 个操作 + 更多下拉」，多余操作进入 `...` 菜单 */
    more?: boolean;
    /** `more` 模式下直接展示的操作数量，默认 1；超出的折叠进 `...` 下拉 */
    visibleCount?: number;
    /** 按钮尺寸 */
    size?: ButtonSize;
    /** 传统写法：包裹带 `perm` 属性的子元素，按 `perm` 过滤。不推荐，建议改用 `actions` 数据驱动 */
    children?: React.ReactNode;
    /** 数据驱动模式：操作数组，按每个 action 的 perm 过滤 */
    actions?: PermAction[];
}

export class PermActions extends React.Component<PermActionsProps> {
    static readonly defaultProps: Partial<PermActionsProps> = {
        more: false,
        visibleCount: 1,
        size: 'middle',
    };

    render() {
        const { children, more, visibleCount = 1, size, actions } = this.props;

        if (actions) {
            return this.renderActions(actions, more, size, visibleCount);
        }

        const checkPerm = (element) => {
            const _props = element?.props;
            return !_props?.perm || PermUtils.hasPermission(_props.perm);
        };

        if (children != null) {
            console.warn(
                '[PermActions] children 写法已不推荐，请改用 actions 数据驱动模式：' +
                '<PermActions actions={[{ label, perm, onClick }]} />'
            );
        }

        const nodes = React.Children.toArray(children).filter(
            (child) => child != null && checkPerm(child)
        );

        if (nodes.length === 0) return null;
        return <Space>{nodes}</Space>;
    }

    private renderButton(action: PermAction, size: 'small' | 'middle' | 'large') {
        const button = (
            <Button
                size={size}
                type={action.type}
                icon={action.icon}
                danger={action.danger}
                disabled={action.disabled}
                onClick={action.confirm ? undefined : action.onClick}
            >
                {action.label}
            </Button>
        );
        if (action.confirm) {
            return (
                <Popconfirm title={action.confirm} onConfirm={action.onClick}>
                    {button}
                </Popconfirm>
            );
        }
        return button;
    }

    private renderActions(
        actions: PermAction[],
        more?: boolean,
        size: 'small' | 'middle' | 'large' = 'small',
        visibleCount: number = 1
    ) {
        const visible = actions.filter((a) => !a.perm || PermUtils.hasPermission(a.perm));
        if (visible.length === 0) return null;

        if (!more || visible.length <= 1) {
            const buttons = visible.map((action, index) => (
                <React.Fragment key={index}>{this.renderButton(action, size)}</React.Fragment>
            ));
            return <Space>{buttons}</Space>;
        }

        const inlineCount = Math.max(1, Math.min(visibleCount, visible.length));
        const inline = visible.slice(0, inlineCount);
        const rest = visible.slice(inlineCount);

        const buttons = inline.map((action, index) => (
            <React.Fragment key={index}>{this.renderButton(action, size)}</React.Fragment>
        ));

        if (rest.length === 0) {
            return <Space>{buttons}</Space>;
        }

        const items = rest.map((action, index) => ({
            key: `more-${index}`,
            label: action.label,
            icon: action.icon,
            danger: action.danger,
            disabled: action.disabled,
            /**
             * 下拉菜单项点击后会关闭下拉，Popconfirm 会因此失去锚点导致确认按钮点不到，
             * 所以 more 模式下 confirm 改用跟随主题的全局弹窗。
             */
            onClick: () => {
                if (action.confirm) {
                    getModalApi().confirm({
                        content: action.confirm,
                        okButtonProps: { danger: action.danger },
                        onOk: () => action.onClick?.(),
                    });
                    return;
                }
                action.onClick?.();
            },
        }));

        return (
            <Space.Compact>
                {buttons}
                <Dropdown menu={{ items }} placement="bottomRight">
                    <Button size={size} icon={<EllipsisOutlined/>}/>
                </Dropdown>
            </Space.Compact>
        );
    }
}
