import React from "react";

interface ViewRangeProps {
  min?: string;
  max?: string;
}

export const ViewRange: React.FC<ViewRangeProps> = ({ min, max }) => {
  if ((min == null || min === "") && (max == null || max === "")) {
    return null;
  }
  const showMin = min ?? '未知';
  const showMax = max ?? '未知';
  return <>{showMin} - {showMax}</>;
};
