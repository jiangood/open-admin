function removeProperty(businessObject, key) {
  delete businessObject.$attrs[key];
}

function removeAllProperty(businessObject) {
  const keys = Object.keys(businessObject.$attrs);
  keys.forEach((key) => {
    delete businessObject.$attrs[key];
  });
}

function setProperty(businessObject, key, value) {
  if (value == null || value == '') {
    removeProperty(businessObject, key);
  } else {
    businessObject.set(key, value);
  }
}

// modeling.updateModdleProperties 没有处理空值
function setProperties(businessObject, values) {
  for (let k in values) {
    let v = values[k];
    setProperty(businessObject, k, v);
  }
}

export default class BpmnUtils {
  constructor({ modeler, modeling }) {
    this.modeling = modeling;
    this.modeler = modeler;
  }

  modeler = null;
  modeling = null;

  static getFormValues(bo) {
    let attrs = bo.$attrs;

    const data = {};
    for (let k in attrs) {
      data[k.replace('flowable:', '')] = attrs[k];
    }

    return data;
  }

  static setFormValues(businessObject, formValues) {
    const data = {};
    for (let k in formValues) {
      const v = formValues[k];
      data['flowable:' + k] = v;
    }

    //清空原有属性
    removeAllProperty(businessObject);
    setProperties(businessObject, data);
  }

  getElementById(id) {
    const elementRegistry = this.modeler.get('elementRegistry');
    return elementRegistry.get(id);
  }

  static removeProperty = removeProperty;

  static setDocumentation = (modeler, element, text) => {
    const modeling = modeler.get('modeling');
    const moddle = modeler.get('moddle');

    var doc = moddle.create('bpmn:Documentation');
    doc.text = text;

    modeling.updateProperties(element, { documentation: [doc] });
  };

  static getDocumentation(bo) {
    if (bo.documentation && bo.documentation.length > 0) {
      return bo.documentation[0].text;
    }
  }
  static removeDocumentation(modeler, element, text) {
    modeling.updateProperties(element, { documentation: null });
  }
}
