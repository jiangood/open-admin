import React from "react";
import {fieldRegistry, viewRegistry} from "./registry";
import {Input} from "antd";
import {ViewText} from "../view";

function getComponent(registry, type) {
    if (!type) {
        return
    }
    for (let key in registry) {
        if (key.toLowerCase() === type.toLowerCase()) {
            return registry[key]
        }
    }
}

function renderField(type, props = {}) {
    const componentClass = getComponent(fieldRegistry, type) || Input
    return React.createElement(componentClass, props)
}

function renderView(type, props = {}) {
    const componentClass = getComponent(viewRegistry, type) || ViewText

    return React.createElement(componentClass, props)
}

export const ValueType = {
    renderView,
    renderField
}



