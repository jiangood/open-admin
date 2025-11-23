import React from 'react';

export interface FieldEditProps  {
    value ?: string;
    onChange ?: (value: string) => void;
    height ?: number;
}

export class FieldEditor extends React.Component<FieldEditProps, any> {}

