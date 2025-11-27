import React from "react";
import {Button, Card, Empty, Form, Input, message, Modal, Space, Splitter} from "antd";

import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
import {getBusinessObject} from "bpmn-js/lib/util/ModelUtil";
import BpmnModeler from 'bpmn-js/lib/Modeler'

import './index.css'
import customTranslate from "./customTranslate/customTranslate";
import ConditionForm from "../../../components/flow/design/form/ConditionForm";
import palette from "./palette";
import contextPad from "./contextPad";
import {CloudUploadOutlined, SaveOutlined} from "@ant-design/icons";
import {HttpUtils, MessageUtils, PageUtils} from "../../../framework";
import 'bpmn-js/dist/assets/bpmn-js.css';
import '@bpmn-io/properties-panel/assets/properties-panel.css';
import {BpmnPropertiesPanelModule, BpmnPropertiesProviderModule} from 'bpmn-js-properties-panel';

import magicPropertiesProviderModule from './provider';
import flowableModdleDescriptor from './descriptors/flowable';

export default class extends React.Component {


    state = {
        id: null,
        model: null,
        conditionVariable: null,

        elementType: null,
        elementName: '',

        showForm: false, // 表单切换过渡使用
    }
    curBo = null
    curNode = null

    bpmRef = React.createRef()

    preXmlRef = React.createRef()

    componentDidMount() {
        let params = PageUtils.currentParams()
        this.state.id = params.id
        this.bpmnModeler = new BpmnModeler({
            propertiesPanel: {
                parent: '#js-properties-panel',

            },
            additionalModules: [
                // 汉化翻译
                {
                    translate: ['value', customTranslate]
                },
                palette,
                contextPad,

                BpmnPropertiesPanelModule,
                BpmnPropertiesProviderModule,
                magicPropertiesProviderModule
            ],
            moddleExtensions: {
                magic: flowableModdleDescriptor
            }
        });

        this.modeling = this.bpmnModeler.get('modeling'); // 建模， 包含很多方法
        this.moddle = this.bpmnModeler.get('moddle'); // 数据模型， 主要存储元数据


        HttpUtils.get('admin/flowable/model/detail', {id: this.state.id}).then(rs => {
            let {conditionVariable, model} = rs;
            this.setState({model, conditionVariable}, () => this.initBpmn(model.content))
        })


        window.bpmnModeler = this.bpmnModeler
        window.moddle = this.moddle;
        window.modeling = this.modeling;
    }

    initBpmn = xml => {
        let parentNode = this.bpmRef.current;
        this.bpmnModeler.attachTo(parentNode);
        this.bpmnModeler.importXML(xml)

        this.bpmnModeler.on('element.contextmenu', e => e.preventDefault()) // 关闭右键，影响操作
        this.bpmnModeler.on('selection.changed', this.onSelectionChanged);
        //this.bpmnModeler.on('element.changed', this.refreshForm);
    };


    showXML = () => {
        this.bpmnModeler.saveXML({format: true}).then(res => {
            MessageUtils.alert(<pre style={{overflowX:"auto", height:'64vh'}}>{res.xml}</pre>,{width:1024})
        })
    }




    onSelectionChanged = e => {
        const {newSelection} = e;
        if (newSelection.length !== 1) {
            this.setState({showForm: false})
            return null
        }
        const curNode = newSelection[0]
        const curBo = getBusinessObject(curNode)

        this.curBo = curBo;
        this.curNode = curNode;

        let elementType = curBo.$type.replace("bpmn:", "");
        let elementName = curBo.get('name');

        console.log('选择元素', elementType, curBo)

        this.setState({
            elementType,
            elementName,
        })
        // 给一个过渡期
        this.refreshForm()
    }


    handleSubmit = () => {
        let id = this.state.id;

        return new Promise((resolve, reject) => {
            const hide = message.loading('保存中...', 0)
            this.bpmnModeler.saveXML().then(res => {
                HttpUtils.post('admin/flowable/model/saveContent', {id: id, content: res.xml}).then(rs => {
                    hide()
                    message.success('服务端保存成功')
                    resolve()
                }).catch(() => {
                    hide()
                    reject()
                })
            })
        })
    }
    handleDeploy = () => {
        let id = this.state.id;

        this.bpmnModeler.saveXML().then(res => {
            HttpUtils.post('admin/flowable/model/deploy', {id: id, content: res.xml})
        })
    }


    refreshForm = () => {
        this.setState({showForm: false}, () => this.setState({showForm: true}))
    }


    render() {

        return <Card title={'流程设计  ' + this.state.model?.name}
                     extra={<Space>
                         <Button type='primary' icon={<SaveOutlined/>} onClick={this.handleSubmit}>暂存</Button>
                         <Button type='primary' danger icon={<CloudUploadOutlined/>}
                                 onClick={this.handleDeploy}>部署</Button>
                         <Button onClick={this.showXML}>XML</Button>
                         <Button
                             onClick={() => PageUtils.open('/flowable/test?id=' + this.state.id, "流程测试")}> 测试 </Button>

                     </Space>}>


            <Splitter >
                <Splitter.Panel>
                    <div ref={this.bpmRef} style={{width: '100%', height: '100%'}}></div>
                </Splitter.Panel>

                <Splitter.Panel defaultSize={300}>
                    <div id={'js-properties-panel'} style={{height:500}}></div>
                    {this.renderForm()}
                    {this.renderMultiInstanceLoopCharacteristics()}
                </Splitter.Panel>
            </Splitter>



        </Card>
    }


    renderForm = () => {
        if (!this.state.showForm) {
            return <Empty description='请选择节点'/>
        }
        const {elementType, conditionVariable} = this.state;
        const {curBo} = this

        switch (elementType) {
            case 'SequenceFlow':
                return <ConditionForm conditionVariable={conditionVariable}
                                      moddle={this.moddle}
                                      bo={curBo}
                                      node={this.curNode}
                                      modeling={this.modeling}
                />

        }
        return <></>
    };

    // 多实例,支持任务和子流程
    renderMultiInstanceLoopCharacteristics = () => {
        if (!this.state.showForm) {
            return
        }

        const {elementType, conditionVariable} = this.state;
        const {curBo} = this


        if (elementType !== 'SubProcess') {
            return
        }
        let bo = curBo.loopCharacteristics;
        if (!bo) {
            return
        }

        const initialValues = Object.assign({}, bo.$attrs)

        if (bo.loopCardinality) {
            initialValues.loopCardinality = bo.loopCardinality.body
        }

        return <Card title='多实例配置'>
            <Form layout='vertical' onValuesChange={this.onMultiInstanceLoopCharacteristicsChange}
                  initialValues={initialValues}>
                <Form.Item label='循环基数' name='loopCardinality'>
                    <Input placeholder='数字或表达式，如 ${userCount}'/>
                </Form.Item>

                <Form.Item label='集合' name='flowable:collection'>
                    <Input placeholder='如 userList'/>
                </Form.Item>
                <Form.Item label='元素变量' name='flowable:elementVariable'>
                    <Input placeholder='如 user'/>
                </Form.Item>

            </Form>
        </Card>


    };

    onMultiInstanceLoopCharacteristicsChange = (changedValue, values) => {
        let bo = this.curBo.loopCharacteristics;
        if (changedValue.loopCardinality != null) {
            const expression = this.moddle.create('bpmn:FormalExpression', {body: changedValue.loopCardinality});
            bo.loopCardinality = expression;
            return
        }

        for (let key in changedValue) {
            bo.set(key, changedValue[key])
        }
    }

}
