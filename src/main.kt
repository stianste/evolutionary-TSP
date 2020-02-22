import jdk.nashorn.internal.runtime.JSType.toDouble
import kotlin.test.assertEquals

val exampleDistanceMatrix = arrayOf(
    doubleArrayOf(0.0,  3.0,  4.0,  2.0,  7.0),
    doubleArrayOf(3.0,  0.0,  4.0,  6.0,  3.0),
    doubleArrayOf(4.0,  4.0,  0.0,  5.0,  8.0),
    doubleArrayOf(2.0,  6.0,  5.0,  0.0,  6.0),
    doubleArrayOf(7.0,  3.0,  8.0,  6.0,  0.0)
)

fun printPopulationMember(member: IntArray) {
    print("[")
    member.forEach { print(it) }
    println("]")
}

fun getDistanceMatrixFromFile(filepath: String): Array<DoubleArray> {
    // TODO Actually read from file
    println("Filename: $filepath") // Placeholder for unused variable
    return exampleDistanceMatrix
}

fun generateRandomSolution(totalNumberOfCities: Int): IntArray {
    // Start at 1 to omit the origin city
    return (1 until totalNumberOfCities).toList().shuffled().toIntArray()
}

fun testScoreFitnessOptimal() {
    // TODO: Write proper tests suite
    // Subtract one from optimal solution for 0 index
    val bestSolution = intArrayOf(1, 3, 2, 5, 4).map { it - 1 }.toIntArray()
    val expectedFitness = -19.0
    val actualFitness: Double = scoreFitness(bestSolution, exampleDistanceMatrix)

    assertEquals(expectedFitness, actualFitness)
}

fun testScoreFitnessNaive() {
    val naiveSolution = intArrayOf(1, 2, 3, 4, 5).map {it - 1}.toIntArray()
    val expectedFitness = -1 * toDouble(3 + 4 + 5 + 6 + 7)
    val actualFitness: Double = scoreFitness(naiveSolution, exampleDistanceMatrix)

    assertEquals(expectedFitness, actualFitness)
}

fun scoreFitness(solution: IntArray, distanceMatrix: Array<DoubleArray>): Double {
    var routeLength = distanceMatrix[0][solution[0]]
    for ( i in 1 until solution.size) {
        routeLength += distanceMatrix[solution[i-1]][solution[i]]
    }

    return -1 * (routeLength + distanceMatrix[solution.last()][0])
}

fun main() {
    // Step One: Generate the initial population of individuals randomly. (First generation)
    //
    // Step Two: Evaluate the fitness of each individual in that population (time limit, sufficient fitness achieved, etc.)
    //
    // Step Three: Repeat the following regenerational steps until termination:
    //
    // Select the best-fit individuals for reproduction. (Parents)
    // Breed new individuals through crossover and mutation operations to give birth to offspring.
    // Evaluate the individual fitness of new individuals.
    // Replace least-fit population with new individuals.

    //  Best solution for five: 19
    //  Best solution for FRI26: 937

    val populationSize = 10
    val filepath = "./data/five/five_d.txt"
    val distances = getDistanceMatrixFromFile(filepath)
    println(distances[0][1])

    val solutionLength = distances.size

    val population = Array(solutionLength) { generateRandomSolution(solutionLength)}
    population.forEach { member -> printPopulationMember(member) }
    testScoreFitnessNaive()
    testScoreFitnessOptimal()
}