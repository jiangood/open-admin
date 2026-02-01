
import {ViewPassword, ViewBoolean, ViewImage, ViewText} from "../../views";
import {FieldDictSelect, FieldRemoteSelect} from "../../fields";
import {Input} from "antd";


export const fieldRegistry = {
    'text':Input,

    'dict':FieldDictSelect,
    'password':Input.Password,

    select: FieldRemoteSelect
}

export const viewRegistry = {
    text: ViewText,
    password: ViewPassword,
    boolean: ViewBoolean,
    imageBase64: ViewImage,
    image:ViewImage
}




