export interface FieldProps {


  /**
   * @description  form表单，read查看， 不填表示表单
   */
  mode?:   'form' | 'read'  ;

  /**
   * @description 值
   */
  value?: any;


  /**
   * @description 函数回调，当值变化时
   */
  onChange?: (value: any) => void;
}
