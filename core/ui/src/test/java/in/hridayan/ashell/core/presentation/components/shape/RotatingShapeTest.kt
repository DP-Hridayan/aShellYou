package `in`.hridayan.ashell.core.presentation.components.shape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RotatingShapeTest {

    @Test
    fun equals_whenSameBaseShapeAndRotationDegrees_returnsTrue() {
        val shapeA = RotatingShape(
            baseShape = RectangleShape,
            rotationDegrees = 45f,
        )
        val shapeB = RotatingShape(
            baseShape = RectangleShape,
            rotationDegrees = 45f,
        )

        assertEquals(shapeA, shapeB)
        assertEquals(shapeA.hashCode(), shapeB.hashCode())
    }

    @Test
    fun equals_whenDifferentRotationDegrees_returnsFalse() {
        val shapeA = RotatingShape(
            baseShape = RectangleShape,
            rotationDegrees = 45f,
        )
        val shapeB = RotatingShape(
            baseShape = RectangleShape,
            rotationDegrees = 90f,
        )

        assertNotEquals(shapeA, shapeB)
    }

    @Test
    fun equals_whenDifferentBaseShape_returnsFalse() {
        val shapeA = RotatingShape(
            baseShape = RectangleShape,
            rotationDegrees = 45f,
        )
        val shapeB = RotatingShape(
            baseShape = RoundedCornerShape(8),
            rotationDegrees = 45f,
        )

        assertNotEquals(shapeA, shapeB)
    }

    @Test
    fun secondaryConstructor_evaluatesRotationDegreesCorrectly() {
        val shape = RotatingShape(
            baseShape = RectangleShape,
            rotationDegrees = { 180f },
        )

        assertEquals(180f, shape.rotationDegrees)
    }
}
