import React from "react";
import { FieldProps } from '../types';

interface FieldNumberRangeProps extends FieldProps<string> {
    defaultValue?: string;
}

export class FieldNumberRange extends React.Component<FieldNumberRangeProps, any> {

}