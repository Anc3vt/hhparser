package ru.ancevt.webparsers.headhunter.tools;

/**
 *
 * @author ancevt
 */
public class ScriptGenerator {
    public static void main(String[] args) {
        final int count = 5000000;
        final int step = 100000;
        
        for(int i = 0; i < count; i += step) {
            System.out.printf("java -jar hhwdg.jar --output-directory out%d Companies.id_ranges=\"%d-%d\"\n", i, i, i + step);
        }
    }
    
}
