// @ts-ignore
import React from "react";

// 推荐(16+8n)px 作为间隔，n为自然数
declare type GapProps = {
    /**
     * mini 4px
     * middle 8px
     * large 16px
     *
     * 默认 middle
     */
    size?: 'small' | 'middle' | 'large';

    /** 方向：水平或垂直
     * 默认垂直：上下间隔
     *
     */
    direction?: 'horizontal' | 'vertical';
};

export class Gap extends React.Component<GapProps, any> {
}
