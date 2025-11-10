import React from 'react';
import {Carousel, Empty} from 'antd';
import {SysUtil} from "../../system";


export class ViewFile extends React.Component {


    render() {
        let fileId = this.props.value

        if (!fileId) {
            return <Empty/>;
        }

        let arr = fileId.split(',');

        let urlList = arr.map(id => SysUtil.wrapServerUrl('sysFile/preview/' + id));
        let height = this.props.height;

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
            return <div style={{height}}>
                <iframe
                    key={url}
                    src={url}
                    width='99%'
                    frameBorder={0}
                    style={{height}}
                />
            </div>
        });

        return <div style={{height}}>
            <Carousel dotPosition={"top"}>
                {iframeList}
            </Carousel>
        </div>
    }
}
