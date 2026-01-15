import {isTextFieldEntryEdited, TextAreaEntry, TextFieldEntry} from '@bpmn-io/properties-panel';
import {useService} from 'bpmn-js-properties-panel';
import {renderReact} from "./utils";
import {ConditionDesignButton} from "./ConditionDesign";

function PreactConditionDesign(props) {
    return renderReact(props, ConditionDesignButton, {getValue,setValue})
}


const getValue = (element) => {
    const condition = element.businessObject.conditionExpression;
    return condition ? condition.body : '';
};

const setValue =( value, element, modeling,moddle) => {
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
        conditionExpression = moddle.create('bpmn:FormalExpression');
        modeling.updateProperties(element, {
            conditionExpression: conditionExpression
        });
    }

    // 更新表达式主体
    modeling.updateModdleProperties(element, conditionExpression, {
        body: value
    });
};
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
    const moddle = useService('moddle');


    return TextAreaEntry({
        element,
        id: id,
        label: '条件表达式(JUEL)',
        getValue,
        setValue: value=>setValue(value,element,modeling,moddle),
        debounce,
    })

}



