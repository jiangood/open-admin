package io.github.jiangood.openadmin.util.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropEvent {

    String dragKey;
    String dropKey;

    boolean dropToGap;

    int dropPosition;

    DropPositionEnum dropPositionEnum;

    @AllArgsConstructor
    public enum DropPositionEnum {
        INSIDE(0),
        TOP(-1),
        BOTTOM(1);

        private final int code;

        public static DropPositionEnum valueOf(int dropPosition) {
            for (DropPositionEnum value : DropPositionEnum.values()) {
                if (value.code == dropPosition) {
                    return value;
                }
            }
            return null;
        }
    }
}
