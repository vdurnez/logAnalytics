package analytics.model;

import java.util.HashMap;
import java.util.Map;

public class StreamLinkedList<T> {

    protected int size;
    private Node<T> first;
    private Node<T> last;
    private Map<T, Node<T>> mapNodes = new HashMap<>();

    public int getSize() {
        return size;
    }

    public void moveNodeAfter(Node<T> currentNode, Node<T> newPrevNode) {
        if (newPrevNode == currentNode)
            return;

        Node<T> oldNext = currentNode.getNext();
        Node<T> oldPrev = currentNode.getPrev();

        if (oldPrev != null) {
            oldPrev.setNext(oldNext);
        } else {
            this.setFirst(oldNext);
        }
        if (oldNext != null)
            oldNext.setPrev(oldPrev);

        currentNode.prev = newPrevNode;
        currentNode.next = newPrevNode.next;
        if (currentNode.next != null)
            currentNode.next.prev = currentNode;

        newPrevNode.next = currentNode;

        if (currentNode.isLast()) {
            this.setLast(currentNode);
        }
    }

    public void addFirst(T item) {
        Node<T> nodeItem = new Node<>(item);
        mapNodes.put(item, nodeItem);

        if (size == 0) {
            last = nodeItem;
        } else {
            nodeItem.next = first;
            if (first.prev != null) {
                System.out.println("PROBLEM first");
            }
            first.prev = nodeItem;
        }

        first = nodeItem;

        size++;
    }

    public void setLast(Node<T> node) {
        last = node;
        node.setNext(null);
    }

    public void setFirst(Node<T> node) {
        first = node;
        node.setPrev(null);
    }

    public Node<T> findNode(T item) {
        return mapNodes.get(item);
    }

    public Node<T> getFirstNode() {
        if (size == 0) {
            return null;
        }
        return first;
    }

    public Node<T> getLastNode() {
        if (size == 0) {
            return null;
        }
        return last;
    }


    public void removeFirst() {
        if (size == 0)
            return;

        mapNodes.remove(first.item);

        first = first.next;
        first.prev = null;

        size--;
    }

    public static class Node<T> {

        public T item;
        private Node<T> prev;
        private Node<T> next;

        public Node(T element) {
            this.item = element;
        }

        public boolean isLast() {
            return next == null;
        }

        public boolean isFirst() {
            return prev == null;
        }

        public Node<T> getPrev() {
            return prev;
        }

        public void setPrev(Node<T> prev) {
            /*
            if (prev == this)
                System.out.println("PROBLEM prev=this - " + item);
            if (next == prev)
                System.out.println("PROBLEM next=previous for " + item);

            if (prev != null && item instanceof StreamTopElements.Count) {
                int nodeVal = ((StreamTopElements.Count) item).count;
                int preVal = ((StreamTopElements.Count) prev.item).count;

                if (nodeVal < preVal) {
                    System.out.println("PROBLEM curr:" + this + "<pre:" + prev);
                }
            }
            */

            this.prev = prev;
        }

        public Node<T> getNext() {
            return next;
        }

        public void setNext(Node<T> next) {/*
            if (next == this)
                System.out.println("PROBLEM next=this - " + item);
            if (next == prev)
                System.out.println("PROBLEM next=previous for " + item);

            if (next != null && item instanceof StreamTopElements.Count) {
                int nodeVal = ((StreamTopElements.Count) item).count;
                int nextVal = ((StreamTopElements.Count) next.item).count;

                if (nodeVal > nextVal) {
                    System.out.println("PROBLEM curr:" + next + ">next:" + this);
                }

            }
            */
            this.next = next;
        }

        @Override
        public String toString() {
            String name = "in";
            if (prev == null) name = "first";
            if (next == null) name = "last";
            if (prev == null && next == null) name = "alone";
            return "Node(" + name + ", " + item + ")";
        }
    }
}
