import React from 'react';
import {Carousel, Empty} from 'antd';
import {UrlUtils} from '../../utils';


export class ViewFile extends React.Component {


    render() {
        const value = this.props.value

        if (!value) {
            return <Empty/>;
        }

        const arr = value.split(',');

        const urlList = arr.map(objectName => UrlUtils.contextPath('/file/' + objectName));
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
