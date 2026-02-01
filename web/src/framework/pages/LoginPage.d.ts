import React, {ReactNode} from "react";

export interface LoginPageProps {

    /**
     * 表单渲染
     * @param origForm 原始表单
     */
    formRender?: (originalForm: ReactNode) => ReactNode


}

export class LoginPage extends React.Component<LoginPageProps, any> {
}

