import React from "react";
import {Button, Card, message, Space, Splitter} from "antd";

import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
import BpmnModeler from 'bpmn-js/lib/Modeler'

import './index.css'
import customTranslate from "./customTranslate/customTranslate";
import contextPad from "./contextPad";
import {CloudUploadOutlined, SaveOutlined} from "@ant-design/icons";
import {HttpUtils, MessageUtils, PageUtils} from "../../../framework";
import 'bpmn-js/dist/assets/bpmn-js.css';
import '@bpmn-io/properties-panel/assets/properties-panel.css';
import {BpmnPropertiesPanelModule, BpmnPropertiesProviderModule} from 'bpmn-js-properties-panel';

import flowablePropertiesProviderModule from './provider';
import flowableJson from './descriptors/flowable';
import Loading from "../../../loading";

export default class extends React.Component {


    state = {
        id: null,
        model: null,
    }

    bpmRef = React.createRef()

    preXmlRef = React.createRef()

    async componentDidMount() {
        let params = PageUtils.currentParams()
        this.state.id = params.id
        const rs = await HttpUtils.get('admin/flowable/model/detail', {id: this.state.id})
        this.setState({model:rs}, this.initBpmn)
    }

    initBpmn = () => {
        let container = this.bpmRef.current;
        let xml = this.state.model.content;

        this.bpmnModeler = new BpmnModeler({
            container: container,
            propertiesPanel: {parent: '#js-properties-panel',},
            additionalModules: [
                {translate: ['value', customTranslate]},
                contextPad,
                BpmnPropertiesPanelModule,
                BpmnPropertiesProviderModule,
                flowablePropertiesProviderModule
            ],
            moddleExtensions: {
             //  flowable: flowableJson
            }
        });

        this.bpmnModeler.importXML(xml)
        this.bpmnModeler.on('element.contextmenu', e => e.preventDefault()) // 关闭右键，影响操作
    };


    showXML = () => {
        this.bpmnModeler.saveXML({format: true}).then(res => {
            MessageUtils.alert(<pre style={{overflowX: "auto", height: '64vh'}}>{res.xml}</pre>, {width: 1024})
        })
    }


    handleSave = async () => {
        let id = this.state.id;
        const hide = MessageUtils.loading('正在保存...')
        try {
            const res = await this.bpmnModeler.saveXML();
            await HttpUtils.post('admin/flowable/model/saveContent', {id, content: res.xml});
        } finally {
            hide()
        }

    }
    handleDeploy = async () => {
        let id = this.state.id;
        const hide = MessageUtils.loading('正在部署...')
        try {
            const res = await this.bpmnModeler.saveXML();
            await HttpUtils.post('admin/flowable/model/deploy', {id, content: res.xml});
        } finally {
            hide()
        }
    }


    render() {
        if (this.state.model == null) {
            return <Loading/>
        }
        return <Card title={'流程设计  ' + this.state.model?.name}
                     extra={<Space>
                         <Button type='primary' icon={<SaveOutlined/>} onClick={this.handleSave}>暂存</Button>
                         <Button type='primary' danger icon={<CloudUploadOutlined/>}
                                 onClick={this.handleDeploy}>部署</Button>
                         <Button onClick={this.showXML}>XML</Button>
                         <Button
                             onClick={() => PageUtils.open('/flowable/test?id=' + this.state.id, "流程测试")}> 测试 </Button>

                     </Space>}>


            <Splitter style={{minHeight: 'calc(100vh - 200px)'}}>
                <Splitter.Panel>
                    <div ref={this.bpmRef} style={{width: '100%', height: '100%'}}></div>
                </Splitter.Panel>

                <Splitter.Panel defaultSize={300}>
                    <div id={'js-properties-panel'}></div>
                </Splitter.Panel>
            </Splitter>


        </Card>
    }


}
