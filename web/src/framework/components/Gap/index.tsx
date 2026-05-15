import React from "react";

const SizeMap = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 40,
};

/**
 * 上下间隔
 */
export const Gap = ({
  size = "md",
  direction = "vertical",
}: {
  size?: keyof typeof SizeMap;
  direction?: "horizontal" | "vertical";
}) => {
  const sizePx = SizeMap[size] || SizeMap.md;
  const isHorizontal = direction === "horizontal";

  const style: React.CSSProperties = {
    display: isHorizontal ? "inline-block" : "block",
    width: isHorizontal ? sizePx : 0,
    height: isHorizontal ? 0 : sizePx,
  };

  return <div style={style} />;
};
