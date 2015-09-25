package analytics.model;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// @RunWith(SpringJUnit4ClassRunner.class)
public class StreamTests {

    @Test
    public void testDistinctElements() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("test_500.tsv")));

        StreamDistinctElements streamDistinctElements = new StreamDistinctElements();

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            streamDistinctElements.addElement(line.split("\\t")[1]);
        }

        Assert.assertEquals(486, streamDistinctElements.cardinality());

    }

    @Test
    public void testTopElements() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("test_500.tsv")));

        StreamTopElements<String> stringStreamTopElements = new StreamTopElements<String>(500);

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringStreamTopElements.addElement(line.split("\\t")[1]);

            checkConsistency(stringStreamTopElements.getElementsOrdered());
        }

        List<StreamTopElements<String>.Count> topElements = stringStreamTopElements.topElementsWithCount(5);
        Assert.assertEquals(3, topElements.get(0).count);
    }

    private <T> void checkConsistency(StreamLinkedList<StreamTopElements<String>.Count> elementsOrdered) {
        Map<T, StreamLinkedList.Node> prevMap = new HashMap<>();
        Map<T, StreamLinkedList.Node> nextMap = new HashMap<>();

        if (elementsOrdered.getSize() == 0)
            return;

        StreamLinkedList.Node<StreamTopElements<String>.Count> currentNode = elementsOrdered.getFirstNode();
        StreamLinkedList.Node<StreamTopElements<String>.Count> nextNode;

        while ((nextNode = currentNode.getNext()) != null) {

            StreamLinkedList.Node nexts = nextMap.get(nextNode.item.element);
            Assert.assertNull("2 nodes have same 'next'", nexts);
            nextMap.put((T) nextNode.item.element, currentNode);

            if (currentNode.getPrev() != null) {
                StreamLinkedList.Node prevs = prevMap.get(currentNode.getPrev().item.element);
                Assert.assertNull("2 nodes have same 'back'", prevs);
                prevMap.put((T) currentNode.getPrev().item.element, currentNode);
            }

            currentNode = nextNode;
        }
    }

}
