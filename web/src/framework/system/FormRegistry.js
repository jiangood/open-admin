class ReactFormRegistry {
  constructor() {
    this.forms = new Map(); // 存储 React 表单组件，key: 表单标识（如 'leave'），value: React 组件
  }

  /**
   * 注册 React 表单组件
   * @param {string} formKey - 表单唯一标识（如 'leave'、'expense'）
   * @param {React.Component} formComponent - React 表单组件
   */
  register(formKey, formComponent) {
    if (!formKey || !formComponent) {
      throw new Error("表单 Key 和组件不能为空！");
    }
    if (this.forms.has(formKey)) {
      console.warn(`⚠️ 表单 "${formKey}" 已存在，将被覆盖！`);
    }
    this.forms.set(formKey, formComponent);
    console.log(`✅ React 表单 "${formKey}" 注册成功`);
  }

  /**
   * 获取 React 表单组件
   * @param {string} formKey - 表单唯一标识
   * @returns {React.Component|null} React 表单组件，如果不存在则返回 null
   */
  get(formKey) {
    const formComponent = this.forms.get(formKey);
    if (!formComponent) {
      console.warn(`⚠️ React 表单 "${formKey}" 未注册！`);
    }
    return formComponent || null;
  }

  /**
   * 检查表单是否已注册
   * @param {string} formKey
   * @returns {boolean}
   */
  has(formKey) {
    return this.forms.has(formKey);
  }

  /**
   * 获取所有已注册的表单 Key
   * @returns {string[]}
   */
  getAllKeys() {
    return Array.from(this.forms.keys());
  }
}

// 创建全局单例（推荐）
const formRegistry = new ReactFormRegistry();

// 导出单例（项目里直接 import { reactFormRegistry } 使用）
export { formRegistry };

