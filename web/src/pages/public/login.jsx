import React from "react";
import {history, LOGIN_PATH} from "../../framework";

/**
 * 旧版登录地址兼容：/public/login?redirect=... → /login?redirect=...
 * 保留一个版本后删除
 */
export default class LegacyLoginRedirect extends React.Component {
    componentDidMount() {
        const search = this.props.location?.search || '';
        history.replace(LOGIN_PATH + search);
    }

    render() {
        return null;
    }
}
