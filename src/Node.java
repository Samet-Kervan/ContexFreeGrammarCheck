import java.util.LinkedList;

public class Node {
	private String name;
	public LinkedList<LinkedList<Node>> nextLines;
	private Node[] emptyNodes;//Nodes linked with checked array. After a node from this list is used for searching its index must be marked as true on checked array
	private boolean[] checked;
	private int count;
	private boolean terminal;
	public Node(String name) {
		this.name = name;
		this.nextLines = new LinkedList<LinkedList<Node>>();
		this.terminal = true;
	}
	public Node(String name, int emptySize) {
		//A node with additional arrays to limit its use
		this(name);
		emptyNodes = new Node[emptySize];
		checked = new boolean[emptySize];
		count = 0;
		for (int i = 0; i < checked.length; i++) {
			checked[i] = false;
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isTerminal() {
		return terminal;
	}
	public void setTerminal(boolean terminal) {
		this.terminal = terminal;
	}
	public void addLine(LinkedList<Node> line) {
		nextLines.add(line);
	}
	public void addEmptyNode(Node empty) {
		//adds the given node to the emptyNodes list
		emptyNodes[count] = empty;
		count++;
	}
	public boolean isChecked(Node node) {
		//returns the checked status of of the given node
		for (int i = 0; i < emptyNodes.length; i++) {
			if (node.getName().equals(emptyNodes[i].getName())) {
				return checked[i];
			}
		}
		return true;
	}
	public void check(Node node) {
		//Marks the given nodes index as true
		for (int i = 0; i < emptyNodes.length; i++) {
			if (node.getName().equals(emptyNodes[i].getName())) {
				checked[i] = true;
			}
		}
	}
}
