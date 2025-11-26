// i18n demo https://github.com/bpmn-io/bpmn-js-examples/blob/main/i18n/src/customTranslate/customTranslate.js

import translations from './translations';


export default function customTranslate(template, replacements) {
  replacements = replacements || {};

  // Translate
  template = translations[template] || template;

  // Replace
  return template.replace(/{([^}]+)}/g, function(_, key) {
    return replacements[key] || '{' + key + '}';
  });
}
