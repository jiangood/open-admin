import {
    FieldDateTimePickerString,
    FieldDictRadio,
    FieldImageBase64,
    FieldInput,
    FieldPassword,
    FieldRadioBoolean, FieldSelect, FieldUploadImage
} from "../field";
import {ViewBoolean, ViewImage, ViewPassword, ViewText} from "../view";


export const fieldRegistry = {
    'text':FieldInput,

    'dict':FieldDictRadio,
    'dictRadio':FieldDictRadio,
    'password':FieldPassword,
    'datetime':FieldDateTimePickerString,
    'boolean':FieldRadioBoolean,
    imageBase64:FieldImageBase64,
    image:FieldUploadImage,

    select: FieldSelect
}

export const viewRegistry = {
    text: ViewText,
    password: ViewPassword,
    boolean: ViewBoolean,
    imageBase64: ViewImage,
    image:ViewImage
}




