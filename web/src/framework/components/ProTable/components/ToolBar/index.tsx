import {ReloadOutlined} from '@ant-design/icons';
import {Button} from 'antd';
import React from 'react';
import './index.less';

export class Toolbar extends React.Component {

    render = () => {
        const {
            onRefresh,
            toolBarRender,
            loading,
        } = this.props;

        return <div className='pro-table-toolbar'>

            <div className='pro-table-toolbar-left'>
                {toolBarRender}
            </div>

            <div className='pro-table-toolbar-option'>
                <Button type='text' title='刷新' size='small' icon={<ReloadOutlined/>} onClick={onRefresh} loading={loading}/>
            </div>
        </div>
    };
}


