import * as React from 'react';

/**
 * 💡 类型定义同上
 */
type FormComponent = React.ComponentType<any>;

/**
 * 🛠️ FormRegistryUtils
 * 一个静态工具类，用于注册、获取和管理表单组件。
 * 调用时无需实例化，直接使用 FormRegistryUtils.methodName()。
 */
export class FormRegistryUtils {
  // 静态私有属性：存储表单组件的 Map
  private static forms: Map<string, FormComponent> = new Map<string, FormComponent>();

  /**
   * 注意：
   * 1. 私有构造函数可以防止该类被外部实例化（可选，但推荐用于纯静态类）。
   * 2. 如果不定义构造函数，TypeScript 默认允许实例化，但因为所有成员都是静态的，实例化没有意义。
   */
  private constructor() {
    // 阻止外部实例化
  }

  /**
   * 静态方法：注册表单组件
   * @param formKey - 表单唯一标识
   * @param formComponent - React 表单组件
   */
  public static register(formKey: string, formComponent: FormComponent): void {
    if (!formKey || !formComponent) {
      throw new Error("【FormRegistryUtils】表单 Key 和组件不能为空！");
    }

    // 访问静态属性需要使用 'FormRegistryUtils.'
    if (FormRegistryUtils.forms.has(formKey)) {
      console.warn(`⚠️ 【FormRegistryUtils】表单 "${formKey}" 已存在，将被覆盖！`);
    }

    FormRegistryUtils.forms.set(formKey, formComponent);
    console.log(`✅ 【FormRegistryUtils】表单 "${formKey}" 注册成功`);
  }

  /**
   * 静态方法：获取表单组件
   * @param formKey - 表单唯一标识
   * @returns React 表单组件，如果不存在则返回 null
   */
  public static get(formKey: string): FormComponent | null {
    const formComponent = FormRegistryUtils.forms.get(formKey);

    if (!formComponent) {
      console.warn(`⚠️ 【FormRegistryUtils】表单 "${formKey}" 未注册！`);
    }

    return formComponent ?? null;
  }

  /**
   * 静态方法：检查表单是否已注册
   * @param formKey - 表单唯一标识
   * @returns boolean
   */
  public static has(formKey: string): boolean {
    return FormRegistryUtils.forms.has(formKey);
  }

  /**
   * 静态方法：获取所有已注册的表单 Key
   * @returns string[] - 所有表单 Key 的数组
   */
  public static getAllKeys(): string[] {
    return Array.from(FormRegistryUtils.forms.keys());
  }
}

