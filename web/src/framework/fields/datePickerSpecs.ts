export interface DatePickerSpec {
    /** antd picker 属性 */
    picker?: 'year' | 'month' | 'quarter';
    /** antd showTime 属性 */
    showTime?: boolean;
    /** antd format 属性 */
    format?: string;
    /** 使用 TimePicker 而非 DatePicker */
    time?: boolean;
}

/** 日期类型 → antd 选择器属性映射（FieldDate / FieldDateRange 共用） */
export const DATE_PICKER_SPECS: Record<string, DatePickerSpec> = {
    'YYYY': {picker: 'year'},
    'YYYY-MM': {picker: 'month'},
    'YYYY-QQ': {picker: 'quarter'},
    'YYYY-MM-DD': {},
    'YYYY-MM-DD HH:mm': {showTime: true, format: 'YYYY-MM-DD HH:mm'},
    'YYYY-MM-DD HH:mm:ss': {showTime: true},
    'HH:mm': {time: true, format: 'HH:mm'},
    'HH:mm:ss': {time: true},
}
