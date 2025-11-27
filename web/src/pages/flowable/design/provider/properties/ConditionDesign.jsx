import {Button, Modal, Select} from "antd";
import React, {Component} from "react";
import {FieldTable, HttpUtils, ThemeUtils} from "../../../../../framework";

export class ConditionDesignButton extends Component {

    state = {
        open: false,
        varList: [],
        varOptions:[]
    }

    componentDidMount() {
        const {processId} = this.props;
        console.log('流程id', processId)

        HttpUtils.get('admin/flowable/model/varList', {code: processId}).then(rs => {

            const options =  rs.map(r=>{
                return {
                    label: r.label,
                    value: r.name
                }
            })
            this.setState({varList: rs,varOptions:options})
        })
    }

    onChange(arr) {
        console.log('arr')
    }

    render() {
        return <div style={{display: 'flex', justifyContent: 'right', padding: 8}}>
            <Button type='primary' size='small'
                    styles={{
                        root: {backgroundColor: ThemeUtils.getColor('primary-color')}
                    }}
                    onClick={() => this.setState({open: true})}
            >条件编辑器</Button>


            <Modal title='条件编辑器' open={this.state.open}
                   onCancel={() => this.setState({open: false})}>
                <FieldTable columns={
                    [
                        {
                            dataIndex: 'key', title: '变量名称',
                            render: () => {
                                return <Select options={this.state.varOptions} style={{width:200}} ></Select>
                            }
                        },
                        {dataIndex: 'op', title: '操作符'},
                        {dataIndex: 'value', title: '值'},
                    ]
                }
                            onChange={this.onChange}
                ></FieldTable>
            </Modal>

        </div>
    }

}


export class ConditionDesign extends Component {

    render() {

    }

}
