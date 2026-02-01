import React from "react";
import { ViewProps } from "../types";

interface ViewTextProps extends ViewProps<string> {
    /**
     * 是否启用文本省略
     */
    ellipsis?: boolean;
    /**
     * 文本省略的最大长度
     */
    maxLength?: number;
}

export class ViewText extends React.Component<ViewTextProps, any> {
}