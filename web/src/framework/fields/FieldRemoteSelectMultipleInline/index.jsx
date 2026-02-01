import React from 'react';
import {Select, Spin, message} from 'antd';
import {debounce} from 'lodash';
import {HttpUtils, StringUtils} from "../../utils";

/**
 *  多选，但是值是字符串，逗号破解的
 */
export class FieldRemoteSelectMultipleInline extends React.Component {
    constructor(props) {
        super(props);

        this.fetchIdRef = 0;
        this.state = {
            options: [],
            loading: false,
        };

        this.loadDataDebounce = debounce(this.loadData, 800);
    }

    static defaultProps = {
        placeholder: '请搜索选择'
    };

    componentDidMount() {
        this.loadData('');
    }

    componentWillUnmount() {
        this.loadDataDebounce.cancel();
    }

    loadData = async (searchText) => {
        const {url, value} = this.props;
        const fetchId = ++this.fetchIdRef;

        this.setState({loading: true});

        try {
            const data = await HttpUtils.get(url, {searchText, selected: value});

            if (fetchId === this.fetchIdRef) {
                this.setState({options: data || []});
            }
        } catch (error) {
            console.error('远程搜索失败:', error);
            message.error('搜索失败，请重试');
            this.setState({options: []});
        } finally {
            if (fetchId === this.fetchIdRef) {
                this.setState({loading: false});
            }
        }
    };

    handleSearch = (value) => {
        if (value.trim() === '') {
            this.setState({options: []});
            return;
        }
        this.loadDataDebounce(value.trim());
    };

    render() {
        const {options, loading} = this.state;
        const {value, onChange, url, ...selectProps} = this.props;
        return (
            <Select
                showSearch={
                    {
                        filterOption: false,
                        onSearch: this.handleSearch,
                    }
                }
                value={StringUtils.split(value, ',')}
                onChange={arr => onChange && onChange(StringUtils.join(arr, ','))}
                options={options}
                notFoundContent={loading ? <Spin size="small"/> : '数据为空'}
                style={{width: '100%', minWidth: 200}}
                allowClear
                mode='multiple'
                {...selectProps}
            >
            </Select>
        );
    }
}