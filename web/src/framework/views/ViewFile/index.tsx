import React from 'react';
import {Carousel, Empty} from 'antd';
import {UrlUtils} from '../../utils';


export class ViewFile extends React.Component {


    render() {
        const fileId = this.props.value

        if (!fileId) {
            return <Empty/>;
        }

        const arr = fileId.split(',');

        const urlList = arr.map(id => UrlUtils.contextPath('/admin/sysFile/preview/' + id));
        const height = this.props.height;

        if(urlList.length === 1){
            const url = urlList[0]
            return    <iframe
                src={url}
                width='99%'
                frameBorder={0}
                style={{height}}
            />
        }

        // 多个文件则用走马灯
        const iframeList = urlList.map((url) => {
            return <div key={url} style={{height}}>
                <iframe
                    src={url}
                    width='99%'
                    frameBorder={0}
                    style={{height}}
                />
            </div>
        });

        return <div style={{height}}>
            <Carousel dotPlacement={"top"}>
                {iframeList}
            </Carousel>
        </div>
    }
}
