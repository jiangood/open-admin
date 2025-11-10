import * as echarts from 'echarts';
import React from "react";


export class Echarts extends React.Component {

    chartRef = React.createRef();
    chartInstance = null;

    componentDidMount() {
        this.initChart();
    }

    componentDidUpdate(prevProps) {
        // 当选项或样式发生变化时更新图表
        if (
            prevProps.option !== this.props.option ||
            prevProps.style !== this.props.style
        ) {
            this.updateChart();
        }
    }

    componentWillUnmount() {
        // 组件卸载时销毁图表实例
        if (this.chartInstance) {
            this.chartInstance.dispose();
            this.chartInstance = null;
        }
    }

    initChart() {
        if (this.chartRef.current) {
            this.chartInstance = echarts.init(this.chartRef.current);
            this.updateChart();
        }
    }

    updateChart = () => {
        if (this.chartInstance && this.props.option) {
            this.chartInstance.setOption(this.props.option);
        }
    };


    render() {
        return <div ref={this.chartRef} style={{height: this.props.height}}></div>
    }
}
