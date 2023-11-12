import java.util.Arrays;

public class SimplexTwoPhase {

    public static void main(String[] args) {
        // Exemple d'utilisation pour un problème de min
        
          double[][] constraints = {
          { 1, 2, 0 },
          { 2, 1, 1 }
          };
          double[] objectiveCoefficients = { 700, 800, 300 };
          double[] constraintConstants = { 10, -4 };
          boolean maximize = false;
          // Exemple d'utilisation pour un problème de maximisation
       /*  double[][] constraints = {
                { 5, 2, 1, 0 },
                { -1, 3, 0, 1 }
        };
        double[] objectiveCoefficients = { 2, 6, 0, 0 };
        double[] constraintConstants = { 15, 3 };

        boolean maximize = true;*/

        double[] solution = solveLinearProgram(constraints, objectiveCoefficients, constraintConstants, maximize);

        System.out.println("Solution optimale : " + Arrays.toString(solution));
    }

    public static double[] solveLinearProgram(double[][] constraints, double[] objectiveCoefficients,
            double[] constraintConstants, boolean maximize) {
        int numConstraints = constraints.length;
        int numVariables = objectiveCoefficients.length;

        // Phase 1
        double[][] phase1Tableau = createPhase1Tableau(constraints, objectiveCoefficients, constraintConstants);
        double[] phase1Solution = solvePhase(phase1Tableau);

        // Vérifier si la phase 1 a trouvé une solution réalisable
        if (phase1Solution[numVariables] > 1e-10) {
            System.out.println("Le problème est infaisable.");
            return null;
        }

        // Phase 2
        double[][] phase2Tableau = createPhase2Tableau(phase1Solution, constraints, objectiveCoefficients);
        double[] phase2Solution = solvePhase(phase2Tableau);

        // Vérifier si la phase 2 a trouvé une solution optimale
        if (maximize) {
            if (phase2Solution[numVariables] < -1e-10) {
                System.out.println("Le problème est non borné.");
                return null;
            }
        } else {
            if (phase2Solution[numVariables] > 1e-10) {
                System.out.println("Le problème est non borné.");
                return null;
            }
        }

        // Récupérer la solution optimale
        double[] solution = new double[numVariables];
        for (int i = 0; i < numConstraints; i++) {
            if (phase2Solution[i] >= 1e-10) {
                int basicVariableIndex = findBasicVariable(phase2Tableau, i);
                solution[basicVariableIndex] = phase2Solution[i];
            }
        }

        return solution;
    }

    private static double[][] createPhase1Tableau(double[][] constraints, double[] objectiveCoefficients,
            double[] constraintConstants) {
        int numConstraints = constraints.length;
        int numVariables = objectiveCoefficients.length;

        double[][] tableau = new double[numConstraints + 1][numVariables + numConstraints + 1];

        // Variables d'écart
        for (int i = 0; i < numConstraints; i++) {
            tableau[i][numVariables + i] = 1;
        }

        // Variables artificielles
        for (int i = 0; i < numConstraints; i++) {
            tableau[i][numVariables + numConstraints] = 1;
        }

        // Coefficients des variables objectives
        System.arraycopy(objectiveCoefficients, 0, tableau[numConstraints], 0, numVariables);

        // Contraintes et termes constants
        for (int i = 0; i < numConstraints; i++) {
            System.arraycopy(constraints[i], 0, tableau[i], 0, numVariables);
            tableau[i][numVariables + numConstraints] = constraintConstants[i];
        }

        return tableau;
    }

    private static double[][] createPhase2Tableau(double[] phase1Solution, double[][] constraints,
            double[] objectiveCoefficients) {
        int numConstraints = constraints.length;
        int numVariables = objectiveCoefficients.length;

        double[][] tableau = new double[numConstraints + 1][numVariables + 1];

        // Coefficients des variables objectives
        System.arraycopy(objectiveCoefficients, 0, tableau[numConstraints], 0, numVariables);

        // Contraintes
        for (int i = 0; i < numConstraints; i++) {
            System.arraycopy(constraints[i], 0, tableau[i], 0, numVariables);
        }

        // Mise à jour des coefficients des variables objectives avec la solution de la
        // phase 1
        for (int i = 0; i < numVariables; i++) {
            double coefficient = 0.0;
            for (int j = 0; j < numConstraints; j++) {
                coefficient += constraints[j][i] * phase1Solution[j];
            }
            tableau[numConstraints][i] -= coefficient;
        }

        return tableau;
    }

    private static double[] solvePhase(double[][] tableau) {
        int numVariables = tableau[0].length - 1;
        int numConstraints = tableau.length - 1;

        while (true) {
            int enteringColumn = findEnteringColumn(tableau, numVariables);
            if (enteringColumn == -1) {
                break; // Solution optimale trouvée
            }

            int leavingRow = findLeavingRow(tableau, enteringColumn, numConstraints);
            if (leavingRow == -1) {
                System.out.println("Le problème est non borné.");
                return null;
            }

            pivot(tableau, leavingRow, enteringColumn);
        }

        return tableau[numConstraints];
    }

    private static int findEnteringColumn(double[][] tableau, int numVariables) {
        int enteringColumn = -1;
        double minValue = 0.0;

        for (int i = 0; i < numVariables; i++) {
            if (tableau[tableau.length - 1][i] < minValue) {
                minValue = tableau[tableau.length - 1][i];
                enteringColumn = i;
            }
        }

        return enteringColumn;
    }

    private static int findLeavingRow(double[][] tableau, int enteringColumn, int numConstraints) {
        int leavingRow = -1;
        double minRatio = Double.MAX_VALUE;

        for (int i = 0; i < numConstraints; i++) {
            if (tableau[i][enteringColumn] > 0.0) {
                double ratio = tableau[i][tableau[0].length - 1] / tableau[i][enteringColumn];
                if (ratio < minRatio) {
                    minRatio = ratio;
                    leavingRow = i;
                }
            }
        }

        return leavingRow;
    }

    private static void pivot(double[][] tableau, int leavingRow, int enteringColumn) {
        int numRows = tableau.length;
        int numColumns = tableau[0].length;

        double pivotElement = tableau[leavingRow][enteringColumn];

        // Mettre à jour la ligne du pivot
        for (int j = 0; j < numColumns; j++) {
            tableau[leavingRow][j] /= pivotElement;
        }

        // Mettre à jour les autres lignes
        for (int i = 0; i < numRows; i++) {
            if (i != leavingRow) {
                double ratio = tableau[i][enteringColumn];
                for (int j = 0; j < numColumns; j++) {
                    tableau[i][j] -= ratio * tableau[leavingRow][j];
                }
            }
        }
    }

    private static int findBasicVariable(double[][] tableau, int row) {
        int numVariables = tableau[0].length - 1;
        int basicVariable = -1;

        for (int i = 0; i < numVariables; i++) {
            if (Math.abs(tableau[row][i] - 1.0) < 1e-10) {
                if (basicVariable == -1) {
                    basicVariable = i;
                } else {
                    return -1; // Plus d'une variable basique dans la ligne
                }
            }
        }

        return basicVariable;
    }
}
