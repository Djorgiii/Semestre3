package lsystem;

public class ArvoreFrutos {

    public static LSystem gerar() {
        LSystem l = new LSystem("F");

        l.addRule('F', "G[+F]-F");
        l.addRule('G', "GG");

        l.iterate(7); // 6 ou 7 fica perfeito

        return l;
    }
}
