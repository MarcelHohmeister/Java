import java.util.Random;
import java.util.Scanner;

public class Main {
    static Random random = new Random();

    //region board Settings
    static int height = 20;
    static int width = 20;
    static float startSpawnProbability = 0.5f;

    static boolean[][] board = new boolean[height][width];
    //endregion

    //region Runtime Settings
    static boolean limitRounds = false;
    static int maxRounds = 30;
    static float delayAmount = 0.5f;

    static int currentRound = 0;
    static volatile boolean isPaused = false;
    //endregion

    public static void main(String[] args) {
        //region Input Thread
        Thread inputThread = new Thread(new Runnable() {            //Erzeugt einen Thread, der parallel zum eigentlichen Programm läuft
            @Override
            //Überschreibt die run()-Methode aus dem Runnable-Interface
            //Notwendig, um eigene Logik in dieser Methode auszuführen
            public void run() {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String input = scanner.nextLine();              //Warten auf Input-Taste
                    if (input.isEmpty()) {                          //Wenn Enter gedrückt wird
                        isPaused = !isPaused;                       //Pausiert oder setzt fort
                        System.out.println(isPaused ? "Simulation pausiert" : "Simulation fortgesetzt"); //Debug:
                    }
                }
            }
        });
        //endregion

        inputThread.start();  //Thread starten

        fillBoard();
        //placeGlider(3, 3);

        drawBoard();
        currentRound++;
        startDelay();

        while (true) {
            if (!isPaused) {
                if (limitRounds && currentRound >= maxRounds) {
                    System.exit(0);
                } else {
                    boolean[][] newBoard = new boolean[height][width];

                    for (int i = 0; i < width; i++) {
                        for (int j = 0; j < height; j++) {
                            int neighbours = countNeighbours(i, j);
                            newBoard[i][j] = manageSurvival(neighbours, board[i][j]);
                        }
                    }

                    board = newBoard;

                    drawBoard();
                    startDelay();
                    currentRound++;
                }
            }
        }
    }

    static void placeGlider(int x, int y) {
        board[x][y + 1] = true;
        board[x + 1][y + 2] = true;
        board[x + 2][y] = true;
        board[x + 2][y + 1] = true;
        board[x + 2][y + 2] = true;
    }


    static void startDelay(){
        try {
            Thread.sleep((long)(delayAmount * 1000L));              //delayAmount wird in Millisekunden umgewandelt, und für Thread.sleep() als long umgeschrieben (Thread.sleep will ein long (64-Bit) anstatt einem int (32-Bit))
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void fillBoard(){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // Zelle mit 50% Wahrscheinlichkeit lebendig oder tot machen
                board[i][j] = random.nextFloat() <= startSpawnProbability;
            }
        }
    }

    static int countNeighbours(int row, int cell){
        int amount = 0;

        for(int i = -1; i <= 1; i++){
            for(int j = -1; j <= 1; j++){   //In alle 8 Richtungen prüfen
                if(!(i == 0 && j == 0)){        //Nicht sich selbst prüfen
                    int newRow = (row + i + height) % height;
                    int newCell = (cell + j + width) % width;

                    if(newRow >= 0 && newRow < height && newCell >= 0 && newCell < width){
                        if(board[newRow][newCell]){
                            amount++;
                        }
                    }
                }
            }
        }

        return amount;
    }

    static boolean manageSurvival(int neighbours, boolean isAlive) {
        if (isAlive) {
            return neighbours == 2 || neighbours == 3; // überlebt
        } else {
            return neighbours == 3; // wird geboren
        }
    }

    static void drawBoard(){
        System.out.println("Gen: " + currentRound);
        for(boolean[] rows: board){
            for(boolean cell: rows){
                System.out.print(cell ? "⬛" : "⬜");
            }
            System.out.println();  // Zeilenumbruch nach jeder Reihe
        }
        System.out.println();
    }
}