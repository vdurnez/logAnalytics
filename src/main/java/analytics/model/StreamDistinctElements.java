package analytics.model;

import java.util.HashSet;
import java.util.Set;

public class StreamDistinctElements {

    private Set<String> elements = new HashSet<>();

    public void addElement(String element) {
        elements.add(element);
    }

    public int cardinality() {
        return elements.size();
    }
}
