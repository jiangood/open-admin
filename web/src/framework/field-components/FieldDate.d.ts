import React from 'react';

export interface FieldDateProps  {

 type: 'YEAR' | 'YEAR_MONTH' | 'YEAR_QUARTER'| 'YEAR_MONTH_DAY' |
     'YEAR_MONTH_DAY_HOUR_MINUTE' | 'YEAR_MONTH_DAY_HOUR_MINUTE_SECOND'|
     'HOUR_MINUTE' | 'HOUR_MINUTE_SECOND';


}

export class FieldDate extends React.Component<FieldDateProps, any> {}

