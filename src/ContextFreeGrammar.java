import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;

public class ContextFreeGrammar {
	private LinkedList<Node> nodes;//Every character is in here
	private LinkedList<Node> products;//For backtracking
	private LinkedList<String> derivation;//Stores derivation process to print later
	private LinkedList<String> parseTree;//Stores parse tree to print later
	public ContextFreeGrammar(String fileName) {
		nodes = new LinkedList<Node>();
		products = new LinkedList<Node>();
		setAlphabet(fileName);
		String inputString = takeInputFromUser();
		nodeCheck(inputString);//Checks every node
		derivation = new LinkedList<String>();
		parseTree = new LinkedList<String>();
		LinkedList<String> previous = new LinkedList<String>();
		previous.add(inputString);
		if(checkAlphabet(inputString, maximumProductLength(), previous)) {
			//This string is in alphabet so it prints the parse tree and derivation
			System.out.println("The string " + inputString + " belongs to the alphabet.");
			System.out.println("Parse tree: ");
			for (int i = 0; i < parseTree.size(); i++) {
				System.out.println(parseTree.get(i));
			}
			System.out.println("It's derivation: ");
			for (int i = 0; i < derivation.size(); i++) {
				System.out.print(derivation.get(i) + " => ");
			}
			System.out.println(inputString);
		}
		else {
			System.out.println("The string " + inputString + " does not belong to the alphabet.");
		}
	}
	
	private void setAlphabet(String fileName) {
		//Reads the alphabet file and creates nodes and their next steps
		//Also creates products(after the '>' part of T>E+T|T) as nodes and saves their parent as their next step
		//Products are used for backtracking to check if a string belongs to the alphabet
		try {
			File file = new File(fileName);
			Scanner reader = new Scanner(file);
			String inputStream;
			while(reader.hasNextLine()) {
				inputStream = reader.nextLine();
				String[] firstSplit = inputStream.split(">");
				if(firstSplit.length != 2) {
					//Input file should have only one '>' character for each line 
					//If it does not then this file is not constructed correctly
					System.out.println("This is not an alphabet!");
					System.exit(1);
				}
				Node node = searchNode(firstSplit[0], nodes);
				if(node == null) {
					//Creates the node if it's not been created
					node = new Node(firstSplit[0]);
					nodes.add(node);
				}
				node.setTerminal(false);
				String[] secondSplit = firstSplit[1].split("\\|");//Splitting to expressions
				for (int i = 0; i < secondSplit.length; i++) {
					LinkedList<Node> nextValue = new LinkedList<Node>();
					String next = "";
					for (int j = 0; j < secondSplit[i].length(); j++) {
						if(secondSplit[i].charAt(j) != '#') {//If it is not an empty string
							next += secondSplit[i].charAt(j);
							Node secondaryNode = searchNode(String.valueOf(secondSplit[i].charAt(j)), nodes);
							if(secondaryNode == null) {
								secondaryNode = new Node(String.valueOf(secondSplit[i].charAt(j)));
								nodes.add(secondaryNode);
							}
							nextValue.add(secondaryNode);
						}
						else {
							nextValue.add(new Node(""));
						}
					}
					node.addLine(nextValue);
					if (!nextValue.get(0).getName().equals("")) {
						//Empty strings will not be added to the process 
						Node product = searchNode(next, products);
						LinkedList<Node> parent = new LinkedList<Node>();
						parent.add(node);
						if(product == null) {
							product = new Node(next);
							products.add(product);
						}
						product.addLine(parent);
					}
				}
			}
			reader.close();
			chomskyForm();//Removes empty string rules and changes the product tree
		} catch (FileNotFoundException e) {//Error handling. 
			//File with given name (fileName) does not exists in the programs files.
			System.out.println("Files are not complete. Try again with all files intact. "
					+ "Check if the " + fileName + " file is correct");
			System.exit(1);
		}
	}
	
	private Node searchNode(String name, LinkedList<Node> list) {
		//Searches a node with the given name in the given list.
		//Returns the node if it exists returns null otherwise
		for (int i = 0; i < list.size(); i++) {
			if(list.get(i).getName().equals(name)) {
				return list.get(i);
			}
		}
		return null;
	}
	
	private void chomskyForm() {
		//Removes empty string rule (ε rules)
		LinkedList<Node> emptyStringNodes = new LinkedList<Node>();
		for (int i = 0; i < nodes.size(); i++) {
			//Finding the nodes with the empty strings
			Node n = nodes.get(i);
			for (int j = 0; j < n.nextLines.size(); j++) {
				try {
					for (int k = 0; k < n.nextLines.get(j).size(); k++) {
						try {
							if (n.nextLines.get(j).get(k).getName().equals("")) {
								emptyStringNodes.add(n);
							}
						} catch (Exception e) {
							
						}
					}
				} catch (IndexOutOfBoundsException e) {
					
				}
			}
		}
		for (int i = 0; i < products.size(); i++) {
			//Changes every nodes in the products list
			Node node = products.get(i);
			String nodeName = node.getName();
			for (int j = 0; j < emptyStringNodes.size(); j++) {
				if (nodeName.contains(emptyStringNodes.get(j).getName())) {
					//This node has the node with empty string inside it
					boolean flag = true;
					try {
						if (node.isChecked(emptyStringNodes.get(j))) {
							flag = false;
						}
					} catch (Exception e) {
					}
					if (flag) {
						int oldIndex = -1;
						while(true) {
							int newIndex = getStartingIndexAfter(nodeName,oldIndex + 1,emptyStringNodes.get(j).getName());
							//Gets the index of the nodes name start. Returns -1 if it does not exist 
							if(newIndex != oldIndex && newIndex != -1) {
								//newIndex should not be equal to the oldIndex
								oldIndex = newIndex;
							}
							else {
								break;
							}
							try {
								for (int k = 0; k < emptyStringNodes.get(j).nextLines.size(); k++) {
									String changeTo = "";
									for (int k2 = 0; k2 < emptyStringNodes.get(j).nextLines.get(k).size(); k2++) {
										//Constructs the string which this node should change
										changeTo +=  emptyStringNodes.get(j).nextLines.get(k).get(k2).getName();
									}
									String s = changeAfter(nodeName,changeTo,oldIndex,emptyStringNodes.get(j).getName());
									//Creates the new node name 
									Node p = searchNode(s,products);//Searches the newly created node name in the products list
									if(p == null) {
										//If the node does not exist it is created
										p = new Node(s,emptyStringNodes.size());
										for (int l = 0; l < emptyStringNodes.size(); l++) {
											//This node is created after a name change so every node with a emptyString must check it
											//but the current node with the empty string must not check it 
											//Otherwise there would be an empty loop
											p.addEmptyNode(emptyStringNodes.get(l));
										}
										products.add(p);
									}
									LinkedList<Node> parent = new LinkedList<Node>();
									parent.add(node.nextLines.getFirst().getFirst());
									p.addLine(parent);
									try {
										p.check(emptyStringNodes.get(j));//Current node with the empty string is marked as checked
										//So it would not be used later
									} catch (Exception e) {
									}
								}
							} catch (IndexOutOfBoundsException e) {
							}
						}
					}
				}
			}
		}
	}
	
	private String takeInputFromUser() {
		//Taking string input from user
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter the input string: ");
		String str = scanner.nextLine();
		scanner.close();
		return str;
	}
	
	private void nodeCheck(String input) {
		//Checks if the inputs every character is a terminal
		//If one of the characters is not a terminal then this string does not belong to the language
		for (int i = 0; i < input.length(); i++) {
			Node node = searchNode(String.valueOf(input.charAt(i)), nodes);
			if(node == null || !node.isTerminal()) {
				//Couldn't find a node with this character
				//or the node is not a terminal
				//So this string does not belong to the language
				System.out.println("This string does not belong to the language.");
				System.exit(2);
			}
		}
	}
	
	private boolean checkAlphabet(String input, int max, LinkedList<String> previous) {
		//A recursive function which tries to reach the start with replacing every product substring with its start
		//Previous is used so it will not look same strings over and over
		if(input.equals(nodes.getFirst().getName())) {
			//This input is already the starting node
			derivation.add(input);
			parseTree.add(input);
			return true;
		}
		String[] nodeNames = new String[max];//Storing the substrings for checking products. Each nth index stores strings with n + 1 length 
		int[] currentIndex = new int[max];//Current index points of the elements in nodeNames array
		for (int i = 0; i < currentIndex.length; i++) {
			currentIndex[i] = 0;
			nodeNames[i] = "";
		}
		for (int i = 0; i < input.length(); i++) {
			for (int j = 0; j < nodeNames.length; j++) {
				if(currentIndex[j] < j + 1) {
					//This index did not reached to its length so characters can be add to the end
					nodeNames[j] += input.charAt(i);
					currentIndex[j]++;
				}
				else {
					//This index already reached its limit. So before this character added it must be shifted
					nodeNames[j] = addCharToEnd(input.charAt(i),nodeNames[j]);
					currentIndex[j]++;
				}
			}
			for (int j = 0; j < nodeNames.length; j++) {
				if(currentIndex[j] >= j + 1) {
					//Only need to check a node name after it reached its limit
					//Otherwise there would be repeats 
					Node n = searchNode(nodeNames[j], products);//This name is searched in products list
					if(n != null) {
						//A product with this name exists
						for (int k = 0; k < n.nextLines.size(); k++) {
							String changeTo = n.nextLines.get(k).getFirst().getName();//The string which inputs substring will be changed
							//There must be only one node in a products nextLines list. So getting the first one is okay
							String next = changeAfter(input, changeTo,0,nodeNames[j]);//Input string with some part of it changed
							if(next.equals(nodes.getFirst().getName())) {
								//This leads to the start. So this string belongs to this alphabet.
								derivation.add(next);//Stored for printing out later
								parseTree.add(next + " to " + nodeNames[j]);
								return true;
							}
							if(!inList(previous, next)) {//Next string to look must not be already searched
														//Made this way to prevent infinite loops of searching the same strings over and over
								previous.add(next);
								if(checkAlphabet(next, max, previous)) {
									//This part has reached to the start. So this string belongs to this alphabet 
									derivation.add(next);//Stored for printing out later
									parseTree.add(changeTo + " to " + nodeNames[j]);
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;//Couldn't reach to the start
	}

	private int maximumProductLength() {
		//Returns the value of longest name of the nodes which stored in products list.
		int max = -1;
		for (int i = 0; i < products.size(); i++) {
			if (products.get(i).getName().length() > max) {
				max = products.get(i).getName().length();
			}
		}
		return max;
	}
	
	private String addCharToEnd(char ch, String str){
		//Shifts every character of the string to the left then adds the given char to the end
	    char[] charArray = str.toCharArray();
	    for (int i = 0; i < charArray.length - 1; i++) {
			charArray[i] = charArray[i + 1];
		}
	    charArray[charArray.length - 1] = ch;
	    return new String(charArray);
	}
	
	private String changeAfter(String original, String change, int index, String replaced) {
		//Changes the given replaced substring which occurs after the given index with the given change string
		//on the given original string and returns it.
		char[] charArray = original.toCharArray();
		char[] finalStr = new char[original.length() + change.length() - replaced.length()];//Change is added to the given string and replaced is deleted
		int charIndex = original.indexOf(replaced, index);
		for (int i = 0; i < charIndex; i++) {
			//String is the same till the index marked with charIndex
			finalStr[i] = charArray[i];
		}
		for (int i = 0; i < change.length(); i++) {
			//Adding the changed part
			finalStr[charIndex + i] = change.charAt(i);
		}
		for (int i = charIndex + change.length(); i < finalStr.length; i++) {
			//Adding the characters after the part thats replaced
			finalStr[i] = charArray[i - change.length() + replaced.length()];
		}
		return new String(finalStr);
	}
	
	private boolean inList(LinkedList<String> list, String str) {
		//Searches the given string in the given list
		//Returns true if it is in the list returns false otherwise
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals(str)) {
				return true;
			}
		}
		return false;
	}
	
	private int getStartingIndexAfter(String string, int index, String search) {
		//Searches the search string in string after the given index and returns its starting index
		//Returns -1 if its not in the string
		char[] array = string.toCharArray();
		char[] newString = new char[array.length - index];
		int j = 0;
		for (int i = index; i < array.length; i++) {
			newString[j] = array[i];
			j++;
		}
		String str = new String(newString);
		return str.indexOf(search);
	}
}
