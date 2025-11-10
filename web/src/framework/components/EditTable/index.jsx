import {Button, Form, Input, message} from 'antd'
import React from 'react'
import {DeleteOutlined, PlusOutlined} from "@ant-design/icons";
import './index.less'

export class EditTable extends React.Component {


    columns = []

    formRef = React.createRef()

    constructor(props) {
        super(props);

        this.columns = this.props.columns.map(col => {
            if (col.render == null) {
                col.render = (v, record) => {
                    return <Form.Item name={col.dataIndex}> <Input value={v}/></Form.Item>
                }
            }
            return col
        })


    }


    componentDidUpdate(prevProps, prevState, snapshot) {
        if(prevProps.dataSource !== this.props.dataSource){
            this.formRef.current.setFieldsValue({items: this.props.dataSource})
        }
    }

    onFinish = values => {
        this.props.onFinish(values.items)
    }


    render() {
        return <div className='edit-table'>

            <Form ref={this.formRef} onFinish={this.onFinish}>
                <Form.List name={'items'}>
                    {(fields, {add, remove}) => <>
                        <table >
                            <thead>
                            <tr>
                                {this.columns.map(col => <th>{col.title}</th>)}
                                <th></th>
                            </tr>
                            </thead>
                            <tbody>
                            {fields.map((field) => <tr
                                    key={field.key}>
                                    {this.columns.map(col => <td className="ant-table-cell" key={col.dataIndex}>
                                        <Form.Item noStyle rules={[{required: true}]} name={[field.name, col.dataIndex]}>
                                            <Input/>
                                        </Form.Item>
                                    </td>)}

                                    <td>
                                        <Button icon={<DeleteOutlined/>} size='small' shape={'circle'}
                                                onClick={() => remove(field.name)}></Button>
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                        <div className='add-btn-wrapper'>
                        <Button type='dashed' icon={<PlusOutlined/>}
                                onClick={() => add()}>增加一行
                        </Button>
                        </div>
                    </>}
                </Form.List>
                <div style={{textAlign: 'center', margin: 16}}>
                    <Button type='primary' htmlType={'submit'}>保存</Button>
                </div>
            </Form>


        </div>
    }
}
