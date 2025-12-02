import {useService} from "bpmn-js-properties-panel";
import {createRoot} from "react-dom/client";
import {h} from "preact";
import React from "react";
import {useEffect, useRef} from "@bpmn-io/properties-panel/preact/hooks";
import {ConfigProvider} from "antd";
import {ThemeUtils} from "../../../../../framework";

// 渲染React组件（bpmn properties-panel 只支持preact）
export function renderReact(props, ReactComponent, moreProps) {
    const {element, id} = props;
    const modeling = useService('modeling');
    const bpmnFactory = useService('moddle');
    const canvas = useService('canvas');

    const domRef = useRef(null);
    const rootElement = canvas.getRootElement();
    const processId = rootElement.id;

    useEffect(() => {
        const root = createRoot(domRef.current);
        root.render(<ConfigProvider theme={{  token: {
                colorPrimary: ThemeUtils.getColor("primary-color"),
                colorSuccess: ThemeUtils.getColor("success-color"),
                colorWarning: ThemeUtils.getColor("warning-color"),
                colorError: ThemeUtils.getColor("error-color"),
                borderRadius: 4,
            },}}>
            <ReactComponent element={element} modeling={modeling} bpmnFactory={bpmnFactory}
                            processId={processId} {...moreProps}/></ConfigProvider>
        );
    }, []);

    return h('div', {ref: domRef})
}
