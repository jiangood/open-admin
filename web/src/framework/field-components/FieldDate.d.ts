import React from 'react';

export interface FieldDateProps {

    type: 'YYYY-MM-DD' |
        'YYYY-MM-DD HH:mm:ss' |
        // 年
        'YYYY' |
        // 年月
        'YYYY-MM' |
        // 季度
        'YYYY-QQ' |

        'YYYY-MM-DD HH:mm' |

        //  时间
        'HH:mm' |
        'HH:mm:ss';


}

export class FieldDate extends React.Component<FieldDateProps, any> {
}

