import {Form} from "antd";
import {
    FieldRemoteSelect,
    FieldRemoteSelectMultipleInline,
    StringUtils
} from "../../../../../framework";
import React from "react";
import {useService} from "bpmn-js-properties-panel";
import {h} from "preact";
import {createRoot} from "react-dom/client";
import {useEffect, useRef} from "@bpmn-io/properties-panel/preact/hooks";

// react 组件，方便使用antd
function UserTaskForm(props) {
    const {element, modeling} = props
    let initialValues = {
        assignee: element.businessObject.assignee,
        candidateGroups: element.businessObject.candidateGroups,
        candidateUsers: StringUtils.split(element.businessObject.candidateUsers, ',')
    };
    return (<div style={{padding:16}}>
        <Form layout='vertical'
              initialValues={initialValues}
              onValuesChange={(changedValues) => {
                  modeling.updateProperties(element, changedValues);
              }}>
            <Form.Item label="办理人" name='assignee'>
                <FieldRemoteSelect url='admin/flowable/model/assigneeOptions'/>
            </Form.Item>
            <Form.Item label="候选组" name='candidateGroups'>
                <FieldRemoteSelect url='admin/flowable/model/candidateGroupsOptions'/>
            </Form.Item>
            <Form.Item label="候选人" name='candidateUsers'>
                <FieldRemoteSelectMultipleInline url='admin/flowable/model/candidateUsersOptions'/>
            </Form.Item>
        </Form>
    </div>
    )
}


// preact 组件， bpmn properties-panel 渲染组件
export function PreactUserTaskForm(props) {
    const {element, id} = props;
    const modeling = useService('modeling');
    const domRef = useRef(null);
    useEffect(() => {
        const root = createRoot(domRef.current);
        root.render(<UserTaskForm element={element} modeling={modeling} />);
    }, []);

    return h('div', {ref:domRef})
}
