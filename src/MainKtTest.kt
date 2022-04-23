import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class MainKtTest {
    @Test
    fun `test get worst potential score`() {
        val expected = 7.0 + 6.0 + 8.0 + 6.0 + 8.0
        val actual = getWorstPotentialScore(exampleDistanceMatrix)
        assertEquals(expected, actual)
    }

    @Test
    fun testMapStringToDoubleArray() {
        val input = "1.0 2.0 3.0 4.0"
        val expected = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        val actual = mapStringToDoubleArray(input)
        assertTrue(expected contentEquals actual)
    }

    @Test
    fun testGetDistanceMatrixFromFile() {
        val distanceMatrix = getDistanceMatrixFromFile(five_filepath)

        assertTrue(distanceMatrix contentDeepEquals exampleDistanceMatrix)
    }

    @Test
    fun testScoreFitnessNaive() {
        val naiveSolution = intArrayOf(1, 2, 3, 4, 5).map {it - 1}.toIntArray()
        val expectedFitness = 35.0 - 25.0
        val actualFitness: Double = scoreFitness(naiveSolution, exampleDistanceMatrix, maxFitness = 35.0)

        assertEquals(expectedFitness, actualFitness)
    }

    @Test
    fun testScoreFitnessOptimal() {
        // Subtract one from optimal solution for 0 index
        val bestSolution = intArrayOf(1, 3, 2, 5, 4).map { it - 1 }.toIntArray()
        val expectedFitness = 35 - 19.0
        val actualFitness: Double = scoreFitness(bestSolution, exampleDistanceMatrix, maxFitness = 35.0)

        assertEquals(expectedFitness, actualFitness)
    }

    @Test
    fun testCreateSelectionTable() {
        val bestSolution = intArrayOf(1, 3, 2, 5, 4).map { it - 1 }.toIntArray()

        val naiveSolution = intArrayOf(1, 2, 3, 4, 5).map {it - 1}.toIntArray()

        val population = arrayOf(bestSolution, naiveSolution)
        val selectionTable = createSelectionTable(population, exampleDistanceMatrix, worstPossibleScore = 35.0)

        // The best solution should have 16 tickets, and the naive solution should have 10 tickets,
        // totaling 26 tickets
        assertEquals(26, selectionTable.size)
    }


}
