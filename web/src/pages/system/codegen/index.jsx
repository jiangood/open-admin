import {Button, Collapse, Drawer, Form, Input, message, Modal, Select, Space, Switch, Tag, Typography} from 'antd'
import React from 'react'
import {HttpClient, Page, Perm} from '../../../framework'

/**
 * 代码生成：动态扫描实体，按实体生成常见 CRUD 代码并写入项目源码目录。
 */
export default class CodegenPage extends React.Component {

    state = {
        entities: [],
        className: undefined,
        module: '',
        label: '',
        parentMenu: 'sys',
        overwrite: false,

        files: [],
        previewOpen: false,
        confirmOpen: false,

        result: null,
        resultOpen: false,

        loading: false,
    }

    componentDidMount() {
        this.loadEntities()
    }

    loadEntities = () => {
        HttpClient.get('admin/codegen/entity-options', null, {toastError: false}).then(rs => {
            this.setState({entities: rs.data || []})
        }).catch(e => {
            console.error('[Codegen] 加载实体列表失败:', e)
        })
    }

    handleEntityChange = (value, option) => {
        const data = option?.data || {}
        this.setState({className: value, module: data.module || '', label: data.label || ''})
    }

    buildReq = () => ({
        className: this.state.className,
        module: this.state.module,
        label: this.state.label,
        parentMenu: this.state.parentMenu,
        overwrite: this.state.overwrite,
    })

    handlePreview = () => {
        if (!this.state.className) {
            message.warning('请先选择实体')
            return
        }
        this.setState({loading: true})
        HttpClient.post('admin/codegen/preview', this.buildReq(), null, {toastError: false}).then(rs => {
            this.setState({files: rs.data || [], previewOpen: true})
        }).catch(e => {
            console.error('[Codegen] 预览失败:', e)
            message.error(e?.message || '预览失败')
        }).finally(() => this.setState({loading: false}))
    }

    handleGenerate = () => {
        if (!this.state.className) {
            message.warning('请先选择实体')
            return
        }
        this.setState({confirmOpen: true})
    }

    doGenerate = () => {
        this.setState({confirmOpen: false, loading: true})
        HttpClient.post('admin/codegen/generate', this.buildReq(), null, {toastError: false}).then(rs => {
            this.setState({result: rs.data, resultOpen: true, previewOpen: false})
        }).catch(e => {
            console.error('[Codegen] 生成失败:', e)
            message.error(e?.message || '生成失败')
        }).finally(() => this.setState({loading: false}))
    }

    copy = (text) => {
        if (navigator.clipboard) {
            navigator.clipboard.writeText(text)
                .then(() => message.success('已复制'))
                .catch(() => message.error('复制失败'))
        } else {
            message.warning('当前浏览器不支持剪贴板')
        }
    }

    selectedOption = () => this.state.entities.find(e => e.value === this.state.className)

    render() {
        const selected = this.selectedOption()
        return <Page title='代码生成' description='扫描实体，生成 Repository / Service / Controller / 前端页面 / 菜单 代码并写入项目源码目录'>
            <Form layout='vertical' style={{maxWidth: 720}}>
                <Form.Item label='实体' required>
                    <Select
                        showSearch
                        optionFilterProp='label'
                        placeholder='请选择实体'
                        options={this.state.entities}
                        value={this.state.className}
                        onChange={this.handleEntityChange}
                    />
                </Form.Item>
                {selected?.data?.frameworkEntity &&
                    <Typography.Text type='warning'>
                        该实体属于框架内置包（{selected.data.packageName}），生成的文件会写入同一包路径，请确认输出位置后再生成。
                    </Typography.Text>
                }
                <Form.Item label='模块名（请求前缀 / 前端目录）'>
                    <Input placeholder='如 customer'
                           value={this.state.module}
                           onChange={e => this.setState({module: e.target.value})}/>
                </Form.Item>
                <Form.Item label='中文名'>
                    <Input value={this.state.label}
                           onChange={e => this.setState({label: e.target.value})}/>
                </Form.Item>
                <Form.Item label='父菜单 id' tooltip='默认挂在系统管理（sys）下'>
                    <Input value={this.state.parentMenu}
                           onChange={e => this.setState({parentMenu: e.target.value})}/>
                </Form.Item>
                <Form.Item label='覆盖已存在文件'>
                    <Switch checked={this.state.overwrite}
                            onChange={checked => this.setState({overwrite: checked})}/>
                </Form.Item>
                <Space>
                    <Perm code='sys-codegen:generate'>
                        <Button type='primary' loading={this.state.loading} onClick={this.handlePreview}>预览</Button>
                    </Perm>
                    <Button onClick={this.loadEntities}>刷新实体</Button>
                </Space>
            </Form>

            <Drawer title='生成预览' width={960}
                    open={this.state.previewOpen}
                    onClose={() => this.setState({previewOpen: false})}
                    extra={
                        <Perm code='sys-codegen:generate'>
                            <Button type='primary' loading={this.state.loading} onClick={this.handleGenerate}>生成并写入</Button>
                        </Perm>
                    }>
                <Collapse
                    items={this.state.files.map((file, index) => ({
                        key: String(index),
                        label: (
                            <Space>
                                <span>{file.path}</span>
                                {file.exists
                                    ? <Tag color='orange'>已存在</Tag>
                                    : <Tag color='green'>新增</Tag>}
                            </Space>
                        ),
                        children: (
                            <>
                                <Button size='small' style={{marginBottom: 8}}
                                        onClick={() => this.copy(file.content)}>复制</Button>
                                <pre style={{
                                    maxHeight: 420,
                                    overflow: 'auto',
                                    background: '#f6f8fa',
                                    padding: 12,
                                    borderRadius: 4,
                                }}>{file.content}</pre>
                            </>
                        ),
                    }))}
                />
            </Drawer>

            <Modal title='确认生成'
                   open={this.state.confirmOpen}
                   confirmLoading={this.state.loading}
                   onOk={this.doGenerate}
                   onCancel={() => this.setState({confirmOpen: false})}>
                <p>将按以下配置生成代码到项目源码目录：</p>
                <p>模块名：{this.state.module}</p>
                <p>覆盖已存在文件：{this.state.overwrite ? '是' : '否'}</p>
            </Modal>

            <Modal title='生成结果'
                   open={this.state.resultOpen}
                   footer={null}
                   onCancel={() => this.setState({resultOpen: false})}>
                <p>已写入 {this.state.result?.written?.length || 0} 个文件，跳过 {this.state.result?.skipped?.length || 0} 个。</p>
                {this.state.result?.skipped?.length > 0 &&
                    <>
                        <Typography.Text type='warning'>以下文件已存在已跳过（可勾选“覆盖已存在文件”后重新生成）：</Typography.Text>
                        <ul>
                            {this.state.result.skipped.map(path => <li key={path}>{path}</li>)}
                        </ul>
                    </>}
                <ul>
                    {(this.state.result?.written || []).map(path => <li key={path}>{path}</li>)}
                </ul>
            </Modal>
        </Page>
    }
}
