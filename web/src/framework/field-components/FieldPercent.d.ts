import React from 'react';

export interface FieldPercentProps  {
    value ?: number;
    onChange ?: (number: string) => void;
}

/**
 * 数字的百分数输入框
 */
export class FieldPercent extends React.Component<FieldPercentProps, any> {}

