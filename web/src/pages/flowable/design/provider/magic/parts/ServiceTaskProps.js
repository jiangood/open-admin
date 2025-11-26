import { html } from 'htm/preact';

import { TextFieldEntry, isTextFieldEntryEdited } from '@bpmn-io/properties-panel';
import { useService } from 'bpmn-js-properties-panel';

export default function(element) {

  return [
    {
      id: 'spell',
      element,
      component: ServiceTaskProps,
      isEdited: isTextFieldEntryEdited
    }

  ];
}

function ServiceTaskProps(props) {
  const { element, id } = props;

  const modeling = useService('modeling');
  const debounce = useService('debounceInput');

  const getValue = () => {
    return element.businessObject.spell || '';
  };

  const setValue = value => {
    return modeling.updateProperties(element, {
      spell: value
    });
  };

  return html`<${TextFieldEntry}
    id=${ id }
    element=${ element }
    description='触发时间 如2019-10-01T12:00:00Z'
    label='开始时间'
    getValue=${ getValue }
    setValue=${ setValue }
    debounce=${ debounce }
  />`;
}
