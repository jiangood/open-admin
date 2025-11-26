import {isTextFieldEntryEdited, SelectEntry} from '@bpmn-io/properties-panel';
import { useService } from 'bpmn-js-properties-panel';
import { useEffect, useState } from '@bpmn-io/properties-panel/preact/hooks';
import {HttpUtils} from "../../../../../framework";
export  function UserProps () {

    return [
        {
            id: 'assignee',
            component: AssigneeComponent,
            isEdited: isTextFieldEntryEdited,
        },
        {
            id: 'candidateGroups',
            component: CandidateGroupsComponent,
            isEdited: isTextFieldEntryEdited,
        },
        {
            id: 'candidateUsers',
            component: CandidateUsersComponent,
            isEdited: isTextFieldEntryEdited,
        },

    ];
}

function AssigneeComponent(props) {
    const { element, id } = props;

    const modeling = useService('modeling');
    const debounce = useService('debounceInput');
    const getValue = (element) => {
        return element.businessObject.assignee || '';
    };

    const setValue = value => {
        return modeling.updateProperties(element, {
            assignee: value
        });
    };

    const [ options, setOptions ] = useState([]);

    useEffect(async () => {
        const rs = await HttpUtils.get('admin/flowable/model/assigneeOptions')
        setOptions(rs)
    }, [ setOptions ]);

    return SelectEntry({
        element,
        id: id,
        label: '办理人',
        getValue,
        setValue,
        debounce,

        getOptions: () => {
            return [{ value: '', label: '<留空>'},...options]
        }
    })

}

function CandidateGroupsComponent(props) {
    const { element, id } = props;

    const modeling = useService('modeling');
    const debounce = useService('debounceInput');
    const getValue = (element) => {
        return element.businessObject.candidateGroups || '';
    };

    const setValue = value => {
        return modeling.updateProperties(element, {
            candidateGroups: value
        });
    };

    const [ options, setOptions ] = useState([]);

    useEffect(async () => {
        const rs = await HttpUtils.get('admin/flowable/model/candidateGroupsOptions')
        setOptions(rs)
    }, [ setOptions ]);

    return SelectEntry({
        element,
        id: id,
        label: '候选组',
        getValue,
        setValue,
        debounce,

        getOptions: () => {
            return [{ value: '', label: '<留空>'},...options]
        }
    })

}
function CandidateUsersComponent(props) {
    const { element, id } = props;

    const modeling = useService('modeling');
    const debounce = useService('debounceInput');
    const getValue = (element) => {
        return element.businessObject.candidateUsers || '';
    };

    const setValue = value => {
        return modeling.updateProperties(element, {
            candidateUsers: value
        });
    };

    const [ options, setOptions ] = useState([]);

    useEffect(async () => {
        const rs = await HttpUtils.get('admin/flowable/model/candidateUsersOptions')
        setOptions(rs)
    }, [ setOptions ]);

    return SelectEntry({
        element,
        id: id,
        label: '候选人',
        getValue,
        setValue,
        debounce,
        multi: true,

        getOptions: () => {
            return [{ value: '', label: '<留空>'},...options]
        }
    })

}
