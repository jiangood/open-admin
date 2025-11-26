import { html } from 'htm/preact';

import { TextFieldEntry, isTextFieldEntryEdited } from '@bpmn-io/properties-panel';
import { useService } from 'bpmn-js-properties-panel';

export default function(element) {

  return [
    {
      id: 'delegateExpression',
      element,
      component: ServiceTaskComponent,
      isEdited: isTextFieldEntryEdited,
    }

  ];
}

function ServiceTaskComponent(props) {
  const { element, id } = props;

  const modeling = useService('modeling');
  const debounce = useService('debounceInput');

  const getValue = () => {
    return element.businessObject.delegateExpression || '';
  };

  const setValue = value => {
    return modeling.updateProperties(element, {
      delegateExpression: value
    });
  };

  return html`<${TextFieldEntry}
    id=${ id }
    element=${ element }
    label='服务类'
    description='实现了JavaDelegate的Bean名称表达式， 如 \${demoDelegate}'
    getValue=${ getValue }
    setValue=${ setValue }
    debounce=${ debounce }
  />`;
}
