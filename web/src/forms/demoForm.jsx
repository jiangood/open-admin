import {Form, Input, InputNumber} from "antd";

export default function (){

    return <div>
        demo表单示例
        <Form>
            <Form.Item label="事由">
                <Input/>
            </Form.Item>
            <Form.Item label="请假天数" name='days'>
                <InputNumber/>
            </Form.Item>
        </Form>
    </div>
}
