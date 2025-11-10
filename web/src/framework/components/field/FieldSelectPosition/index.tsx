import {ArrayLngLat, Map, Marker} from 'react-amap';
import React from 'react';
import {FieldProps} from "../FieldProps";

interface MapProps extends FieldProps {

  /**
   * @description 经纬度 lng,lat。 默认北京116.405003,39.917742
   */
  value: string

  /**
   * @description 画布宽度
   */
  width?: number;

  /**
   * @description 画布高度
   */
  height?: number;

}




/**
 * 设置高德地图key
 * @param key
 */
export function configAMapKey(key: string) {
  localStorage.setItem("amapKey", key)
}

function getAMapKey(): string {
  return localStorage.getItem("amapKey") || ''
}

export class FieldSelectPosition extends React.Component<MapProps, any> {

  static defaultProps = {
    width: 600,
    height: 400,
    value: '116.405003,39.917742'
  }


  render() {
    let {width, height, mode, value} = this.props
    let disabled = mode == 'read'

    if (!value) {
      value = FieldSelectPosition.defaultProps.value
    }

    let [lng, lat] = value.split(',')
    let position: ArrayLngLat = [parseFloat(lng), parseFloat(lat)];

    return (
      <div style={{width, height}}>
        <Map
          zoomEnable={!disabled}
          center={position}
          dragEnable={!disabled}
          zoom={16}
          amapkey={getAMapKey()}
          events={{
            click: (e:any) => {
              if (disabled) {
                return;
              }
              const {lnglat} = e;
              const {lng, lat} = lnglat; // 经纬度
              if (this.props.onChange) {
                this.props.onChange(lng + ',' + lat);
              }
            },
          }}
        >
          {value && <Marker position={position}/>}
        </Map>
      </div>
    );
  }
}
