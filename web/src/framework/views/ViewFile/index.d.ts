// @ts-ignore
import React from "react";
import { ViewProps } from "../types";

interface ViewFileProps extends ViewProps<string> {
    height: string;
}

export class ViewFile extends React.Component<ViewFileProps, any> {
}
