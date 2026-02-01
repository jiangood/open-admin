import React from 'react';
import {Space} from 'antd';
import {PermUtils} from "../../utils";


/**
 * 带权限的按钮列表
 * @param maxNum: 显示子节点的个数， 超过的为收缩起来
 *
 */
export class ButtonList extends React.Component {


    render() {
        let {children} = this.props;

        // 单节点的情况
        if (!Array.isArray(children)) {
            if (this.checkPerm(children)) {
                return children;
            }
            return null
        }

        const menus = [];
        for (let child of children) {
            if (child === null || child === undefined) {
                continue;
            }
            if (this.checkPerm(child)) {
                menus.push(child);
            }
        }
        return <Space>{menus}</Space>;
    }

    checkPerm = element => {
        let props = element.props;
        return props == null || props.perm == null || PermUtils.hasPermission(props.perm);

    };
}
