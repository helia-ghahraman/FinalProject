package Interpreter2;

import javafx.application.Application;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Program {
    static Integer lineNumber = 0;
    static String path = null;
    static ArrayList<String> codes = null;
    static String commentPattern=" [/]{2}.+";
    static String logicPattern="^\\b([\\w|\\$]+[ ][=][ ]([\\d]|[\\w|\\$]+))( [\\-+*/] ([\\d]|[\\w|\\$]+))*\\b( [/]{2}.+)?$";
    static String forPattern="^for [1-9]( [/]{2}.+)?$";
    static String endforPattern="^end for( [/]{2}.+)?$";
    static String printPattern="^print .+( [/]{2}.+)?$";
    //Main Method ... ********************************************************************

    public static void main(String[] args) throws IOException {
        Application.launch(Graphics.class,args);
        path = "TextFiles//src6.txt";
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("File does not exist!");
        } else {
            if (file.isDirectory()) {
                throw new IllegalArgumentException("there is a directory...");
            } else if (file.isFile()) {
                readFile(file);
            }
        }
    }
    //Other methods ... *******************************************************************
    public static void readFile(File f) throws IOException {
        Boolean faz1 = true; //true -> faz1, false -> faz2
        Scanner sc = new Scanner(f);
        try {
            while (faz1) {
                String pattern="^\\b(((int )|(float ))[\\w|\\$]+([ ][=][ ]([\\d]|[\\w|\\$]+))?)|^\\b([\\w|\\$]+[ ][=][ ]([\\d]|[\\w|\\$]+))?( [/]{2}.+)?$";
                String line = sc.nextLine();
                lineNumber++;
                line = line.trim();
                line = line.replaceAll("//", " //");
                line = line.replaceAll("([ ]+|[\\t]+)+", " ");
                String[] tokens = line.split(" ");
                if (line.isEmpty()||line.matches(commentPattern)) continue;
                if (line.equals("%%")) {
                    faz1 = false; //jump to faz2
                } else if(line.matches(pattern)){
                    GiveValue giveValue = new GiveValue(tokens);
                }else {
                    throw new IllegalArgumentException("this line is NOT valied!!!(at line: " + lineNumber + ")");
                }
            }
            //start faz2
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                lineNumber++;
                line = line.trim();
                line = line.replaceAll("([ ]+|[\\t]+)+", " ");
                String[] tokens = line.split(" ");
                if (tokens[0].equals("for")) gotoEnd(sc);
                if (makeTokens(line)==-1)continue;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            sc.close();
        }
    }

    public static int makeTokens(String line) throws IOException {
        line = line.replaceAll("//", " //");
        line = line.replaceAll("([ ]+|[\\t]+)+", " ");
        String[] tokens = line.split(" ");
        if (line.matches(logicPattern)) {
            if (tokens.length == 3) {
                GiveValue giveValue = new GiveValue(tokens);
            } else if (tokens.length == 5) {
                Logic logic = new Logic(tokens);
            }
        } else if (line.matches(forPattern)) {
            codes = new ArrayList();
            int start = Program.lineNumber;
            int finish = search(start, codes);
            Program.lineNumber = finish + 1;
            Loop loop = new Loop(tokens, start, finish, codes);
        } else if (line.matches(printPattern)) {
            Print print = new Print(tokens);
            return print.getCharNumber();
        } else if (line.isEmpty()||line.matches(commentPattern)) return -1;
        else System.err.println("this lines is not valid (at line: " + lineNumber + ") " + "--- The program is Stopped.");

        return 0;
        /*switch (tokens.length) {
            case 5:
                Logic logic = new Logic(tokens);
                break;
            case 2:
                choose(tokens);
                break;
            case 3:
                GiveValue giveValue = new GiveValue(tokens);
                break;
            default:
                System.err.println("this lines is not valid (at line: " + lineNumber + ") " + "--- The program is Stopped.");
        }*/
    }

  /*  public static int choose(String[] tokens) throws IOException {
        String pattern = "[1-9]+[0]*";
        if (tokens[0].equals("for")) {
            if (tokens[1].matches(pattern)) {
                codes = new ArrayList();
                int start = Program.lineNumber;
                int finish = search(start, codes);
                Program.lineNumber = finish + 1;
                Loop loop = new Loop(tokens, start, finish, codes);
            } else {
                System.err.println("Loop counter is NOT valid (at line: " + lineNumber + ") " + "--- The program is Stopped.");
            }
        } else if (tokens[0].equals("print")) {
            Print print = new Print(tokens);
            return print.getCharNumber();
        } else {
            throw new IllegalArgumentException("Line does not make sense (" + "at line: " + lineNumber + ")");
        }
        return 0;
    }*/

    private static int search(int start, ArrayList codes) throws IOException {
        String line = null;
        int forCounter = 0;
        int endCounter = 0;
        int counter = start + 1;
        boolean sw = true;
        String[] array = null;
        while (sw) {
            line = getLine(counter);
            if ( line == null &&forCounter == endCounter ){
                System.err.println("Loop does not have an 'end for'");
                throw new IllegalArgumentException("Loop does not have an 'end for'");
            }
            line = line.trim();
            line = line.replaceAll("([ ]+|[\\t]+)+", " ");
            array = line.split(" ");
            if (array[0].equals("for"))
                forCounter++;
            else if (array[0].equals("end") && (endCounter < forCounter))
                endCounter++;
            else if (array[0].equals("end") && (endCounter == forCounter))
                return (counter - 1);

            codes.add(line);
            counter++;
        }
        return 0;
    }

    public static String getLine(int lineNum) throws IOException {
        String line;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            for (int i = 0; i < lineNum - 1; i++) {
                bufferedReader.readLine();
            }
            line = bufferedReader.readLine();
            return line;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public static void gotoEnd(Scanner sc) {
        String line = null;
        int forCount = 1;
        int endCount = 0;
        String[] tokens = null;
        while (sc.hasNextLine()) {
            line = sc.nextLine();
            line = line.trim();
            line = line.replaceAll("([ ]+|[\\t]+)+", " ");
            tokens = line.split(" ");
            if (tokens[0].equals("for")) forCount++;
            if (tokens[0].equals("end")) endCount++;
            if (endCount >= forCount) break;
        }
    }
}
