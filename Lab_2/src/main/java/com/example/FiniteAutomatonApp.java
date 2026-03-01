package com.example;

import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Format;
import java.util.*;
import static guru.nidi.graphviz.model.Factory.*;

public class FiniteAutomatonApp {

    public static void main(String[] args) throws Exception {

        System.out.println("Running NEW VERSION");

        FAProcessor processor = new FAProcessor();

        processor.buildNFA();

        System.out.println("=== REGULAR GRAMMAR ===");
        processor.convertToGrammar();

        System.out.println("\n=== DFA OR NFA? ===");
        processor.checkDeterminism();

        System.out.println("\n=== NFA TO DFA ===");
        processor.convertNFAtoDFA();

        System.out.println("\n=== GENERATING VISUAL ===");
        processor.generateGraph("fa.png"); // output image
        System.out.println("Graph saved as fa.png");
    }
}

// Class containing all FA functionality
class FAProcessor {

    String[] states = { "q0", "q1", "q2", "q3", "q4" };
    char[] alphabet = { 'a', 'b' };
    HashSet<String> finalStates = new HashSet<>();

    Map<String, Map<Character, Set<String>>> nfa = new HashMap<>();

    public FAProcessor() {
        finalStates.add("q4");
    }

    // Build NFA
    void buildNFA() {
        for (String s : states)
            nfa.put(s, new HashMap<>());
        addTransition("q0", 'a', "q1");
        addTransition("q1", 'b', "q1");
        addTransition("q1", 'a', "q2");
        addTransition("q2", 'b', "q2");
        addTransition("q2", 'b', "q3"); // The same state again
        addTransition("q3", 'b', "q4");
        addTransition("q3", 'a', "q1");
    }

    void addTransition(String from, char input, String to) {
        nfa.get(from).putIfAbsent(input, new HashSet());
        nfa.get(from).get(input).add(to);
    }

    // a) Convert FA to Regular Grammar
    void convertToGrammar() {

        HashMap<String, String> mapToLetters = new HashMap<>();

        // Use the mapping set for grammar
        int i = 0;
        for (String state : states) {
            mapToLetters.put(state, String.valueOf((char)('A' + i)));
            i++;
        }

        for (String state : states) {
            Map<Character, Set<String>> transitions = nfa.get(state);
            for (Character input : transitions.keySet()) {
                for (String next : transitions.get(input)) {
                    System.out.println(mapToLetters.get(state) + " -> " + input + mapToLetters.get(next));
                }
            }
            if (finalStates.contains(state))
                System.out.println(mapToLetters.get(state) + " -> ε");
            i++;
        }
    }

    // b) Check if DFA
    void checkDeterminism() {
        boolean isDFA = true;
        for (String state : states) {
            for (char symbol : alphabet) {
                Set<String> nextStates = nfa.get(state).getOrDefault(symbol, new HashSet<>());
                if (nextStates.size() > 1)
                    isDFA = false;
            }
        }
        System.out.println(isDFA ? "DFA" : "NFA (NDFA)");
    }

    // c) Subset Construction Algorithm NFA to DFA
    void convertNFAtoDFA() {
        Set<Set<String>> dfaStates = new HashSet<>();
        Queue<Set<String>> queue = new LinkedList<>();
        Set<String> start = new HashSet<>();
        start.add("q0");
        queue.add(start);
        dfaStates.add(start);

        while (!queue.isEmpty()) {
            Set<String> current = queue.poll();
            for (char symbol : alphabet) {
                Set<String> newState = new HashSet<>();
                for (String s : current)
                    newState.addAll(nfa.get(s).getOrDefault(symbol, new HashSet<>()));
                System.out.println(current + " --" + symbol + "--> " + newState);
                if (!newState.isEmpty() && !dfaStates.contains(newState)) {
                    dfaStates.add(newState);
                    queue.add(newState);
                }
            }
        }
    }

    // d) Generate Graph Visual using Graphviz
    void generateGraph(String filename) throws Exception {
        MutableGraph g = mutGraph("FA").setDirected(true);
        Map<String, MutableNode> nodes = new HashMap<>();

        for (String s : states) {
            MutableNode n = mutNode(s);

            if (finalStates.contains(s)) {
                n.add(Shape.DOUBLE_CIRCLE);
            }

            nodes.put(s, n);
            g.add(n);
        }

        for (String from : nfa.keySet()) {
            for (Character c : nfa.get(from).keySet()) {
                for (String to : nfa.get(from).get(c)) {

                    nodes.get(from).addLink(
                            to(nodes.get(to)).with(Label.of("" + c)));

                }
            }
        }

        Graphviz.fromGraph(g).render(Format.PNG).toFile(new java.io.File(filename));
    }
}