import { is } from 'bpmn-js/lib/util/ModelUtil';
import { TextFieldEntry, isTextFieldEntryEdited } from '@bpmn-io/properties-panel';

export default function(element, bpmnFactory, translate) {
    const assigneeEntry = {
        id: 'flowable-assignee',
        element,
        // 1. 定义组件类型
        component: TextFieldEntry,
        // 2. 标签/显示名
        label: translate('Assignee (Flowable)'),
        // 3. 读取属性值 (从 flowable:assignee)
        getValue: (element) => {
            const businessObject = element.businessObject;
            return businessObject.get('flowable:assignee'); // 👈 读取 Moddle 属性
        },
        // 4. 写入属性值
        setValue: (element, value) => {
            return { 'flowable:assignee': value }; // 👈 写入 Moddle 属性
        },
        is}

    return {
        id: 'assignment',
        label: translate('Assignment'),
        entries: [ assigneeEntry ]
    };
}
