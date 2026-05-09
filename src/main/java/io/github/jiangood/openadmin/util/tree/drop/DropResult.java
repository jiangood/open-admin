package io.github.jiangood.openadmin.util.tree.drop;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DropResult {

    String parentKey;


    List<String> sortedKeys = new ArrayList<>();

}
