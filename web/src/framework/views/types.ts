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
export interface ViewComponent<T> extends React.ComponentClass<ViewProps<T> & any> {
}

/**
 * 通用视图组件函数类型
 * @template T - value 的类型
 */
export interface ViewFunctionComponent<T> extends React.FC<ViewProps<T> & any> {
}
