import {StrUtil} from "./str";
import {DateUtil} from "./date";



const ENCRYPT = false;

export const StorageUtil = {


    get(key, defaultValue = null) {
        let v = localStorage.getItem(key);
        if (v == null) {
            v = defaultValue;
        }else {
            const item = JSON.parse(v);
            v = item.data
        }


        return v
    },

    set(key, value) {
        if(value == null){
            localStorage.removeItem(key)
        }
        const item = {
            createTime: DateUtil.now(),
            data: value
        }
        const dataStr = JSON.stringify(item)
        localStorage.setItem(key, dataStr)
    }

}



