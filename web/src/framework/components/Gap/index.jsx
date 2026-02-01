import React from "react";

/**
 * 上下间隔
 */
export class Gap extends React.Component {

    static defaultProps = {
        size: 'middle',
        direction: 'vertical',
    };

    render() {
        const {n = 0, size, direction} = this.props;
        if (n) {
            console.warn('参数 n 已经不被支持')
            return
        }

        const SizeMap = {
            x: 4,
            small: 8,
            middle: 16,
            large: 24,
            xLarge: 32,
            xxLarge: 40,
        };


        let sizePx = SizeMap[size];
        const isHorizontal = direction === 'horizontal';


        // 水平间隔：有宽度，无高度
        // 垂直间隔：有高度，无宽度
        const width = isHorizontal ? sizePx : 0;
        const height = isHorizontal ? 0 : sizePx;

        // 水平间隔用 inline-block（避免换行），垂直间隔用 block
        const display = isHorizontal ? 'inline-block' : 'block';

        const style = {display, height, width}

        return <div style={style}></div>
    }
}
