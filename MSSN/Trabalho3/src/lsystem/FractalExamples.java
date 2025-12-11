package lsystem;

public class FractalExamples {

    // Koch Snowflake
    public static LSystem koch() {
        LSystem l = new LSystem("F--F--F");
        l.addRule('F', "F+F--F+F");
        l.iterate(4);
        return l;
    }

    // Planta clássica
    public static LSystem planta() {
        LSystem l = new LSystem("X");
        l.addRule('X', "F[+X][-X]FX");
        l.addRule('F', "FF");
        l.iterate(6);
        return l;
    }
}
