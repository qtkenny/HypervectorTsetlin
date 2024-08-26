package hyperdimension.examples.validations;

import java.util.Arrays;
import java.util.Random;

public class HDSequenceRepresentation {

    private static final int D = 10000;  // Dimensionality of the hyperdimensional space
    private static final Random rng = new Random();

    public static void main(String[] args) {
        // Example sequences (e.g., sequences of integers representing different classes)
        int[][] sequences = {
                {1, 2, 3, 4, 5},
                {6, 7, 8, 9, 0},
                {1, 3, 5, 7, 9},
                {2, 4, 6, 8, 0}
        };

        // Corresponding class labels
        int[] labels = {0, 1, 0, 1};

        // Parameters for n-gram encoding
        int n = 3;  // Length of n-grams

        // Generate random vectors for values and positions
        boolean[][] valueVectors = generateRandomVectors(10, D);  // Assuming values are in the range [0, 9]
        boolean[][] positionVectors = generateRandomVectors(n, D);  // Positions within the n-gram

        // Encode the sequences
        boolean[][] encodedSequences = new boolean[sequences.length][D];
        for (int i = 0; i < sequences.length; i++) {
            encodedSequences[i] = encodeSequence(sequences[i], valueVectors, positionVectors, n);
        }

        // Classify using k-NN (k=1 for simplicity)
        int[] predictedLabels = classifySequences(encodedSequences, labels, encodedSequences, 1);

        // Output the classification results
        for (int i = 0; i < predictedLabels.length; i++) {
            System.out.println("Sequence " + Arrays.toString(sequences[i]) + " is classified as " + predictedLabels[i]);
        }

        // Evaluate classification accuracy
        int correct = 0;
        for (int i = 0; i < labels.length; i++) {
            if (labels[i] == predictedLabels[i]) {
                correct++;
            }
        }
        double accuracy = (double) correct / labels.length;
        System.out.println("Classification accuracy: " + accuracy);
    }

    // Generate random binary vectors
    public static boolean[][] generateRandomVectors(int numVectors, int dimension) {
        boolean[][] vectors = new boolean[numVectors][dimension];
        for (int i = 0; i < numVectors; i++) {
            for (int j = 0; j < dimension; j++) {
                vectors[i][j] = rng.nextBoolean();
            }
        }
        return vectors;
    }

    // Encode a sequence using n-gram encoding and temporal binding
    public static boolean[] encodeSequence(int[] sequence, boolean[][] valueVectors, boolean[][] positionVectors, int n) {
        int length = sequence.length;
        int numGrams = length - n + 1;

        boolean[][] nGrams = new boolean[numGrams][D];

        // Generate n-grams with temporal binding
        for (int i = 0; i < numGrams; i++) {
            boolean[] nGramVector = new boolean[D];
            Arrays.fill(nGramVector, false);

            for (int j = 0; j < n; j++) {
                boolean[] permutedValueVector = permute(valueVectors[sequence[i + j]], j);
                boolean[] positionVector = positionVectors[j];
                boolean[] combinedVector = xorVectors(permutedValueVector, positionVector);
                nGramVector = xorVectors(nGramVector, combinedVector);
            }

            nGrams[i] = nGramVector;
        }

        // Aggregate n-grams to form the final sequence vector
        return aggregateVectors(nGrams);
    }

    // Perform permutation on a binary vector using a cyclical shift
    public static boolean[] permute(boolean[] vector, int shift) {
        int length = vector.length;
        boolean[] permutedVector = new boolean[length];
        for (int i = 0; i < length; i++) {
            permutedVector[(i + shift) % length] = vector[i];
        }
        return permutedVector;
    }

    // Perform XOR on two binary vectors
    private static boolean[] xorVectors(boolean[] vector1, boolean[] vector2) {
        int length = vector1.length;
        boolean[] result = new boolean[length];
        for (int i = 0; i < length; i++) {
            result[i] = vector1[i] ^ vector2[i];
        }
        return result;
    }

    // Aggregate encoded vectors into a single hyperdimensional vector using addition (bundling)
    public static boolean[] aggregateVectors(boolean[][] vectors) {
        int dimension = vectors[0].length;
        int numVectors = vectors.length;

        // Count the number of true values for each position
        int[] trueCounts = new int[dimension];
        for (int i = 0; i < dimension; i++) {
            int count = 0;
            for (boolean[] vector : vectors) {
                if (vector[i]) {
                    count++;
                }
            }
            trueCounts[i] = count;
        }

        // Determine the majority value for each position
        boolean[] aggregatedVector = new boolean[dimension];
        for (int i = 0; i < dimension; i++) {
            aggregatedVector[i] = trueCounts[i] > numVectors / 2;
        }

        return aggregatedVector;
    }




    // Classify sequences using k-NN
    public static int[] classifySequences(boolean[][] trainVectors, int[] trainLabels, boolean[][] testVectors, int k) {
        int[] predictedLabels = new int[testVectors.length];
        for (int i = 0; i < testVectors.length; i++) {
            predictedLabels[i] = classifySequence(trainVectors, trainLabels, testVectors[i], k);
        }
        return predictedLabels;
    }



    // Classify a single sequence using k-NN
    public static int classifySequence(boolean[][] trainVectors, int[] trainLabels, boolean[] testVector, int k) {
        int numTrainVectors = trainVectors.length;
        int[] distances = new int[numTrainVectors];

        // Calculate Hamming distances between the test vector and all training vectors
        for (int i = 0; i < numTrainVectors; i++) {
            distances[i] = calculateHammingDistance(trainVectors[i], testVector);
        }

        // Find the k nearest neighbors
        int[] nearestNeighbors = new int[k];
        Arrays.fill(nearestNeighbors, -1);
        for (int i = 0; i < numTrainVectors; i++) {
            for (int j = 0; j < k; j++) {
                if (nearestNeighbors[j] == -1 || distances[i] < distances[nearestNeighbors[j]]) {
                    System.arraycopy(nearestNeighbors, j, nearestNeighbors, j + 1, k - j - 1);
                    nearestNeighbors[j] = i;
                    break;
                }
            }
        }

        // Count the votes for each label
        int maxLabel = Arrays.stream(trainLabels).max().getAsInt();
        int[] votes = new int[maxLabel + 1];
        for (int i = 0; i < k; i++) {
            if (nearestNeighbors[i] != -1) {
                votes[trainLabels[nearestNeighbors[i]]]++;
            }
        }

        // Find the label with the most votes
        int maxVotes = 0;
        int predictedLabel = -1;
        for (int i = 0; i < votes.length; i++) {
            if (votes[i] > maxVotes) {
                maxVotes = votes[i];
                predictedLabel = i;
            }
        }

        return predictedLabel;
    }


    // Calculate Hamming distance between two binary vectors
    public static int calculateHammingDistance(boolean[] vector1, boolean[] vector2) {
        int distance = 0;
        for (int i = 0; i < vector1.length; i++) {
            if (vector1[i] != vector2[i]) {
                distance++;
            }
        }
        return distance;
    }


}