import InstanceInfo from "../InstanceInfo";
import {Page, PageUtils} from "../../../framework";
import React from "react";

export default class extends React.Component{

    render() {
        let params = PageUtils.currentParams();
        const {businessKey} = params
        return <Page padding>
            <InstanceInfo businessKey={businessKey} />
        </Page>
    }

}