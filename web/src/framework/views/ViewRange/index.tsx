import React from "react";

interface ViewRangeProps {
  min?: string;
  max?: string;
}

export class ViewRange extends React.Component<ViewRangeProps> {
  render() {
    const { min, max } = this.props;
    if ((min == null || min === "") && (max == null || max === "")) {
      return null;
    }
    const showMin = min ?? '未知';
    const showMax = max ?? '未知';
    return <>{showMin} - {showMax}</>;
  }
}
