import {isTextFieldEntryEdited, SelectEntry, TextFieldEntry} from '@bpmn-io/properties-panel';
import {useService} from 'bpmn-js-properties-panel';
import {useEffect, useState} from '@bpmn-io/properties-panel/preact/hooks';
import {HttpUtils} from "../../../../../framework";
import {renderReact} from "./utils";
import {ConditionDesignButton} from "./ConditionDesign";

function PreactConditionDesign(props) {
    return renderReact(props, ConditionDesignButton)
}
export function ConditionProps() {

    return [
        {
            id: 'expression',
            component: Component,
            isEdited: isTextFieldEntryEdited,
        },
        {
            id: 'expressionDesign',
            component: PreactConditionDesign,
            isEdited: isTextFieldEntryEdited,
        }
    ]
}

function Component(props) {
    const {element, id} = props;

    const modeling = useService('modeling');
    const debounce = useService('debounceInput');
    const getValue = (element) => {
        const condition = element.businessObject.conditionExpression;
        return condition ? condition.body : '';
    };

    const setValue = value => {
        const businessObject = element.businessObject;
        let conditionExpression = businessObject.conditionExpression;

        if (!value) {
            // 移除条件表达式
            modeling.updateProperties(element, {
                conditionExpression: undefined
            });
            return;
        }

        if (!conditionExpression) {
            // 创建一个新的 tFormalExpression 元素
            const bpmnFactory = useService('bpmnFactory');
            conditionExpression = bpmnFactory.create('bpmn:tFormalExpression');

            modeling.updateProperties(element, {
                conditionExpression: conditionExpression
            });
        }

        // 更新表达式主体
        modeling.updateModdleProperties(element, conditionExpression, {
            body: value
        });
    };


    return TextFieldEntry({
        element,
        id: id,
        label: '条件表达式',
        getValue,
        setValue,
        debounce,


    })

}



