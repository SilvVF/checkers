package io.silv.checkers

import io.silv.checkers.usecase.checkPieceForLoss
import io.silv.checkers.usecase.generateInitialBoard
import io.silv.checkers.usecase.validatePlacement
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class MoveValidationTest {

    lateinit var board: MutableList<MutableList<Piece>>

    @Before
    fun generateBoard() {
        board = generateInitialBoard().map { it.toMutableList() }.toMutableList()
    }

    @Test
    fun `check out of bounds move`() {

        val result = validatePlacement(board, 0 to 0, 100 to 20 )

        Assert.assertEquals(result.valid, false)
        Assert.assertEquals(board, result.data)
        Assert.assertNull(result.removed)
    }

    @Test
    fun `check single space move red left`() {

        val redPiece = 2 to 1
        val blankSpaceRight = 3 to 0

        val resultLeft = validatePlacement(board,redPiece, blankSpaceRight)

        board[blankSpaceRight.first][blankSpaceRight.second] = Red(false)
        board[redPiece.first][redPiece.second] = Empty

        Assert.assertEquals(true, resultLeft.valid)
        Assert.assertEquals(board, resultLeft.data)
        Assert.assertNull(resultLeft.removed)
    }

    @Test
    fun `check single space move red right`() {

        val redPiece = 2 to 1
        val blankSpaceLeft = 3 to 2

        val resultRight = validatePlacement(board, redPiece, blankSpaceLeft)

        board[3][2] = Red(false)
        board[2][1] = Empty

        Assert.assertEquals(true, resultRight.valid)
        Assert.assertEquals(board, resultRight.data)
        Assert.assertNull(resultRight.removed)
    }

    @Test
    fun `check single space move blue left`() {

        val bluePiece = 5 to 2
        val blankSpaceLeft = 4 to 1

        val resultLeft = validatePlacement(board,bluePiece, blankSpaceLeft)

        board[blankSpaceLeft.first][blankSpaceLeft.second] = Blue(false)
        board[bluePiece.first][bluePiece.second] = Empty

        Assert.assertEquals(true, resultLeft.valid)
        Assert.assertEquals(board, resultLeft.data)
        Assert.assertNull(resultLeft.removed)
    }

    @Test
    fun `check single space move blue right`() {

        val bluePiece = 5 to 2
        val blankSpaceLeft = 4 to 3

        val resultRight = validatePlacement(board, bluePiece, blankSpaceLeft)

        board[blankSpaceLeft.first][blankSpaceLeft.second] = Blue(false)
        board[bluePiece.first][bluePiece.second] = Empty

        Assert.assertEquals(true, resultRight.valid)
        Assert.assertEquals(board, resultRight.data)
        Assert.assertNull(resultRight.removed)
    }

    @Test
    fun `check winner is found no red`() {

        board.forEachIndexed { i,  row ->
            row.forEachIndexed { j, p ->
                if (p.value == Red().value)
                    board[i][j] = Empty
            }
        }

        val blueWin = checkPieceForLoss(board, Red())
        Assert.assertTrue(blueWin)
    }

    @Test
    fun `check winner is found no blue`() {
        board.forEachIndexed { i,  row ->
            row.forEachIndexed { j, p ->
                if (p.value == Blue().value)
                    board[i][j] = Empty
            }
        }

        val redWin = checkPieceForLoss(board, Blue())
        Assert.assertTrue(redWin)
    }

    @Test
    fun `check winner is not found for normal board`() {

        val blueLose = checkPieceForLoss(board, Blue())
        val redLose = checkPieceForLoss(board, Red())
        Assert.assertFalse(blueLose)
        Assert.assertFalse(redLose)
    }
}