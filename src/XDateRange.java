import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Stack;

public class XDateRange<T> {
    private enum NType { RED, BLACK }
    private class Node {
        NType type;
        T max_point;
        T start_point, end_point;
        Node left, right, parent;
        boolean doesIntersect (T start_point, T end_point) {
            if (compareTo(start_point, this.end_point) > 0 || compareTo(end_point, this.start_point) < 0) {
                return false;
            }

            return true;
        }
    }

    public class Ticket {
        public T start_point, end_point;
        public boolean isFlagged;

        public Ticket () { }

        public Ticket (T start_point, T end_point, boolean isFlagged) {
            this.start_point = start_point;
            this.end_point = end_point;
            this.isFlagged = isFlagged;
        }
    }

    int compareTo (T obj1, T obj2) {
        try {
            Method compareMethod = obj1.getClass().getMethod("compareTo", Object.class);
            return (int)compareMethod.invoke(obj1, obj2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    int getDayOfMonth (T obj) {
        try {
            Method getDayOfMonth = obj.getClass().getMethod("getDayOfMonth", null);
            return (int)getDayOfMonth.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Node root = null;
    private int size = 0;
    private T min_point;

    public XDateRange () { }

    public int size () {
        return size;
    }

    public void insert (T start_point, T end_point) {
        size++;
        System.out.println("Inserting: ["+getDayOfMonth(start_point)+"]");

        if (root == null) {
            min_point = start_point;
            root = newNode(start_point, end_point, null);
            return;
        }

        if (compareTo(start_point, min_point) < 0) { min_point = start_point; }

        Node temp = root;
        while (true) {
            if (compareTo(start_point, temp.start_point) < 0) {
                if (temp.left == null) {
                    temp.left = newNode(start_point, end_point, temp);
                    fixVoilations(temp, temp.left);
                    updateMaxPoint(temp.left);
                    return;
                }
                temp = temp.left;
            } else {
                if (temp.right == null) {
                    temp.right = newNode(start_point, end_point, temp);
                    fixVoilations(temp, temp.right);
                    updateMaxPoint(temp.right);
                    return;
                }
                temp = temp.right;
            }
        }
    }

    public void print () {
        printTree(root);
    }

    public void printNodes (Node node) {
        if (node == null) {
            return;
        }

        printNodes(node.left);
        System.out.println("["+node.start_point+" -> "+node.end_point+"]("+node.max_point+")");
        printNodes(node.right);
    }

    public int getMaxOverlappingIntervals (T start_point, T end_point) {
        ArrayList<Ticket> tickets = getTicketsList(start_point, end_point);
        int max = 0;
        int active = 0;

        int i = 0;
        int j = 0;
        int m, n;
        m = n = tickets.size();
        while(i < m && j < n){
            if(compareTo(tickets.get(i).start_point, tickets.get(j).end_point) < 0)
            {
                active++;
                max = Math.max(max, active);
                i++;
            } else {
                active--;
                j++;
            }
        }

        return max;
    }

    public void printTree (Node node) {
        if (node == null) {
            return;
        }

        System.out.println("\t\t"+printNodeValue(node.parent));
        System.out.println("\t\t  ^");
        System.out.println(printNodeValue(node.left)+" <= ["+getDayOfMonth(node.start_point)+"]("+node.type+")("+getDayOfMonth(node.max_point)+") => "+printNodeValue(node.right));
        System.out.println();

        printTree(node.left);
        printTree(node.right);
    }

    public String printNodeValue (Node node) {
        if (node == null) {
            return "null";
        }

        return "["+getDayOfMonth(node.start_point)+"]";
    }

    public void printTickets (T start_point, T end_point) {
        ArrayList<Ticket> tickets = getTicketsList(start_point, end_point);

        for (Ticket ticket : tickets) {
            System.out.print("["+ticket.start_point+" -> "+ticket.end_point+"]");
//            if (ticket.isFlagged) {
//                System.out.print("(INCOMPLETE)");
//            }
            System.out.println("");
        }
    }

    public T getMinPoint () {
        return min_point;
    }

    private ArrayList<Ticket> getTicketsList (T start_point, T end_point) {
        ArrayList<Ticket> tickets = new ArrayList<>();
        Stack<Node> stack = new Stack<>();
        Node node = root;

        while (node != null || !stack.empty()) {
            while (node != null) {
                stack.push(node);
                if (node.left != null && (compareTo(start_point, node.left.max_point) < 0))
                    node = node.left;
                else
                    node = null;
            }

            node = stack.pop();

            if (node.doesIntersect(start_point, end_point)){
                Ticket temp = new Ticket();
                temp.start_point = node.start_point;
                temp.end_point = node.end_point;
                temp.isFlagged = false;

                tickets.add(temp);
            }

            if (node.right != null &&  compareTo(start_point, node.right.max_point) < 0)
                node = node.right;
            else
                node = null;
        }

        return tickets;
    }

    // =============================================================== //

    private void updateMaxPoint (Node n) {
        Node temp = n;
        while (temp != null) {
            Node parent = temp.parent;
            if (parent == null) {
                break;
            }

            if (compareTo(parent.max_point, temp.max_point) < 0) {
                parent.max_point = temp.max_point;
            }

            temp = parent;
        }
    }

    private void fixVoilations (Node parent, Node child) {
        if (parent == null) {
            return;
        }

        Node grandParent = parent.parent;
        if (grandParent == null) {
            parent.type = NType.BLACK;
            return;
        }

        if (parent.type == NType.RED && child.type == NType.RED) {
            Node uncle = grandParent.right;
            if (grandParent.right == parent) {
                uncle = grandParent.left;
            }

            if (uncle == null || uncle.type == NType.BLACK) {
                if (grandParent.left == parent && parent.left == child) {
                    leftLeft(parent, child);
                } else if (grandParent.left == parent && parent.right == child) {
                    leftRight(parent, child);
                } else if (grandParent.right == parent && parent.right == child){
                    rightRight(parent, child);
                } else if (grandParent.right == parent && parent.left == child) {
                    rightLeft(parent, child);
                }
                fixVoilations(child.parent, child);
                return;
            }

            if (uncle.type == NType.RED) {
                fixColor(uncle.parent);
                fixVoilations(grandParent.parent, grandParent);
                return;
            }
        }
    }

    private void leftLeft (Node parent, Node child) {
        System.out.println("LeftLeft");
        Node grandParent = parent.parent;
        rightRotate(grandParent, parent);

        NType temp = grandParent.type;
        grandParent.type = parent.type;
        parent.type = temp;
    }

    private void leftRight (Node parent, Node child) {
        System.out.println("LeftRight");
        leftRotate(parent, child);

        leftLeft(child, child.parent);
    }

    private void rightRight (Node parent, Node child) {
        System.out.println("RightRight");
        Node grandParent = parent.parent;
        leftRotate(grandParent, parent);

        NType temp = grandParent.type;
        grandParent.type = parent.type;
        parent.type = temp;
    }

    private void rightLeft (Node parent, Node child) {
        System.out.println("RightLeft");
        rightRotate(parent, child);

        rightRight(child, child.parent);
    }

    private void leftRotate (Node parent, Node child) {
        System.out.println("Left Rotation");
        if (parent.parent != null) {
            Node gParent = parent.parent;
            if (gParent.left == parent) {
                gParent.left = child;
            } else {
                gParent.right = child;
            }
        }

        System.out.println("Parent: " + getDayOfMonth(parent.start_point) + ", GrandParent: " + printNodeValue(parent.parent) + ", Child: "+ getDayOfMonth(child.start_point));
        System.out.println("Parent.right: "+printNodeValue(parent.right) + ", Child.left: "+ printNodeValue(child.left));

        child.parent = parent.parent;
        parent.parent = child;

        parent.right = child.left;

        if (child.left != null) {
            child.left.parent = parent;
        }

        child.left = parent;

        if (parent.right != null) {
            if (compareTo(parent.end_point, parent.right.max_point) < 0) {
                parent.max_point = parent.right.max_point;
            }
        } else {
            parent.max_point = parent.end_point;
        }

        if (child.parent == null) {
            root = child;
        }
    }

    private void rightRotate (Node parent, Node child) {
        System.out.println("Right Rotation");
        if (parent.parent != null) {
            Node gParent = parent.parent;
            if (gParent.left == parent) {
                gParent.left = child;
            } else {
                gParent.right = child;
            }
        }

        child.parent = parent.parent;
        parent.parent = child;

        parent.left = child.right;
        if (child.right != null) {
            child.right.parent = parent;
        }
        child.right = parent;

        if (parent.left != null) {
            if (compareTo(parent.end_point, parent.left.max_point) < 0) {
                parent.max_point = parent.left.max_point;
            }
        } else {
            parent.max_point = parent.end_point;
        }

        if (child.parent == null) {
            root = child;
        }
    }

    private void fixColor (Node grandParent) {
        System.out.println("Fix Colors");
        grandParent.right.type = NType.BLACK;
        grandParent.left.type = NType.BLACK;
        if (grandParent.parent != null)
            grandParent.type = NType.RED;
    }

    private Node newNode (T start_point, T end_point, Node parent) {
        Node node = new Node();
        node.type = NType.RED;
        node.parent = parent;

        node.start_point = start_point;
        node.end_point = end_point;
        node.max_point = end_point;

        node.right = null;
        node.left = null;

        return node;
    }
}
