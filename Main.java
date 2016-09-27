/* WORD LADDER Main.java
 * EE422C Project 3 submission by
 * Andrew Chau
 * ac57662
 * 16460
 * Pratyush Hebera
 * pb22426
 * 16460
 * Slip days used: <0>
 * Git URL: github.com/andrewcchau/422Lab3
 * Fall 2016
 */


package assignment3;
import java.util.*;
import java.io.*;

public class Main {
	
	// static variables and constants only here.
	private static ArrayList<String> words; //holds the words from user input
	private static Set<String> dict; //dictionary
	private static ArrayList<String> link; //the ladder between start and end
	private static Set<String> visited; //visited words
	private static Set<String> dead; //dead ends
	private static ArrayList<String> temp; //multipurpose arrList

	public static void main(String[] args) throws Exception {
		Scanner kb;	// input Scanner for commands
		PrintStream ps;	// output file
		// If arguments are specified, read/write from/to files instead of Std IO.
		if (args.length != 0) {
			kb = new Scanner(new File(args[0]));
			ps = new PrintStream(new File(args[1]));
			System.setOut(ps);			// redirect output to ps
		} else {
			kb = new Scanner(System.in);// default from Stdin
			ps = System.out;			// default to Stdout
		}
		initialize();
		
		//read in initial command/words
		words = parse(kb);
		
		//if /quit or isn't 2 words, continue. Otherwise quit
		while(!words.isEmpty() && !words.get(0).equals("QUIT") && words.size() == 2){
			ArrayList<String> end = getWordLadderDFS(words.get(0), words.get(1));
			printLadder(end);
			initialize();

			ArrayList<String> end2 = getWordLadderBFS(words.get(0),words.get(1));
			printLadder(end2);
			initialize();

			//get more commands/words
			words.clear();
			words = parse(kb);
		}
	}
	
	public static void initialize() {
		// initialize your static variables or constants here.
		// We will call this method before running our JUNIT tests.  So call it 
		// only once at the start of main.

		dict = makeDictionary();
		link = new ArrayList<String>();
		visited = new HashSet<String>();
		dead = new HashSet<String>();
	}
	
	/**
	 * @param keyboard Scanner connected to System.in
	 * @return ArrayList of 2 Strings containing start word and end word. 
	 * If command is /quit, return empty ArrayList. 
	 */
	public static ArrayList<String> parse(Scanner keyboard) {
		String in = keyboard.nextLine();
		String[] words;
		ArrayList<String> word = new ArrayList<String>();
		int index = 0;
		
		//quit command
		if(in.trim().equals("/quit")){
			word.add("QUIT");
			return word;
		}else{
			words = in.trim().split(" ");
			while(index < words.length){
				words[index] = words[index].trim();
				if(!words[index].equals("") && !words[index].equals(" ")){
					word.add(words[index].toUpperCase());
				}
				index++;
			}
		}
		return word;
	}
	
	/**
	 * DFS through dictionary
	 * @param start beginning word
	 * @param end end word
	 * @return ladder from start to end if it exists, otherwise null
	 */
	public static ArrayList<String> getWordLadderDFS(String start, String end) {
		
		// Returned list should be ordered start to end.  Include start and end.
		// Return empty list if no ladder.
		// TODO some code
		Set<String> dict = makeDictionary();
		ArrayList<String> sim = new ArrayList<String>();
		String n;
		Iterator<String> i = dict.iterator();
	
		try{
			//get similar words
			while(i.hasNext()){
				n = i.next();
				if(similar(start, n) && !visited.contains(n)){
					sim.add(n);
				}
			}
			
			//add dead ends to dead
			if(sim.isEmpty()){
				dead.add(start);
				dict.remove(start);
			}
			
			//check current word
			if(start.equals(end)){
				link.add(start);
				return link;
			}else if(dead.contains(start)){ //current word is a dead end
				return null;
			}
			else if(similar(start, end)){ //current word is similar to goal word
				visited.add(start);
				link.add(start);
				return getWordLadderDFS(end, end);
			}else{
				//search words similar to current word for a link
				for(int ind = 0; ind < sim.size(); ind++){
					visited.add(start);
					temp = getWordLadderDFS(sim.get(ind), end);
					if(temp != null){
						temp.add(0, start);
						return temp;
					}
				}
			}
			
			//current word's relations have dead end
			dead.add(start);
			visited.remove(start);
			return null;
		}catch (StackOverflowError e){
			return getWordLadderDFS(end, start);
		}
	}
	
 	/**
	 * BFS through dictionary
	 * @param start beginning word
	 * @param end end word
	 * @return ladder from start to end if it exists, otherwise null
	 */
    public static ArrayList<String> getWordLadderBFS(String start, String end) {
		ArrayList<Node> connections = new ArrayList<Node>();
		dict.remove(start); //so it won't loop back
		link.add(start);
		Node c;
		
		//creates the queue
		createConnections(new Node(start), connections);
		
		if(connections.isEmpty()){
			return null;
		}
		
		//parse through the connections and add as needed
		for(int index = 0; index < connections.size(); index++){
			c = connections.get(index);
			
			if(!c.getWord().equals(end)){ //current != end word
				createConnections(c, connections);
			}else if(c.getWord().equals(end)){
				createLinksBFS(c);
				break;
			}else{
				return null;
			}
		}
		
		if(!link.get(0).equals(start) || !link.get(link.size() - 1).equals(end)){
			link.clear();
			return null;
		}else{
			return link;
		}
    }
    
    /**
     * Creates the dictionary from a given file
     * @return dictionary as a set of strings
     */
	public static Set<String>  makeDictionary () {
		Set<String> words = new HashSet<String>();
		Scanner infile = null;
		try {
			infile = new Scanner (new File("five_letter_words.txt"));
		} catch (FileNotFoundException e) {
			System.out.println("Dictionary File not Found!");
			e.printStackTrace();
			System.exit(1);
		}
		while (infile.hasNext()) {
			words.add(infile.next().toUpperCase());
		}
		return words;
	}
	
	/**
	 * Prints the ladder of words
	 * @param ladder the ladder of words start to end
	 */
	public static void printLadder(ArrayList<String> ladder) {
		if(ladder == null){
			System.out.println("no word ladder can be found between " + words.get(0)+ " and " + words.get(1) + ".");
		}else{
			for(int i = 0; i < ladder.size(); i++){
				System.out.println(ladder.get(i));
			}
		}
	}

	/**
	 * Determines whether two given words have a 1 letter difference
	 * @param a first word
	 * @param b second word
	 * @return true if letter difference is 1, otherwise false
	 */
	private static boolean similar(String a, String b){
		int difference = 0;
		for(int i = 0; i < a.length(); i++){
			if(a.toUpperCase().charAt(i) != b.toUpperCase().charAt(i)){
				difference++;
			}
			if(difference > 1){
				return false;
			}
		}
		if(difference == 1){
			return true;
		}
		return false;
	}
	
	/**
	 * Adds similar words onto the queue
	 * @param origin word to compare
	 * @param exclude words to exclude
	 * @param q where to add
	 */
	private static void createConnections(Node origin, ArrayList<Node> q){
		Iterator<String> i = dict.iterator();
		ArrayList<String> rem = new ArrayList<String>();
		String n;

		//create connection
		while(i.hasNext()){
			n = i.next();
			if(similar(origin.getWord(), n)){
				q.add(new Node(origin, n));
				rem.add(n);
			}
		}
		
		//remove from dictionary
		for(int index = 0; index < rem.size(); index++){
			dict.remove(rem.get(index));
		}
	}
	
	/**
	 * Creates a link based on the ending node as a result of BFS
	 * @param end end node
	 */
	private static void createLinksBFS(Node end){
		Stack<Node> rev = new Stack<Node>();
		
		//creates the reverse order of ladder
		while(end.getParent() != null){
			rev.push(end);	
			end = end.getParent();
		}
		
		//flip the order
		while(!rev.isEmpty()){
			link.add(rev.pop().getWord());
		}
	}
}