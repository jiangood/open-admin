import React from "react";
import {DatePicker} from "antd";
import dayjs from "dayjs";

/**
 * 日期区间选择， 格式为时间戳数组
 *
 * 时间范围使用ISO 8601 时间间隔格式
 *  存储格式：开始时间/结束时间
 * 2023-01-01/2023-01-01
 *
 * 提示：后端便捷方式       q.betweenIsoDateRange("createTime", dateRange);
 */
export class FieldDateRange extends React.Component {


    str2dayjs(v) {
        console.log('调用转换')
        if (v ) {
            const arr = v.split("/")

            let v1 = arr[0];
            let v2 = arr[1];
            return [dayjs(v1), dayjs(v2)]
        }

        return null;
    }



    render() {
        let {value, onChange} = this.props

        value = this.str2dayjs(value)

        return <DatePicker.RangePicker
            value={value}
            format={"YYYY-MM-DD"}
            onChange={(value, dateString) => {
                let strValue = dateString.join('/');
                strValue = strValue === '/' ? null: strValue
                onChange(strValue)
            }}
        >
        </DatePicker.RangePicker>
    }

}
