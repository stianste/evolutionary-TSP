fun getDistanceMatrixFromFile(filepath: String): Array<DoubleArray> {
    // TODO Actually read from file
    println("Filename: $filepath") // Placeholder for unused variable

    return arrayOf(
        doubleArrayOf(0.0,  3.0,  4.0,  2.0,  7.0),
        doubleArrayOf(3.0,  0.0,  4.0,  6.0,  3.0),
        doubleArrayOf(4.0,  4.0,  0.0,  5.0,  8.0),
        doubleArrayOf(2.0,  6.0,  5.0,  0.0,  6.0),
        doubleArrayOf(7.0,  3.0,  8.0,  6.0,  0.0)
    )
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

    val filepath = "./data/five/five_d.txt"
    val distances = getDistanceMatrixFromFile(filepath)
    println(distances[0][1])

}