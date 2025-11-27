import {isTextFieldEntryEdited, SelectEntry} from '@bpmn-io/properties-panel';
import { useService } from 'bpmn-js-properties-panel';
import { useEffect, useState } from '@bpmn-io/properties-panel/preact/hooks';
import {HttpUtils} from "../../../../../framework";
export  function ConditionProps () {

    return [
        {
            id: 'form',
            component: Component,
            isEdited: isTextFieldEntryEdited,
        }

    ];
}

function Component(props) {
    const { element, id } = props;

    const modeling = useService('modeling');
    const debounce = useService('debounceInput');
    const canvas = useService('canvas');
    const rootElement = canvas.getRootElement();
    const processId = rootElement.id;
    const getValue = (element) => {
        return element.businessObject.formKey || '';
    };

    const setValue = value => {
        return modeling.updateProperties(element, {
            formKey: value
        });
    };

    const [ options, setOptions ] = useState([]);

    useEffect(async () => {
        const rs = await HttpUtils.get('admin/flowable/model/formOptions',{code:processId})
        setOptions(rs)
    }, [ setOptions ]);

    return SelectEntry({
        element,
        id: id,
        label: '选择表单',
        getValue,
        setValue,
        debounce,

        getOptions: () => {
            return [{ value: '', label: '<留空>'},...options]
        }
    })

}
