import java.util.Arrays;
public class Equation {

    public static void main(String[] args) {
        double[][] A = {
                { -1, 1, 1, 0, 0 },
                { 1, -1, 0, 1, 0 },
                { 1, 1, 0, 0, 1 }
        };
        double[] b = { 1, 1, 2 };
        double[] c = { 1, 1, 0, 0, 0 };

        double maxValue = simplexTwoPhase(A, b, c);

        System.out.println("Valeur maximale : " + maxValue);
    }

    public static double simplexTwoPhase(double[][] A, double[] b, double[] c) {
        int m = A.length; // Nombre de contraintes
        int n = A[0].length; // Nombre de variables

        // Phase 1 : Trouver une solution de base admissible

        // Ajout de variables artificielles
        double[][] tableau = new double[m + 1][n + m + 1];
        for (int i = 0; i < m; i++) {
            System.arraycopy(A[i], 0, tableau[i], 0, n);
            tableau[i][n + i] = 1.0;
        }

        // Coefficients des variables artificielles
        double[] cPhase1 = new double[n + m];
        for (int i = 0; i < m; i++) {
            cPhase1[n + i] = -1.0;
        }

        // Phase 1 du Simplex
        int[] basis = new int[m];
        for (int i = 0; i < m; i++) {
            basis[i] = n + i;
        }

        double[] x = simplex(tableau, b, cPhase1, basis);

        if (x == null || x[n + m] != 0) {
            throw new ArithmeticException("Le programme linéaire est infaisable");
        }

        // Phase 2 : Maximisation du Simplex

        // Suppression des variables artificielles
        double[][] tableauPhase2 = new double[m][n + 1];
        for (int i = 0; i < m; i++) {
            System.arraycopy(tableau[i], 0, tableauPhase2[i], 0, n);
            tableauPhase2[i][n] = tableau[i][n + m];
        }

        double[] cPhase2 = new double[n];
        System.arraycopy(c, 0, cPhase2, 0, n);

        // Phase 2 du Simplex
        x = simplex(tableauPhase2, b, cPhase2, basis);

        if (x == null) {
            throw new ArithmeticException("Le programme linéaire est non borné");
        }

        return x[n];
    }

    public static double[] simplex(double[][] tableau, double[] b, double[] c, int[] basis) {
        int m = tableau.length; // Nombre de contraintes
        int n = tableau[0].length - 1; // Nombre de variables

        while (true) {
            // Recherche de la colonne pivot
            int q = findPivotColumn(tableau, c);

            if (q == -1) {
                break; // Optimal
            }

            // Recherche de la ligne pivot
            int p = findPivotRow(tableau, b, q);

            if (p == -1) {
                return null; // Non borné
            }

            // Mise à jour du tableau
            pivot(tableau, b, c, basis, p, q);
        }

        double[] x = new double[n + 1];
        for (int i = 0; i < m; i++) {
            if (basis[i] < n) {
                x[basis[i]] = tableau[i][n];
            }
        }

        return x;
    }

    public static int findPivotColumn(double[][] tableau, double[] c) {
        int n = tableau[0].length - 1; // Nombre de variables
        int q = -1;

        for (int j = 0; j < n; j++) {
            if (c[j] > 0) {
                if (q == -1 || tableau[tableau.length - 1][j] > tableau[tableau.length - 1][q]) {
                    q = j;
                }
            }
        }

        return q;
    }

    public static int findPivotRow(double[][] tableau, double[] b, int q) {
        int m = tableau.length; // Nombre de contraintes
        int p = -1;

        for (int i = 0; i < m; i++) {
            if (tableau[i][q] > 0) {
                if (p == -1 || tableau[i][tableau[0].length - 1] / tableau[i][q] < tableau[p][tableau[0].length - 1]
                        / tableau[p][q]) {
                    p = i;
                }
            }
        }

        return p;
    }

    public static void pivot(double[][] tableau, double[] b, double[] c, int[] basis, int p, int q) {
        int m = tableau.length;
        int n = tableau[0].length;

        // Mettre la colonne q à 1
        for (int i = 0; i < m; i++) {
            if (i != p) {
                tableau[i][q] = 0.0;
            }
        }
        tableau[p][q] = 1.0;

        // Mettre la ligne p à l'inverse
        for (int j = 0; j < n; j++) {
            if (j != q) {
                tableau[p][j] /= tableau[p][q];
            }
        }
        tableau[p][q] = 1.0;

        // Mise à jour du reste du tableau
        for (int i = 0; i < m; i++) {
            if (i != p) {
                double alpha = tableau[i][q];
                for (int j = 0; j < n; j++) {
                    if (j != q) {
                        tableau[i][j] -= tableau[p][j] * alpha;
                    }
                }
                tableau[i][q] = 0.0;
            }
        }

        // Mise à jour de b et de la base
        b[p] /= tableau[p][q];
        for (int i = 0; i < m; i++) {
            if (i != p) {
                b[i] -= tableau[i][q] * b[p];
            }
        }

        basis[p] = q;
    }
}
