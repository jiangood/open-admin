import {Button, Form, Modal} from "antd";
import React, {Component} from "react";
import {FieldTable, ThemeUtils} from "../../../../../framework";

export class ConditionDesignButton extends Component {

    state = {
        open:false
    }

    onChange(arr){
        console.log('arr')
    }

    render() {
        return <div style={{display:'flex',justifyContent:'right'}}>
            <Button type='primary' size='small'
                    styles={{
                        root: {backgroundColor: ThemeUtils.getColor('primary-color')}
                    }}
                    onClick={()=>this.setState({open:true})}
            >条件设计器</Button>


            <Modal title='条件设计器' open={this.state.open} onCancel={()=>this.setState({open:false})}>
                <FieldTable columns={
                    [
                        {dataIndex:'key',title:'变量名称'},
                        {dataIndex:'op',title:'操作符'},
                        {dataIndex:'value',title:'值'},
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
