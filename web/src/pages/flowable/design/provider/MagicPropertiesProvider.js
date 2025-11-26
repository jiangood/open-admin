
import {is} from 'bpmn-js/lib/util/ModelUtil';
import {ServiceTaskProps} from "./properties/ServiceTaskProps";
import {UserTaskFormProps} from "./properties/UserTaskFormProps";

const LOW_PRIORITY = 10001;


export default function MagicPropertiesProvider(propertiesPanel) {

    this.getGroups = function (element) {
        return function (groups) {
            if(is(element, 'bpmn:ServiceTask')){
                groups.push({
                    id: 'ServiceTaskLike',
                    label: "任务处理器",
                    entries: ServiceTaskProps(element),
                })
            }

            if(is(element,'bpmn:UserTask')){
                groups.push({
                    id: 'UserTaskLike',
                    label: "表单",
                    entries: UserTaskFormProps(element),
                })
            }

            return groups;
        }
    }

    propertiesPanel.registerProvider(LOW_PRIORITY, this);
}

MagicPropertiesProvider.$inject = ['propertiesPanel'];


