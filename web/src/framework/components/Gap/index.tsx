import React from "react";

const SizeMap = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 40,
};

type GapProps = {
  size?: keyof typeof SizeMap;
  direction?: "horizontal" | "vertical";
} & React.HTMLAttributes<HTMLDivElement>;

/**
 * 上下间隔
 */
export const Gap: React.FC<GapProps> = ({
  size = "md",
  direction = "vertical",
  ...rest
}) => {
  if (size && !Object.hasOwn(SizeMap, size)) {
    throw new Error(
      `Gap: size 属性必须为 [${Object.keys(SizeMap).join(", ")}] 之一，当前传入：${size}`
    );
  }

  const sizePx = SizeMap[size];
  const isHorizontal = direction === "horizontal";

  const style: React.CSSProperties = {
    display: isHorizontal ? "inline-block" : "block",
    width: isHorizontal ? sizePx : 0,
    height: isHorizontal ? 0 : sizePx,
    ...rest.style,
  };

  return <div {...rest} style={style} />;
};
