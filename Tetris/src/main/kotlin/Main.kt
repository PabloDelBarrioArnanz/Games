package org.delbarriopablo

import org.delbarriopablo.Game.BACKGROUND
import org.delbarriopablo.Game.BOARD_HEIGHT
import org.delbarriopablo.Game.BOARD_WIDTH
import org.delbarriopablo.Game.PIECE_LINE_1
import org.delbarriopablo.Game.PIECE_LINE_2
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.*

object Game {
    const val BOARD_WIDTH = 1200
    const val BOARD_HEIGHT = 1130

    val BACKGROUND: Image = ImageIcon(readResource("./boardbackground.png")).image
    val PIECE_LINE_1: Image = ImageIcon(readResource("linepiece1.png")).image
    val PIECE_LINE_2: Image = ImageIcon(readResource("linepiece2.png")).image

    private fun readResource(resourcePath: String): ByteArray {
        return javaClass.classLoader.getResourceAsStream(resourcePath)?.readAllBytes()!!
    }
}

fun main() {

    val mainFrame = JFrame("Tetris")
        .apply {
            isResizable = false
            setLocationRelativeTo(null)
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            setSize(BOARD_WIDTH, BOARD_HEIGHT)
        }

    mainFrame.add(Tetris().apply {
        requestFocus()
    })

    mainFrame.pack()
    mainFrame.isVisible = true
}

class Tetris : JPanel(), ActionListener, KeyListener {

    init {
        preferredSize = Dimension(BOARD_WIDTH, BOARD_HEIGHT)
        background = Color.BLACK
        isFocusable = true
        addKeyListener(this)
    }

    private val piecesList = Pieces().apply { addPiece(LinePiece()) }

    private val gameLoop = Timer(1000 / 60, this).apply { start() }

    override fun paintComponent(graphics: Graphics?) {
        super.paintComponent(graphics)
        graphics?.drawImage(BACKGROUND, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, null)

        graphics?.color = Color.WHITE
        graphics?.font = Font("Arial", Font.BOLD, 32)
        graphics?.drawString("SCORE 000", 840, 150)
        graphics?.drawString("NEXT", 880, 270)
        graphics?.drawString("SAVE", 880, 630)

        piecesList.getPieces().forEach { piece ->
            graphics?.drawString("@", piece.x, piece.y)
            graphics?.drawImage(
                piece.getImage(),
                piece.x,
                piece.y,
                piece.width(),
                piece.height(),
                null
            )
        }
    }

    override fun actionPerformed(e: ActionEvent?) {
        piecesList.getFallingPiece()?.descend()
        repaint()
    }

    override fun keyPressed(e: KeyEvent?) {
        piecesList.getFallingPiece()
            ?.let { fallingPiece ->
                if (e?.keyCode == KeyEvent.VK_RIGHT && fallingPiece.x + fallingPiece.width() + 10 < 600) {
                    fallingPiece.moveRight()
                } else if (e?.keyCode == KeyEvent.VK_LEFT && fallingPiece.x + fallingPiece.width() - 10 > 104) {
                    fallingPiece.moveLeft()
                } else if (e?.keyCode == KeyEvent.VK_UP) {
                    fallingPiece.rotatePiece()
                }
            }
    }

    override fun keyTyped(e: KeyEvent?) {}

    override fun keyReleased(e: KeyEvent?) {}
}

class Pieces(
    private val piecesList: MutableList<Piece> = mutableListOf(),
) {

    fun addPiece(piece: Piece) {
        piecesList.add(piece)
    }

    fun getPieces() = piecesList

    fun getFallingPiece(): Piece? = piecesList.firstOrNull { piece -> !piece.placed }

}

abstract class Piece(
    open var x: Int = 280,
    open var y: Int = 95,
    open var placed: Boolean = false,
) {
    abstract fun width(): Int
    abstract fun height(): Int
    abstract fun rotatePiece()
    abstract fun getImage(): Image

    fun moveRight() {
        x += 10
    }

    fun moveLeft() {
        x -= 10
    }

    fun descend() {
        y++
    }
}

class LinePiece(
    override var x: Int = 280,
    override var y: Int = 95,
    override var placed: Boolean = false,
    private var rotation: Int = 2,
    private val imagePosition1: Image = PIECE_LINE_1,
    private val imagePosition2: Image = PIECE_LINE_2
) : Piece(x, y, placed) {

    companion object {
        const val WIDTH: Int = 159
        const val HEIGHT: Int = 43
    }

    override fun width(): Int {
        return if (rotation % 2 == 0) WIDTH
        else HEIGHT
    }

    override fun height(): Int {
        return if (rotation % 2 == 0) HEIGHT
        else WIDTH
    }

    override fun rotatePiece() {
        rotation++
    }

    override fun getImage(): Image {
        return if (rotation % 2 == 0) imagePosition1
        else imagePosition2
    }
}
