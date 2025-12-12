package lsystem;

import java.util.HashMap;

public class LSystem {

    private String current;
    private HashMap<Character, String> rules = new HashMap<>();

    public LSystem(String axiom) {
        this.current = axiom;
    }

    public void addRule(char from, String to) {
        rules.put(from, to);
    }

    public void iterate(int n) {
        for (int i = 0; i < n; i++) {
            StringBuilder next = new StringBuilder();
            for (char c : current.toCharArray()) {
                if (rules.containsKey(c)) next.append(rules.get(c));
                else next.append(c);
            }
            current = next.toString();
        }
    }

    public String getString() {
        return current;
    }
}
