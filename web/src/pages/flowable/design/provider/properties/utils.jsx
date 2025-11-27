import {useService} from "bpmn-js-properties-panel";
import {createRoot} from "react-dom/client";
import {h} from "preact";
import React from "react";
import {useEffect, useRef} from "@bpmn-io/properties-panel/preact/hooks";

// 渲染React组件（bpmn properties-panel 只支持preact）
export function renderReact(props, ReactComponent) {
    const {element, id} = props;
    const modeling = useService('modeling');
    const domRef = useRef(null);
    useEffect(() => {
        const root = createRoot(domRef.current);
        root.render(<ReactComponent element={element} modeling={modeling}/>);
    }, []);

    return h('div', {ref: domRef})
}
