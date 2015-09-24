package analytics.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreamTopElements<T> {

    // defines max "capacity" of mapElements and elements
    private int capacity;
    private long idx;

    // map of top elements
    private Map<T, Count> mapElements = new HashMap<>();

    // top elements, ordered by count (with next/prev methods to ease re-ordering)
    private StreamLinkedList<Count> elementsOrdered = new StreamLinkedList<Count>();

    public StreamTopElements(int capacity) {
        this.capacity = capacity;
    }

    protected StreamLinkedList<Count> getElementsOrdered() {
        return this.elementsOrdered;
    }

    public void addElement(T element) {
        this.addElement(element, 1);
    }


    public void addElement(T element, int increment) {
        idx++;

        Count countElement = mapElements.get(element);
        if (countElement != null) {
            countElement.count += increment;

            // update counter position in elements (to keep ordered counts)
            StreamLinkedList.Node<Count> nodeElement = elementsOrdered.findNode(countElement);
            StreamLinkedList.Node<Count> nextElement = nodeElement;
            while (nextElement.getNext() != null && nodeElement.item.count > nextElement.getNext().item.count) {
                nextElement = nextElement.getNext();
            }

            elementsOrdered.moveNodeAfter(nodeElement, nextElement);

        } else {
            // if capacity exceeded, remove smallest element (and most recent)
            if (mapElements.size() > capacity) {
                Count elementToRemove = elementsOrdered.getFirstNode().item;
                elementToRemove.count--;
                if (elementToRemove.count == 0) {
                    elementsOrdered.removeFirst();
                    mapElements.remove(elementToRemove.element);
                }
            } else {
                countElement = new Count(element, 1);
                mapElements.put(element, countElement);
                elementsOrdered.addFirst(countElement);
            }
        }
    }

    public List<T> topElements(int topK) {
        List<T> result = new ArrayList<>();

        for (StreamLinkedList.Node<Count> topElement = elementsOrdered.getLastNode(); topElement != null; topElement = topElement.getPrev()) {
            result.add(topElement.item.element);
            if (result.size() >= topK)
                return result;
        }

        return result;
    }


    public List<Count> topElementsWithCount(int topK) {
        List<Count> result = new ArrayList<>();

        for (StreamLinkedList.Node<Count> topElement = elementsOrdered.getLastNode(); topElement != null; topElement = topElement.getPrev()) {
            result.add(topElement.item);
            if (result.size() >= topK)
                return result;
        }
        return result;
    }


    public class Count {
        public T element;
        public int count;

        public Count(T element, int count) {
            this.element = element;
            this.count = count;
        }

        @Override
        public String toString() {
            return count + ":" + element;
        }
    }

}
