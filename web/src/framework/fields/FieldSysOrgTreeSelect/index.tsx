import React from "react";
import {FieldRemoteTreeSelect} from "../FieldRemoteTreeSelect";
import {orgTreeUrl} from '../orgTree';
import type {FieldProps} from '../types';

interface FieldSysOrgTreeSelectProps extends FieldProps<string | string[]> {
    type?: string;
}

export class FieldSysOrgTreeSelect extends React.Component<FieldSysOrgTreeSelectProps> {

    static readonly defaultProps = {
        type: 'dept',
    };

    render() {
        const {type, ...rest} = this.props;
        return <FieldRemoteTreeSelect url={orgTreeUrl(type)} {...rest}/>;
    }
}
