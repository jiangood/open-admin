package io.github.jiangood.as.common.tools.tree.drop;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DropResult {

    String parentKey;


    List<String> sortedKeys = new ArrayList<>();

}
