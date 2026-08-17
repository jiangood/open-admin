import React from "react";
import {TreeSelect} from "antd";
import {HttpClient} from "../../utils";

interface OrgNode {
    title: string;
    key: string;
    children?: OrgNode[];
}

const countUnits = (nodes: OrgNode[] = []): number =>
    nodes.reduce((sum, n) => sum + 1 + countUnits(n.children), 0);

/**
 * 组织机构切换器：切换当前组织机构（单位），默认隐藏，由 Layouts 的 orgSwitcher 插槽按需渲染。
 */
export class OrgSwitcher extends React.Component<Record<string, never>, {tree: OrgNode[]; currentOrgId: string | null}> {
    state = {tree: [], currentOrgId: null};

    componentDidMount() {
        HttpClient.get("admin/myOrgs", null).then(data => {
            if (data.data) {
                this.setState({tree: data.data.tree || [], currentOrgId: data.data.currentOrgId || null});
            }
        });
    }

    handleChange = (orgId: string) => {
        HttpClient.post("admin/switchOrg", {orgId}, null).then(() => {
            setTimeout(() => location.reload(), 1000);
        });
    };

    render() {
        const {tree, currentOrgId} = this.state;

        if (countUnits(tree) === 0) {
            return <span style={{marginRight: 8, color: "#999"}}>暂无组织机构</span>;
        }

        if (countUnits(tree) === 1) {
            return <span style={{marginRight: 8}}>{tree[0].title}</span>;
        }

        return (
            <TreeSelect
                style={{width: 160, marginRight: 8}}
                treeData={tree}
                value={currentOrgId || undefined}
                placeholder="选择组织机构"
                onChange={this.handleChange}
                treeDefaultExpandAll
                showSearch
            />
        );
    }
}
