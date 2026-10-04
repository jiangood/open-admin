import React from 'react';

/**
 * 通用视图组件属性接口
 * @template T - value 的类型
 */
export interface ViewProps<T> {
  /**
   * 视图值
   */
  value?: T;
}

/**
 * 通用视图组件类类型
 * @template T - value 的类型
 */
export type ViewComponent<T> = React.ComponentClass<ViewProps<T>>;

/**
 * 通用视图组件函数类型
 * @template T - value 的类型
 */
export type ViewFunctionComponent<T> = React.FC<ViewProps<T>>;
