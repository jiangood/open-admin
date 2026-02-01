import React from "react";
import { FieldProps } from '../types';

interface FieldDateRangeProps extends FieldProps<string> {
    type: 'YYYY-MM-DD' | 'DAY'|
        'YYYY-MM-DD HH:mm:ss' |
        // 年
        'YYYY' | 'YEAR' |
        // 年月
        'YYYY-MM' | 'YEAR_MONTH'|
        // 季度
        'YYYY-QQ' | 'YEAR_QUARTER'|

        'YYYY-MM-DD HH:mm' |

        //  时间
        'HH:mm' |
        'HH:mm:ss';
}

export class FieldDateRange extends React.Component<FieldDateRangeProps, any> {
}