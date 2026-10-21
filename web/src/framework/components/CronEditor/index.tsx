import React from 'react';
import {Button, Input, InputNumber, Modal, Segmented, Select, Space, TimePicker} from 'antd';
import {ScheduleOutlined} from '@ant-design/icons';
import dayjs from 'dayjs';
import type {Dayjs} from 'dayjs';
import {CRON_INTERVAL_UNITS, CRON_WEEKDAYS, CronUtils} from '../../utils/CronUtils';
import type {CronIntervalUnit, CronPeriod} from '../../utils/CronUtils';

export interface CronPreset {
    label: React.ReactNode;
    value: string;
}

export interface CronEditorProps {
    value?: string;
    onChange?: (value: string) => void;
    /** 常用表达式，点击后填入编辑器 */
    presets?: CronPreset[];
    placeholder?: string;
    disabled?: boolean;
}

type Mode = 'interval' | 'schedule' | 'custom';

const PERIOD_OPTIONS: { value: CronPeriod; label: string }[] = [
    {value: 'DAILY', label: '每天'},
    {value: 'WEEKLY', label: '每周'},
    {value: 'MONTHLY', label: '每月'},
    {value: 'YEARLY', label: '每年'},
];

const MONTH_OPTIONS = Array.from({length: 12}, (_, i) => ({value: i + 1, label: `${i + 1} 月`}));
const DAY_OPTIONS: { value: number | 'L'; label: string }[] = [
    ...Array.from({length: 31}, (_, i) => ({value: i + 1, label: `${i + 1} 号`})),
    {value: 'L', label: '最后一天'},
];

function FieldRow({label, children}: { label: string; children: React.ReactNode }) {
    return <div style={{display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap'}}>
        <span style={{width: 40, color: 'var(--oa-color-text-secondary, rgba(0, 0, 0, 0.65))'}}>{label}</span>
        {children}
    </div>;
}

/**
 * cron 表达式输入框 + 常用表达式助手。
 * 值为 Quartz 格式：秒 分 时 日 月 周 [年]，第一位是秒。
 */
export function CronEditor({value, onChange, presets = [], placeholder, disabled}: CronEditorProps) {
    const [open, setOpen] = React.useState(false);
    const [mode, setMode] = React.useState<Mode>('interval');
    const [every, setEvery] = React.useState(5);
    const [unit, setUnit] = React.useState<CronIntervalUnit>('minute');
    const [period, setPeriod] = React.useState<CronPeriod>('DAILY');
    const [time, setTime] = React.useState<Dayjs>(() => dayjs().hour(2).minute(0).second(0));
    const [weekday, setWeekday] = React.useState('MON');
    const [dayOfMonth, setDayOfMonth] = React.useState<number | 'L'>(1);
    const [month, setMonth] = React.useState(1);
    const [custom, setCustom] = React.useState('');

    const generated = React.useMemo(() => {
        if (mode === 'interval') {
            return CronUtils.buildInterval(every, unit);
        }
        if (mode === 'schedule') {
            return CronUtils.buildSchedule({
                period,
                hour: time.hour(),
                minute: time.minute(),
                second: time.second(),
                weekday,
                dayOfMonth,
                month,
            });
        }
        return custom.trim();
    }, [mode, every, unit, period, time, weekday, dayOfMonth, month, custom]);

    const description = CronUtils.describe(generated);

    const changeUnit = (nextUnit: CronIntervalUnit) => {
        const max = nextUnit === 'second' || nextUnit === 'minute' ? 59 : nextUnit === 'hour' ? 23 : 31;
        setUnit(nextUnit);
        setEvery(v => Math.min(Math.max(1, v), max));
    };

    const changePeriod = (nextPeriod: CronPeriod) => {
        setPeriod(nextPeriod);
        if (nextPeriod === 'YEARLY' && dayOfMonth === 'L') {
            setDayOfMonth(1);
        }
    };

    const applyExpression = (expr: string) => {
        const parsed = CronUtils.parse(expr);
        if (parsed?.mode === 'interval') {
            setMode('interval');
            setEvery(parsed.every);
            setUnit(parsed.unit);
            return;
        }
        if (parsed?.mode === 'schedule') {
            setMode('schedule');
            setPeriod(parsed.period);
            setTime(dayjs().hour(parsed.hour).minute(parsed.minute).second(parsed.second));
            setWeekday(parsed.weekday || 'MON');
            setDayOfMonth(parsed.dayOfMonth ?? 1);
            setMonth(parsed.month ?? 1);
            return;
        }
        setMode('custom');
        setCustom(expr);
    };

    const openEditor = () => {
        const expr = (value || '').trim();
        if (expr) {
            applyExpression(expr);
        } else {
            setMode('interval');
            setEvery(5);
            setUnit('minute');
            setCustom('');
        }
        setOpen(true);
    };

    const handleOk = () => {
        if (!generated) return;
        onChange?.(generated);
        setOpen(false);
    };

    const changeMode = (nextMode: Mode) => {
        if (nextMode === 'custom' && !custom.trim()) {
            setCustom(generated);
        }
        setMode(nextMode);
    };

    return <>
        <div style={{display: 'flex', gap: 8, width: '100%'}}>
            <Input
                value={value ?? ''}
                onChange={e => onChange?.(e.target.value)}
                placeholder={placeholder}
                disabled={disabled}
                allowClear
            />
            <Button
                icon={<ScheduleOutlined/>}
                title='cron 表达式助手'
                aria-label='cron 表达式助手'
                onClick={openEditor}
                disabled={disabled}
            />
        </div>

        <Modal
            open={open}
            title='cron 表达式助手'
            width={600}
            onCancel={() => setOpen(false)}
            onOk={handleOk}
            okText='使用该表达式'
            okButtonProps={{disabled: !generated}}
            destroyOnHidden
        >
            <div style={{display: 'grid', gap: 16}}>
                {presets.length > 0 && <div>
                    <div style={{marginBottom: 8, color: 'var(--oa-color-text-secondary, rgba(0, 0, 0, 0.65))'}}>
                        常用表达式
                    </div>
                    <Space size={[8, 8]} wrap>
                        {presets.map(preset => <Button
                            key={preset.value}
                            size='small'
                            title={preset.value}
                            onClick={() => applyExpression(preset.value)}
                        >
                            {preset.label}
                        </Button>)}
                    </Space>
                </div>}

                <Segmented
                    block
                    value={mode}
                    onChange={v => changeMode(v as Mode)}
                    options={[
                        {label: '按间隔', value: 'interval'},
                        {label: '按时间点', value: 'schedule'},
                        {label: '自定义', value: 'custom'},
                    ]}
                />

                {mode === 'interval' && <div style={{display: 'grid', gap: 10}}>
                    <FieldRow label='每隔'>
                        <InputNumber
                            min={1}
                            max={unit === 'second' || unit === 'minute' ? 59 : unit === 'hour' ? 23 : 31}
                            precision={0}
                            value={every}
                            onChange={v => setEvery(Number(v) || 1)}
                            style={{width: 110}}
                        />
                        <Select
                            value={unit}
                            onChange={changeUnit}
                            options={CRON_INTERVAL_UNITS}
                            style={{width: 100}}
                        />
                        <span>执行一次</span>
                    </FieldRow>
                    <div style={{fontSize: 12, color: 'var(--oa-color-text-tertiary, #999)'}}>
                        秒级间隔过短可能增加系统压力，建议按实际需要设置。
                    </div>
                </div>}

                {mode === 'schedule' && <div style={{display: 'grid', gap: 10}}>
                    <FieldRow label='周期'>
                        <Select
                            value={period}
                            onChange={changePeriod}
                            options={PERIOD_OPTIONS}
                            style={{width: 110}}
                        />
                        <span>时间</span>
                        <TimePicker
                            value={time}
                            onChange={v => {
                                if (v) setTime(v);
                            }}
                            format='HH:mm:ss'
                            allowClear={false}
                            style={{width: 130}}
                        />
                    </FieldRow>

                    {period === 'WEEKLY' && <FieldRow label='星期'>
                        <Select
                            value={weekday}
                            onChange={setWeekday}
                            options={CRON_WEEKDAYS}
                            style={{width: 110}}
                        />
                    </FieldRow>}

                    {period === 'MONTHLY' && <FieldRow label='日期'>
                        <Select<number | 'L'>
                            value={dayOfMonth}
                            onChange={setDayOfMonth}
                            options={DAY_OPTIONS}
                            style={{width: 110}}
                        />
                    </FieldRow>}

                    {period === 'YEARLY' && <FieldRow label='日期'>
                        <Select
                            value={month}
                            onChange={setMonth}
                            options={MONTH_OPTIONS}
                            style={{width: 100}}
                        />
                        <Select<number | 'L'>
                            value={dayOfMonth === 'L' ? 1 : dayOfMonth}
                            onChange={setDayOfMonth}
                            options={DAY_OPTIONS.filter(o => o.value !== 'L')}
                            style={{width: 100}}
                        />
                    </FieldRow>}

                    {period === 'MONTHLY' && typeof dayOfMonth === 'number' && dayOfMonth > 28 &&
                        <div style={{fontSize: 12, color: 'var(--oa-color-text-tertiary, #999)'}}>
                            部分月份没有 {dayOfMonth} 号，这些月份会跳过本次执行。
                        </div>}
                </div>}

                {mode === 'custom' && <div style={{display: 'grid', gap: 8}}>
                    <Input
                        value={custom}
                        onChange={e => setCustom(e.target.value)}
                        placeholder='如 0 */5 * * * ?'
                    />
                    <div style={{fontSize: 12, color: 'var(--oa-color-text-tertiary, #999)'}}>
                        格式：秒 分 时 日 月 周（可选 年）。例如 0 */5 * * * ? 表示每隔 5 分钟。
                    </div>
                </div>}

                <div style={{
                    padding: 12,
                    borderRadius: 6,
                    background: 'var(--oa-color-fill, #fafafa)',
                    border: '1px solid var(--oa-color-border, #f0f0f0)',
                }}>
                    <div style={{
                        fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Consolas, monospace',
                        fontSize: 14,
                        wordBreak: 'break-all',
                    }}>
                        {generated || '—'}
                    </div>
                    <div style={{
                        marginTop: 6,
                        fontSize: 12,
                        color: 'var(--oa-color-text-secondary, rgba(0, 0, 0, 0.65))',
                    }}>
                        {generated ? (description || '暂不支持预览该表达式，保存时由后端校验') : '请先设置执行规则'}
                    </div>
                </div>

                <div style={{fontSize: 12, color: 'var(--oa-color-text-tertiary, #999)'}}>
                    Quartz 格式：秒 分 时 日 月 周（可选 年），第一位是秒。
                </div>
            </div>
        </Modal>
    </>;
}
