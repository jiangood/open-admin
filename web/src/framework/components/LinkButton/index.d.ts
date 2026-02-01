// @ts-ignore
import React from "react";

interface LinkButtonProps {
    /**
     *   路径
     */
    path: string;
    label: string;
    type?: 'primary' | 'default' | 'dashed' | 'link' | 'text' | 'ghost'
};

export class LinkButton extends React.Component<LinkButtonProps, any> {
}
