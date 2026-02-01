import React from "react";
import {FieldRemoteTreeSelectProps} from "./FieldRemoteTreeSelect";

export interface FieldSysOrgTreeSelectProps extends Omit<FieldRemoteTreeSelectProps, 'url'> {
    type?: 'dept' | 'unit';
}

export class FieldSysOrgTreeSelect extends React.Component<FieldSysOrgTreeSelectProps> {
}