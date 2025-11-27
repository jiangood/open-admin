function removeProperty(businessObject, key) {
  delete businessObject.$attrs[key];
}
export default class BpmnUtils {
  constructor({ modeler, modeling }) {
    this.modeling = modeling;
    this.modeler = modeler;
  }

  modeler = null;
  modeling = null;
  getElementById(id) {
    const elementRegistry = this.modeler.get('elementRegistry');
    return elementRegistry.get(id);
  }

  static removeProperty = removeProperty;
}
