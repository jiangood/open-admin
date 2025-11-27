
import {is} from 'bpmn-js/lib/util/ModelUtil';
import {DelegateExpressionProps} from "./properties/DelegateExpressionProps";
import {FormProps} from "./properties/FormProps";
import {PreactUserTaskForm} from "./properties/UserTaskForm";
import {isTextFieldEntryEdited} from "@bpmn-io/properties-panel";

const LOW_PRIORITY = 10001;


export default function MagicPropertiesProvider(propertiesPanel) {

    this.getGroups = function (element) {
        return function (groups) {
            if(is(element, 'bpmn:ServiceTask')){
                groups.push({
                    id: 'processBean',
                    label: "处理器",
                    entries: DelegateExpressionProps(element),
                    shouldOpen:true
                })
            }

            if(is(element,'bpmn:UserTask')){
                groups.push({
                    id: 'user',
                    label: "用户",
                    entries: [
                        {
                            id: 'user',
                            component: PreactUserTaskForm,
                            isEdited: isTextFieldEntryEdited,
                        }
                    ],
                    // 默认打开（看源码发现的）
                    shouldOpen:true
                })
                groups.push({
                    id: 'form',
                    label: "表单",
                    entries: FormProps(element),
                })
            }

            return groups;
        }
    }

    propertiesPanel.registerProvider(LOW_PRIORITY, this);
}

MagicPropertiesProvider.$inject = ['propertiesPanel'];


