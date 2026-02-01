import React from "react";
import {FieldRemoteTreeProps} from "./FieldRemoteTree";

export interface FieldSysOrgTreeProps extends Omit<FieldRemoteTreeProps, 'url'> {
    type?: 'dept' | 'unit';
}

export class FieldSysOrgTree extends React.Component<FieldSysOrgTreeProps> {
}