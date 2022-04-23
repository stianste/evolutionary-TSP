import java.io.File
import kotlin.random.Random

val random = Random(1)

val exampleDistanceMatrix = arrayOf(
    doubleArrayOf(0.0,  3.0,  4.0,  2.0,  7.0),
    doubleArrayOf(3.0,  0.0,  4.0,  6.0,  3.0),
    doubleArrayOf(4.0,  4.0,  0.0,  5.0,  8.0),
    doubleArrayOf(2.0,  6.0,  5.0,  0.0,  6.0),
    doubleArrayOf(7.0,  3.0,  8.0,  6.0,  0.0)
)

val five_filepath = "./src/data/five/five_d.txt"
val p01_filepath = "./src/data/p01/p01_d.txt"
val fri26_filepath = "./src/data/FRI26/fri26_d.txt"

fun printPopulationMember(member: IntArray) {
    print("[")
    member.forEach {
        val zeroAdjusted = it + 1
        print("$zeroAdjusted, ")
    }
    println("]")
}

fun mapStringToDoubleArray(s: String): DoubleArray {
    return s.split(" ").filter{it != ""}.map { it.toDouble() }.toDoubleArray()
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


fun generateRandomSolution(totalNumberOfCities: Int): IntArray {
    // Start at 1 to omit the origin city
    return (1 until totalNumberOfCities).toList().shuffled(random).toIntArray()
}

fun scoreFitness(solution: IntArray, distanceMatrix: Array<DoubleArray>, maxFitness: Double): Double {
    var routeLength = distanceMatrix[0][solution[0]]
    for (i in 1 until solution.size) {
        routeLength += distanceMatrix[solution[i-1]][solution[i]]
    }

    routeLength += distanceMatrix[solution.last()][0]

    return maxFitness - routeLength
}


fun orderCrossover(parent1: IntArray, parent2: IntArray, windowSize: Int = 3): IntArray {
    val parentLengths = parent1.size
    val splitIndex = random.nextInt(parentLengths - windowSize - 1)

    val offspring = IntArray(parentLengths)

    for (i in splitIndex until splitIndex + windowSize) {
        offspring[i] = parent1[i]
    }

    var offspringBaseIndex = 0
    for (i in parent2.indices) {
        val parentIndex  = (i + splitIndex + windowSize) % parentLengths
        val offspringIndex  = (offspringBaseIndex + splitIndex + windowSize) % parentLengths
        val candidate = parent2[parentIndex]
        if (!offspring.contains(candidate)) {
            offspring[offspringIndex] = candidate
            offspringBaseIndex += 1
        }
    }

    return offspring
}

fun mutate(genome: IntArray, mutationChance: Double = 0.01): IntArray {
    if (random.nextDouble() > mutationChance) {
        return genome
    }

    val firstSwapIndex = random.nextInt(genome.size)
    var secondSwapIndex = random.nextInt(genome.size)

    while (secondSwapIndex == firstSwapIndex)
        secondSwapIndex = random.nextInt(genome.size)

    val newGenome = genome.toMutableList()
    newGenome[firstSwapIndex] = genome[secondSwapIndex]
    newGenome[secondSwapIndex] = genome[firstSwapIndex]

    return newGenome.toIntArray()
}

fun createSelectionTable(
    population: Array<IntArray>,
    distanceMatrix: Array<DoubleArray>,
    worstPossibleScore: Double,
    likelyhoodCoeficient: Int = 1

): MutableList<IntArray> {

    val selectionTable = mutableListOf<IntArray>()
    population.forEach { member ->
        val fitness = scoreFitness(member, distanceMatrix, worstPossibleScore)
        val numLotteryTickets = fitness.toInt() * likelyhoodCoeficient
        (0 until numLotteryTickets).map {selectionTable.add(member)}
    }

    return selectionTable
}

fun deterministicTournamentSelection(
        population: Array<IntArray>,
        distanceMatrix: Array<DoubleArray>,
        worstPossibleScore: Double,
        k: Int = 10
    ): IntArray {

    val tournamentCandidates = population.toList().shuffled(random).take(k)
    return tournamentCandidates.maxByOrNull { scoreFitness(it, distanceMatrix, worstPossibleScore) }!!
}

fun naturalSelectionByTournament(
        population: Array<IntArray>,
        distanceMatrix: Array<DoubleArray>,
        worstPossibleScore: Double,
        k: Int = 10
    ): Pair<IntArray, IntArray> {

    return Pair(
        deterministicTournamentSelection(population, distanceMatrix, worstPossibleScore, k),
        deterministicTournamentSelection(population, distanceMatrix, worstPossibleScore, k)
    )
}

fun naturalSelection(selectionTable:  MutableList<IntArray>): Pair<IntArray, IntArray> {
    val selectedCandidates = selectionTable.shuffled(random).take(2)
    return Pair(selectedCandidates[0], selectedCandidates[1])
}


fun getWorstPotentialScore(distanceMatrix: Array<DoubleArray>): Double {
    return distanceMatrix.mapNotNull { it.maxOrNull() }.toTypedArray().sumOf { it }
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

fun combinePopulations(
    currentPopulation: Array<IntArray>,
    nextPopulation: Array<IntArray>,
    ratio: Double,
    distanceMatrix: Array<DoubleArray>,
    worstPossibleScore: Double
): Array<IntArray> {

    fun getTopRatioCandidatesOf(population: Array<IntArray>, ratio: Double): Array<IntArray> {
        return population.sortedBy { -1.0 * scoreFitness(it, distanceMatrix, worstPossibleScore) }
            .take((population.size * ratio).toInt()).toTypedArray()
    }

    return getTopRatioCandidatesOf(currentPopulation, 1.0 - ratio) +
            getTopRatioCandidatesOf(nextPopulation, ratio)
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
    //  Best solution for p01: 291
    //  Best solution for FRI26: 937

    val startTime = System.currentTimeMillis()

    val FILEPATH = p01_filepath
    val optimalSolution = if (FILEPATH == fri26_filepath) 937 else 291

    val POPULATION_SIZE = 1000
    val TOURNAMENT_SIZE = POPULATION_SIZE / 10
    val MUTATION_CHANCE = 0.03
    val NEW_OLD_POPULATION_RATIO = 0.1
    val MAX_ITERATIONS = 200
    val WINDOW_SIZE = 8

    val distanceMatrix = getDistanceMatrixFromFile(FILEPATH)
    val WORST_POSSIBLE_SCORE = getWorstPotentialScore(distanceMatrix)

    println("Worst possible score: $WORST_POSSIBLE_SCORE")

    var currentPopulation = Array(POPULATION_SIZE) { generateRandomSolution(distanceMatrix.size)}
    println("Preview of initial population:")
    currentPopulation.take(10).forEach ( ::printPopulationMember )

    var bestFitness = 0.0
    var bestSolution = intArrayOf()
    var bestSolutionGeneration = 0
    var bestSolutionTime = 0L

    (0..MAX_ITERATIONS).forEach{ generationNumber ->
        val nextPopulation = mutableListOf<IntArray>()
        val generationFitnesses = DoubleArray(POPULATION_SIZE)

        (0 until POPULATION_SIZE).forEach {
            val parents = naturalSelectionByTournament(
                currentPopulation,
                distanceMatrix,
                WORST_POSSIBLE_SCORE,
                k = TOURNAMENT_SIZE
            )
            val newMember = orderCrossover(parents.first, parents.second, WINDOW_SIZE)
            val mutatedMember = mutate(newMember, MUTATION_CHANCE)

            verifyPotentialSolutions(newMember, mutatedMember)

            val fitness = scoreFitness(mutatedMember, distanceMatrix, WORST_POSSIBLE_SCORE)
            generationFitnesses[it] = fitness

            if (fitness > bestFitness) {
                bestFitness = fitness
                bestSolution = mutatedMember
                bestSolutionGeneration = generationNumber
                bestSolutionTime = System.currentTimeMillis() - startTime

                println("Improvement in generation number $generationNumber:")
                printPopulationMember(mutatedMember)
                println(fitness)

                val originalRouteLength = WORST_POSSIBLE_SCORE - fitness
                println("Resulting in route length: $originalRouteLength")
            }

            nextPopulation.add(mutatedMember)
        }

        val averageGenerationFitness = generationFitnesses.sum() / generationFitnesses.size
        println("Average fitness of generation $generationNumber was $averageGenerationFitness")

        currentPopulation = combinePopulations(
            currentPopulation,
            nextPopulation.toTypedArray(),
            NEW_OLD_POPULATION_RATIO,
            distanceMatrix,
            WORST_POSSIBLE_SCORE
        )
    }

    println("Finished after $MAX_ITERATIONS iterations. Best fitness: $bestFitness, " +
            "which results in route length ${WORST_POSSIBLE_SCORE - bestFitness}, " +
            "which is ${WORST_POSSIBLE_SCORE - bestFitness - optimalSolution} " +
            "away from optimal solution. " +
            "This solution was found in generation $bestSolutionGeneration " +
            "after $bestSolutionTime ms"
    )
    printPopulationMember(bestSolution)
}

