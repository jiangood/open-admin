// i18n demo https://github.com/bpmn-io/bpmn-js-examples/blob/main/i18n/src/customTranslate/customTranslate.js

import translations1 from './translations';
import translations2 from './translations-properties-panel';

export default function customTranslate(template, replacements) {
  replacements = replacements || {};

  // Translate
  template = translations1[template] || translations2[template] || template;

  // Replace
  return template.replace(/{([^}]+)}/g, function(_, key) {
    return replacements[key] || '{' + key + '}';
  });
}
