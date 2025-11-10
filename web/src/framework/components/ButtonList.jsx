import React from 'react';
import { Dropdown, Menu, Space } from 'antd';
import { DownSquareTwoTone } from '@ant-design/icons';
import {PermUtil} from "../system";






/**
 * @param maxNum: 显示子节点的个数， 超过的为收缩起来
 *
 */
export class ButtonList extends React.Component {
  static defaultProps = {
    maxNum: 2
  }

  render() {
    let {children, maxNum} = this.props;


// 单节点
    if (children.length === undefined) {
      const node = children;

      const perm = node.props == null ? null : node.props.perm;

      if (perm == null || PermUtil.hasPermission(perm)) {
        return  node
      }
      return  null
    }

    // 多个
    const showList = [];
    const dropdownList = [];

     {
      const menus = [];
      // 权限过滤
      for (let child of children) {
        if (child === null || child === undefined) {
          continue;
        }
        // @ts-ignore

        if (child ) {
          if(  child.props == null || child.props.perm == null || PermUtil.hasPermission(child.props.perm)){
            menus.push(child);
          }

        }
      }

      // 添加分割线
      const len = menus.length;
      for (let i = 0; i < len; i++) {
        if (i + 1 <= maxNum) {
          showList.push(menus[i]);
        } else {
          dropdownList.push(menus[i]);
        }
      }
    }

    return (
      <Space>
        {showList}
        {dropdownList.length > 0 && (
          <Dropdown
            overlay={
              <Menu>
                {dropdownList.map((d, i) => {
                  // @ts-ignore
                  let disabled = d.props.disabled;
                  return (
                    <Menu.Item key={i} disabled={disabled}>
                      {d}
                    </Menu.Item>
                  );
                })}
              </Menu>
            }
          >
            <a className="ant-dropdown-link" onClick={(e) => e.preventDefault()}>
              <DownSquareTwoTone style={{fontSize: 14}}/>
            </a>
          </Dropdown>
        )}
      </Space>
    );
  }
}
