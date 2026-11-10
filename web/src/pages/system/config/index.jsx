import React from 'react'
import {
    Button,
    Card,
    Col,
    Form,
    Input,
    InputNumber,
    Popconfirm,
    Row,
    Select,
    Space,
    Switch,
    Tag,
    Tooltip,
    Typography,
    message
} from 'antd'
import {QuestionCircleOutlined, UndoOutlined} from '@ant-design/icons'
import {EventBus, GlobalData, HttpClient, Page, Perm} from '../../../framework'

/** 下拉值统一转字符串；清空时 antd 给 undefined，按空串处理 */
const selectText = (value) => (value === null || value === undefined ? '' : String(value))

/** 只读参数轮询间隔：启动时间里的「已运行」与连接池用量会随运行变化 */
const SYSTEM_POLL_INTERVAL = 15000

/** 旧后端或意外值一律归入启动期配置 */
const groupOf = (item) => (item.group === 'runtime' ? 'runtime' : 'startup')

/**
 * 系统参数页。
 *
 * 配置项由后端 ConfigProvider 声明（分组 / 类型 / 默认值 / 范围 / 说明），
 * 本页面只负责按元数据渲染、收集修改项并提交差异；右侧只读卡片展示
 * 来自 application.yml / 代码默认值的启动期参数与运行时状态。
 */
export default class SysConfigPage extends React.Component {

    state = {
        groups: [],
        systemParams: [],
        loading: true,
        saving: false,
        dirty: false,
    }

    formRef = React.createRef()

    systemTimer = null

    componentDidMount() {
        this.load()
        this.startSystemPolling()
    }

    componentWillUnmount() {
        this.stopSystemPolling()
    }

    /** 多 Tab 下页面保持 mounted，切回时重新拉取；有未保存修改时不覆盖用户输入 */
    onShow = () => {
        if (this.state.dirty || this.state.loading) {
            return
        }
        this.load()
    }

    load = async () => {
        this.setState({loading: true})
        const [itemsResult] = await Promise.allSettled([
            HttpClient.get('admin/sys-config/items'),
            this.loadSystem(),
        ])
        if (itemsResult.status === 'fulfilled') {
            this.applyGroups(itemsResult.value.data?.groups || [])
        } else {
            this.setState({loading: false})
        }
    }

    /** 只刷新只读参数（启动时间 / 连接池），不触碰可编辑表单；轮询失败保持现状 */
    loadSystem = async () => {
        try {
            const rs = await HttpClient.get('admin/sys-config/system', null, {toastError: false})
            this.setState({systemParams: rs.data?.items || []})
        } catch {
            // 轮询失败静默，下次再试
        }
    }

    startSystemPolling = () => {
        this.stopSystemPolling()
        this.systemTimer = setInterval(() => {
            if (!document.hidden) {
                this.loadSystem()
            }
        }, SYSTEM_POLL_INTERVAL)
    }

    stopSystemPolling = () => {
        if (this.systemTimer) {
            clearInterval(this.systemTimer)
            this.systemTimer = null
        }
    }

    /**
     * 用后端返回值更新页面，不触发整页 loading。
     * syncKeys 为空时同步全部字段；否则只同步指定项，保留其他未保存的修改。
     */
    applyGroups = (groups, syncKeys = null) => {
        this.setState({groups, loading: false}, () => {
            const form = this.formRef.current
            if (!form) {
                this.setState({dirty: false})
                return
            }
            const target = syncKeys ? new Set(syncKeys) : null
            groups.forEach(group => {
                group.items.forEach(item => {
                    if (!target || target.has(item.key)) {
                        form.setFieldValue([item.key], item.secret ? undefined : item.value)
                    }
                })
            })
            this.setState({dirty: Object.keys(this.buildPatch(form.getFieldsValue())).length > 0})
        })
    }

    /** 只收集与当前生效值不同的项 */
    buildPatch = (values) => {
        const items = new Map()
        this.state.groups.forEach(group => {
            group.items.forEach(item => items.set(item.key, item))
        })

        const patch = {}
        Object.entries(values || {}).forEach(([key, value]) => {
            const item = items.get(key)
            if (!item) {
                return
            }
            // 下拉值统一按字符串比较：清空后 antd 给 undefined，存库值可能是 null
            const next = item.type === 'select' ? selectText(value) : value
            if (next === undefined) {
                return
            }
            // 敏感值留空表示不修改
            if (item.secret && (next === null || next === '')) {
                return
            }
            const before = item.type === 'select' ? selectText(item.value) : item.value
            const same = Array.isArray(next) || Array.isArray(before)
                ? JSON.stringify(next ?? []) === JSON.stringify(before ?? [])
                : next === before
            if (!same) {
                patch[key] = next
            }
        })
        return patch
    }

    onValuesChange = (_, allValues) => {
        const dirty = Object.keys(this.buildPatch(allValues)).length > 0
        if (dirty !== this.state.dirty) {
            this.setState({dirty})
        }
    }

    save = async () => {
        const form = this.formRef.current
        if (!form) {
            return
        }
        const patch = this.buildPatch(form.getFieldsValue())
        const values = Object.entries(patch).map(([key, value]) => ({key, value}))
        if (values.length === 0) {
            message.info('没有需要保存的修改')
            return
        }

        this.setState({saving: true})
        try {
            const rs = await HttpClient.post('admin/sys-config/update', {values})
            this.applyGroups(rs.data?.groups || [], Object.keys(patch))
            await this.refreshSiteInfo()
        } catch {
            // 错误提示由 HttpClient 统一处理
        } finally {
            this.setState({saving: false})
        }
    }

    resetKeys = async (keys) => {
        try {
            const rs = await HttpClient.post('admin/sys-config/reset', {keys})
            this.applyGroups(rs.data?.groups || [], keys && keys.length ? keys : null)
            await this.refreshSiteInfo()
        } catch {
            // 错误提示由 HttpClient 统一处理
        }
    }

    /** 站点标题、水印等配置经 site-info 下发，保存后刷新本地缓存并通知布局 */
    refreshSiteInfo = async () => {
        try {
            const rs = await HttpClient.get('/admin/public/site-info', null, {toastError: false})
            const next = rs.data || {}
            const changed = JSON.stringify(GlobalData.getSiteInfo()) !== JSON.stringify(next)
            GlobalData.setSiteInfo(next)
            if (changed) {
                EventBus.emit('siteInfoChange')
            }
        } catch {
            // site-info 刷新失败不影响配置保存结果
        }
    }

    controlOf = (item) => {
        switch (item.type) {
            case 'bool':
                return <Switch/>
            case 'int':
                return <InputNumber style={{width: '100%'}} precision={0} step={1}
                                    min={item.min ?? undefined} max={item.max ?? undefined}/>
            case 'float':
                return <InputNumber style={{width: '100%'}} step={0.01}
                                    min={item.min ?? undefined} max={item.max ?? undefined}/>
            case 'time':
                return <Input placeholder="HH:mm" style={{width: '100%'}}/>
            case 'list':
                return <Select mode="tags" tokenSeparators={[',']} style={{width: '100%'}}
                               placeholder="输入后回车，或粘贴逗号分隔的内容"/>
            case 'select':
                return <Select style={{width: '100%'}}
                               showSearch
                               optionFilterProp="label"
                               allowClear={item.allowEmpty}
                               placeholder={item.allowEmpty ? '不设置' : undefined}
                               options={(item.options || []).map(option => ({
                                   value: selectText(option.value),
                                   label: option.label,
                               }))}/>
            case 'text':
                return <Input.TextArea rows={3}/>
            case 'proxy':
                return <Input placeholder="http://user:pass@192.168.1.10:8080" autoComplete="off"/>
            case 'string':
                return item.secret
                    ? <Input.Password autoComplete="new-password"
                                      placeholder={item.hasValue ? '已设置，留空表示不修改' : '未设置'}/>
                    : <Input/>
            default:
                return <Input/>
        }
    }

    renderItem = (item) => {
        const hasRange = (item.min !== null && item.min !== undefined)
            || (item.max !== null && item.max !== undefined)
        const range = hasRange
            ? `范围 ${item.min ?? '-∞'} ~ ${item.max ?? '+∞'}`
            : null
        const sourceLabel = item.source === 'db'
            ? '数据库覆盖'
            : item.source === 'env' ? 'application.yml' : '代码默认'
        const defaultValue = item.defaultValue === null
            || item.defaultValue === undefined
            || item.defaultValue === ''
            ? '空'
            : JSON.stringify(item.defaultValue)

        return <Col xs={24} md={12} xl={8} key={item.key}>
            <Form.Item
                name={[item.key]}
                required={item.required}
                label={<Space size={4}>
                    <span>{item.label}</span>
                    <Tooltip title={<>
                        {item.hint && <div>{item.hint}</div>}
                        {!item.secret && <div>默认值：{defaultValue}</div>}
                        {range && <div>{range}</div>}
                        <div>当前来源：{sourceLabel}</div>
                        <div>配置键：{item.key}</div>
                    </>}>
                        <QuestionCircleOutlined style={{color: 'var(--oa-color-text-tertiary)'}}/>
                    </Tooltip>
                    {item.overridden && (
                        <Space size={2}>
                            <Tag color="blue" style={{marginInlineEnd: 0}}>已覆盖</Tag>
                            <Perm code='sys-config:update'>
                                <Popconfirm title="恢复为默认值？" onConfirm={() => this.resetKeys([item.key])}>
                                    <Button type="text" size="small" title="恢复默认"
                                            icon={<UndoOutlined/>}
                                            style={{padding: 0, width: 20, minWidth: 20, height: 20}}/>
                                </Popconfirm>
                            </Perm>
                        </Space>
                    )}
                </Space>}
            >
                {this.controlOf(item)}
            </Form.Item>
            <div style={{marginTop: -18, marginBottom: 12}}>
                <Typography.Text type="secondary" style={{fontSize: 12}}>{item.hint}</Typography.Text>
                {item.secret && (
                    <Typography.Text type="secondary" style={{fontSize: 12, marginInlineStart: 8}}>
                        {item.hasValue ? '已设置' : '未设置'}
                    </Typography.Text>
                )}
            </div>
        </Col>
    }

    /**
     * 只读参数固定在页面右侧，占约三分之一宽度，长路径 / JDBC URL 不至于挤成一列；窄屏时自然堆叠到表单下方。
     *
     * 运行时状态与启动期配置分成两张卡片，避免两种性质不同的值混在一张卡里；
     * 每项按「标签 / 配置键（右对齐、可省略）/ 值」排布，而不是 antd Descriptions：
     * 垂直 Descriptions 的标签与值同为正文级字号，技术配置键夹在中间会让人分不清哪行是值。
     * 运行时状态（启动时间 / 连接池）不是 yml 配置项，只显示标签和值。
     */
    renderSystemCards = () => {
        const {systemParams} = this.state
        if (systemParams.length === 0) {
            return null
        }
        const runtime = systemParams.filter(item => groupOf(item) === 'runtime')
        const startup = systemParams.filter(item => groupOf(item) !== 'runtime')

        return <Col xs={24} xl={8}>
            <div style={{display: 'flex', flexDirection: 'column', gap: 16}}>
                {runtime.length > 0 && (
                    <Card
                        title="运行时状态"
                        extra={<Typography.Text type="secondary" style={{fontSize: 12}}>
                            每 {SYSTEM_POLL_INTERVAL / 1000} 秒刷新
                        </Typography.Text>}
                    >
                        {runtime.map((item, index) => this.renderSystemRow(item, index))}
                    </Card>
                )}
                {startup.length > 0 && (
                    <Card title="启动期配置" extra={<Tag style={{marginInlineEnd: 0}}>需重启</Tag>}>
                        {startup.map((item, index) => this.renderSystemRow(item, index))}
                    </Card>
                )}
            </div>
            <Typography.Text type="secondary" style={{display: 'block', fontSize: 12, marginTop: 8}}>
                以上参数仅展示，不参与保存。
            </Typography.Text>
        </Col>
    }

    renderSystemRow = (item, index) => {
        // 运行时状态不是 yml 里能查到的配置项（启动时间 / 连接池用量），右侧不再挂配置键
        const showKey = groupOf(item) !== 'runtime'
        return <div key={item.key} style={{
            padding: '8px 0',
            borderTop: index === 0 ? undefined : '1px solid var(--oa-color-border)',
        }}>
            <div style={{display: 'flex', alignItems: 'baseline', gap: 8}}>
                <Typography.Text style={{fontSize: 12, color: 'var(--oa-color-text-secondary)'}}>
                    {item.label}
                </Typography.Text>
                {showKey && (
                    <Typography.Text title={item.key} style={{
                        minWidth: 0,
                        marginInlineStart: 'auto',
                        fontSize: 11,
                        color: 'var(--oa-color-text-tertiary)',
                        fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace',
                        whiteSpace: 'nowrap',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                    }}>
                        {item.key}
                    </Typography.Text>
                )}
            </div>
            <Typography.Text style={{
                display: 'block',
                marginTop: 2,
                fontSize: 13,
                overflowWrap: 'anywhere',
            }}>
                {item.value || '-'}
            </Typography.Text>
        </div>
    }

    render() {
        const {groups, loading, saving, dirty} = this.state
        const hasOverridden = groups.some(group => group.items.some(item => item.overridden))
        // 首次加载显示骨架屏；切回 Tab 等刷新时保留现有内容，避免闪白
        const initialLoading = loading && groups.length === 0

        return <Page>
            <Row gutter={[16, 16]}>
                <Col xs={24} xl={16}>
                    <Form ref={this.formRef} layout="vertical" onValuesChange={this.onValuesChange}>
                        {initialLoading && <Card loading style={{marginBottom: 16}}/>}
                        <div style={{display: 'flex', flexDirection: 'column', gap: 16}}>
                            {groups.map(group => (
                                <Card key={group.code}
                                      title={group.label}>
                                    {group.description && (
                                        <Typography.Text type="secondary"
                                                         style={{display: 'block', fontSize: 12, marginBottom: 16}}>
                                            {group.description}
                                        </Typography.Text>
                                    )}
                                    <Row gutter={16}>
                                        {group.items.map(this.renderItem)}
                                    </Row>
                                </Card>
                            ))}
                        </div>

                        {groups.length > 0 && (
                            <Space wrap style={{marginTop: 16}}>
                                <Perm code='sys-config:update'>
                                    <Button type="primary" loading={saving} disabled={!dirty} onClick={this.save}>
                                        保存修改
                                    </Button>
                                </Perm>
                                <Perm code='sys-config:update'>
                                    <Popconfirm
                                        title="恢复全部默认值？"
                                        description="所有已修改的配置项都会回到默认值。"
                                        disabled={!hasOverridden}
                                        onConfirm={() => this.resetKeys([])}
                                    >
                                        <Button danger disabled={!hasOverridden}>全部恢复默认</Button>
                                    </Popconfirm>
                                </Perm>
                                <Typography.Text type="secondary" style={{fontSize: 12}}>
                                    配置默认值来自 application.yml 或代码声明
                                </Typography.Text>
                            </Space>
                        )}
                    </Form>
                </Col>
                {this.renderSystemCards()}
            </Row>
        </Page>
    }
}
