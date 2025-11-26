
import {is} from 'bpmn-js/lib/util/ModelUtil';
import {createServiceTaskEntries} from "./parts/CreateServiceTaskEntries";

const LOW_PRIORITY = 10001;


export default function MagicPropertiesProvider(propertiesPanel) {

    this.getGroups = function (element) {
        console.log('element',element)
        /**
         * We return a middleware that modifies
         * the existing groups.
         */
        return function (groups) {
            if (is(element, 'bpmn:ServiceTask')) {
                groups.push({
                    id: 'magic',
                    label: "服务任务",
                    entries: createServiceTaskEntries(element),
                });
            }
            return groups;
        }
    }

    propertiesPanel.registerProvider(LOW_PRIORITY, this);
}

MagicPropertiesProvider.$inject = ['propertiesPanel'];

