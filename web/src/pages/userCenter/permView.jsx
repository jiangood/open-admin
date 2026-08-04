import React from "react";
import {Card, Empty, Tag, Tree} from "antd";
import {HttpUtils} from "../../framework";

const DATA_PERM_LABEL = {
    ALL: '所有',
    LEVEL: '本级',
    CHILDREN: '本级和子级',
    CUSTOM: '自定义',
};

export default class PermView extends React.Component {

    state = {
        data: null,
    }

    componentDidMount() {
        HttpUtils.get('admin/userCenter/perms').then(rs => {
            this.setState({data: rs})
        })
    }

    renderOrgTitle = (node) => {
        const {data} = this.state;
        const mine = data && (node.key === data.unitId || node.key === data.orgId);
        return <span>
            {node.title}
            {mine ? <Tag color='blue' style={{marginLeft: 8}}>我的机构</Tag> : null}
        </span>;
    }

    render() {
        const {data} = this.state;
        if (!data) {
            return <Empty description='加载中...'/>;
        }
        return <div>
            <div style={{marginBottom: 16}}>
                <span>数据权限：
                    <Tag color='processing'>{DATA_PERM_LABEL[data.dataPermType] || data.dataPermType || '-'}</Tag>
                </span>
            </div>
            <Card title='机构权限' size='small' style={{marginBottom: 16}} styles={{body: {maxHeight: 480, overflow: 'auto'}}}>
                {data.orgTree && data.orgTree.length
                    ? <Tree treeData={data.orgTree}
                            checkedKeys={data.orgPermIds}
                            checkable
                            selectable={false}
                            defaultExpandAll
                            showLine
                            titleRender={this.renderOrgTitle}/>
                    : <Empty description='暂无机构数据'/>}
            </Card>
            <Card title='菜单权限' size='small' styles={{body: {maxHeight: 480, overflow: 'auto'}}}>
                {data.menuTree && data.menuTree.length
                    ? <Tree treeData={data.menuTree}
                            checkedKeys={data.ownedPerms}
                            checkable
                            selectable={false}
                            defaultExpandAll
                            showLine/>
                    : <Empty description='暂无权限数据'/>}
            </Card>
        </div>;
    }
}
