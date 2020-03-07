import java.io.File
import kotlin.random.Random.Default.nextDouble
import kotlin.random.Random.Default.nextInt
import kotlin.test.assertEquals
import kotlin.test.assertTrue

val exampleDistanceMatrix = arrayOf(
    doubleArrayOf(0.0,  3.0,  4.0,  2.0,  7.0),
    doubleArrayOf(3.0,  0.0,  4.0,  6.0,  3.0),
    doubleArrayOf(4.0,  4.0,  0.0,  5.0,  8.0),
    doubleArrayOf(2.0,  6.0,  5.0,  0.0,  6.0),
    doubleArrayOf(7.0,  3.0,  8.0,  6.0,  0.0)
)

val five_filepath = "./src/data/five/five_d.txt"
val fri26_filepath = "./src/data/FRI26/fri26_d.txt"

fun printPopulationMember(member: IntArray) {
    print("[")
    member.forEach {print("$it, ")}
    println("]")
}

private fun mapStringToDoubleArray(s: String): DoubleArray {
    return s.split(" ").filter{it != ""}.map { it.toDouble() }.toDoubleArray()
}

fun testMapStringToDoubleArray() {
    val input = "1.0 2.0 3.0 4.0"
    val expected = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
    val actual = mapStringToDoubleArray(input)
    assertTrue(expected contentEquals actual)
}

fun getDistanceMatrixFromFile(fileName: String): Array<DoubleArray> {

    val distanceMatrix = mutableListOf<DoubleArray>()

    File(fileName).useLines { line ->
        line.toList().filter{it != ""}.map {
            distanceMatrix.add(mapStringToDoubleArray(it))
        }
    }

    return distanceMatrix.toTypedArray()
}

fun testGetDistanceMatrixFromFile() {
    val distanceMatrix = getDistanceMatrixFromFile(five_filepath)

    assertTrue(distanceMatrix contentDeepEquals exampleDistanceMatrix)
}

fun generateRandomSolution(totalNumberOfCities: Int): IntArray {
    // Start at 1 to omit the origin city
    return (1 until totalNumberOfCities).toList().shuffled().toIntArray()
}

fun testScoreFitnessOptimal() {
    // TODO: Write proper tests suite
    // Subtract one from optimal solution for 0 index
    val bestSolution = intArrayOf(1, 3, 2, 5, 4).map { it - 1 }.toIntArray()
    val expectedFitness = 35 - 19.0 // 11
    val actualFitness: Double = scoreFitness(bestSolution, exampleDistanceMatrix, maxFitness = 35.0)

    assertEquals(expectedFitness, actualFitness)
}

fun testScoreFitnessNaive() {
    val naiveSolution = intArrayOf(1, 2, 3, 4, 5).map {it - 1}.toIntArray()
    val expectedFitness = 35.0 - 25.0 // 5.0
    val actualFitness: Double = scoreFitness(naiveSolution, exampleDistanceMatrix, maxFitness = 35.0)

    assertEquals(expectedFitness, actualFitness)
}

fun scoreFitness(solution: IntArray, distanceMatrix: Array<DoubleArray>, maxFitness: Double): Double {
    var routeLength = distanceMatrix[0][solution[0]]
    for ( i in 1 until solution.size) {
        routeLength += distanceMatrix[solution[i-1]][solution[i]]
    }

    return maxFitness - (routeLength + distanceMatrix[solution.last()][0])
}

fun testOrderCrossover() {
    //    TODO: Mock Random.nextInt. Only works with split index = 3
    //    val parent1 = intArrayOf(3,4,8,2,7,1,6,5)
    //    val parent2 = intArrayOf(4,2,5,1,6,8,3,7)
    //    val expectedOffspring = intArrayOf(5,6,8,2,7,1,3,4)
    //
    //    val actualOffspring: IntArray = orderCrossover(parent1, parent2)
    //    printPopulationMember(actualOffspring)
    //    assertTrue(expectedOffspring.contentEquals(actualOffspring))
}

fun orderCrossover(parent1: IntArray, parent2: IntArray, windowSize: Int = 3): IntArray {
    val parentLengths = parent1.size
    val splitIndex = nextInt(parentLengths - windowSize - 1)

    val offspring = IntArray(parentLengths)

    for (i in splitIndex until splitIndex + windowSize) {
        offspring[i] = parent1[i]
    }

    var offspringBaseIndex = 0
    for (i in parent2.indices) {
        val parentIndex  = (i + splitIndex + windowSize) % parentLengths
        val offspringIndex  = (offspringBaseIndex + splitIndex + windowSize) % parentLengths
        val candidate: Int = parent2[parentIndex]
        if (!offspring.contains(candidate)) {
            offspring[offspringIndex] = candidate
            offspringBaseIndex += 1
        }
    }

    return offspring
}

fun mutate(genome: IntArray, mutationChance: Double = 0.01): IntArray {
    if (nextDouble() > mutationChance) {
        return genome
    }

    val firstSwapIndex = nextInt(genome.size)
    var secondSwapIndex = nextInt(genome.size)

    while (secondSwapIndex == firstSwapIndex)
        secondSwapIndex = nextInt(genome.size)

    val newGenome = genome.toMutableList()
    newGenome[firstSwapIndex] = genome[secondSwapIndex]
    newGenome[secondSwapIndex] = genome[firstSwapIndex]

    return newGenome.toIntArray()
}

fun createSelectionTable(
    population: Array<IntArray>,
    distanceMatrix: Array<DoubleArray>,
    WORST_POSSIBLE_SCORE: Double
): MutableList<IntArray> {

    val selectionTable = mutableListOf<IntArray>()
    population.forEach { member ->
        val fitness = scoreFitness(member, distanceMatrix, WORST_POSSIBLE_SCORE)
        val numLotteryTickets = fitness.toInt()
        (0 until numLotteryTickets).map {selectionTable.add(member)}
    }

    return selectionTable
}

fun naturalSelection(selectionTable:  MutableList<IntArray>): Pair<IntArray, IntArray> {

    val selectedCanidates = selectionTable.shuffled().take(2)
    return Pair(selectedCanidates[0], selectedCanidates[1])
}

fun testCreateSelectionTable() {
    // 35 - 19 = 16 fitness
    val bestSolution = intArrayOf(1, 3, 2, 5, 4).map { it - 1 }.toIntArray()

    // 35 - 25 = 10 fitness
    val naiveSolution = intArrayOf(1, 2, 3, 4, 5).map {it - 1}.toIntArray()

    val population = arrayOf(bestSolution, naiveSolution)
    val selectionTable = createSelectionTable(population, exampleDistanceMatrix, WORST_POSSIBLE_SCORE = 35.0)

    // The best solution should have 16 tickets, and the naive solution should have 10 tickets,
    // totaling 16 + 10 = 26 tickets
    assertEquals(26, selectionTable.size)
}

fun getWorstPotentialScore(distanceMatrix: Array<DoubleArray>): Double {
    return distanceMatrix.mapNotNull {it.max()}.toTypedArray().sumByDouble { it }
}

fun testGetWorstPotentialScore() {
    val expected = 7.0 + 6.0 + 8.0 + 6.0 + 8.0
    val actual = getWorstPotentialScore(exampleDistanceMatrix)
    assertEquals(expected, actual)
}

fun <T> verifyAllValuesInArrayUnique(array: Array<T>): Boolean {
    return array.toSet().size == array.size
}

fun verifyPotentialSolutions(newMember: IntArray, mutatedMember: IntArray) {
    if (!(verifyAllValuesInArrayUnique(newMember.toTypedArray()))) {
        println("WARNING: member of population is invalid")
        printPopulationMember(newMember)
    }

    if (!(verifyAllValuesInArrayUnique(mutatedMember.toTypedArray()))) {
        println("WARNING: mutated child is invalid:")
        printPopulationMember(mutatedMember)
    }
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

    testGetWorstPotentialScore()
    testMapStringToDoubleArray()
    testGetDistanceMatrixFromFile()
    testScoreFitnessNaive()
    testScoreFitnessOptimal()
    testOrderCrossover()
    testCreateSelectionTable()

    val FILEPATH = five_filepath
    val POPULATION_SIZE = 100
    val MUTATION_CHANCE = 0.01
    val MAX_ITERATIONS = 100
    val WINDOW_SIZE = 2

    val distanceMatrix = getDistanceMatrixFromFile(FILEPATH)
    val WORST_POSSIBLE_SCORE = getWorstPotentialScore(distanceMatrix)

    println("Worst possible score: $WORST_POSSIBLE_SCORE")

    val currentPopulation = Array(POPULATION_SIZE) { generateRandomSolution(distanceMatrix.size)}
    println("Preview of initial population:")
    currentPopulation.take(10).forEach ( ::printPopulationMember )

    var bestFitness = 0.0

    (0..MAX_ITERATIONS).forEach{ generationNumber ->
        val nextPopulation = mutableListOf<IntArray>()
        val selectionTable = createSelectionTable(currentPopulation, distanceMatrix, WORST_POSSIBLE_SCORE)

        repeat((0..POPULATION_SIZE).count()) {
            val parents = naturalSelection(selectionTable)

            val newMember = orderCrossover(parents.first, parents.second, WINDOW_SIZE)
            val mutatedMember = mutate(newMember, MUTATION_CHANCE)

            verifyPotentialSolutions(newMember, mutatedMember)

            val fitness = scoreFitness(mutatedMember, distanceMatrix, WORST_POSSIBLE_SCORE)
            if (fitness > bestFitness) {
                bestFitness = fitness
                println("Improvement in generation number $generationNumber:")
                printPopulationMember(mutatedMember)
                println(fitness)
            }

            nextPopulation.add(mutatedMember)
        }
    }
}