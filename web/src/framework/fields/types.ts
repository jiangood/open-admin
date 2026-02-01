import React from 'react';

/**
 * 通用字段组件属性接口
 * @template T - value 的类型
 */
export interface FieldProps<T> {
  /**
   * 字段值
   */
  value?: T;
  /**
   * 值变化回调函数
   */
  onChange?: (value: T) => void;
}
